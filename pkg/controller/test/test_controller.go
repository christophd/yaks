/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test

import (
	"context"

	"github.com/citrusframework/yaks/pkg/util/digest"
	batchv1 "k8s.io/api/batch/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/client-go/rest"
	k8sclient "sigs.k8s.io/controller-runtime/pkg/client"

	"sigs.k8s.io/controller-runtime/pkg/controller"
	"sigs.k8s.io/controller-runtime/pkg/event"
	"sigs.k8s.io/controller-runtime/pkg/handler"
	"sigs.k8s.io/controller-runtime/pkg/manager"
	"sigs.k8s.io/controller-runtime/pkg/predicate"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
	"sigs.k8s.io/controller-runtime/pkg/source"

	"github.com/citrusframework/yaks/pkg/apis/yaks/v1alpha1"
	"github.com/citrusframework/yaks/pkg/client"
)

/**
* USER ACTION REQUIRED: This is a scaffold file intended for the user to modify with their own Controller
* business logic.  Delete these comments after modifying this file.*
 */

// Add creates a new Test Controller and adds it to the Manager. The Manager will set fields on the Controller
// and Start it when the Manager is Started.
func Add(mgr manager.Manager) error {
	c, err := client.FromManager(mgr)
	if err != nil {
		return err
	}
	return add(mgr, newReconciler(mgr, c, mgr.GetConfig()))
}

// newReconciler returns a new reconcile.Reconciler.
func newReconciler(mgr manager.Manager, c client.Client, cfg *rest.Config) reconcile.Reconciler {
	return &ReconcileIntegrationTest{
		client: c,
		scheme: mgr.GetScheme(),
		config: cfg,
	}
}

// add adds a new Controller to mgr with r as the reconcile.Reconciler.
func add(mgr manager.Manager, r reconcile.Reconciler) error {
	// Create a new controller
	c, err := controller.New("test-controller", mgr, controller.Options{Reconciler: r})
	if err != nil {
		return err
	}

	// Watch for changes to primary resource Test
	err = c.Watch(&source.Kind{Type: &v1alpha1.Test{}}, &handler.EnqueueRequestForObject{}, predicate.Funcs{
		UpdateFunc: func(e event.UpdateEvent) bool {
			oldTest, ok := e.ObjectOld.(*v1alpha1.Test)
			if !ok {
				return false
			}
			newTest, ok := e.ObjectNew.(*v1alpha1.Test)
			if !ok {
				return false
			}
			// Ignore updates to the test status in which case metadata.Generation does not change,
			// or except when the test phase changes as it's used to transition from one phase
			// to another
			return oldTest.Generation != newTest.Generation ||
				oldTest.Status.Phase != newTest.Status.Phase
		},
		DeleteFunc: func(e event.DeleteEvent) bool {
			// Evaluates to false if the object has been confirmed deleted
			return !e.DeleteStateUnknown
		},
	})
	if err != nil {
		return err
	}

	// Watch for related Jobs changing
	err = c.Watch(&source.Kind{Type: &batchv1.Job{}},
		handler.EnqueueRequestsFromMapFunc(func(a k8sclient.Object) []reconcile.Request {
			var requests []reconcile.Request

			if job, ok := a.(*batchv1.Job); ok {
				if testName, ok := job.Labels["yaks.citrusframework.org/test"]; ok {
					requests = append(requests, reconcile.Request{
						NamespacedName: types.NamespacedName{
							Namespace: job.Namespace,
							Name:      testName,
						},
					})
				}
			}

			return requests
		}),
	)

	if err != nil {
		return err
	}

	return nil
}

var _ reconcile.Reconciler = &ReconcileIntegrationTest{}

// ReconcileIntegrationTest reconciles a IntegrationTest object.
type ReconcileIntegrationTest struct {
	// This client, initialized using mgr.Client() above, is a split client
	// that reads objects from the cache and writes to the apiserver
	client client.Client
	scheme *runtime.Scheme
	config *rest.Config
}

// Reconcile reads that state of the cluster for a Test object and makes changes based on the state read
// and what is in the Test.Spec
// Note:
// The Controller will requeue the Request to be processed again if the returned error is non-nil or
// Result.Requeue is true, otherwise upon completion it will remove the work from the queue.
func (r *ReconcileIntegrationTest) Reconcile(ctx context.Context, request reconcile.Request) (reconcile.Result, error) {
	rlog := Log.WithValues("request-namespace", request.Namespace, "request-name", request.Name)
	rlog.Info("Reconciling Test")

	// Fetch the Test instance
	var instance v1alpha1.Test

	if err := r.client.Get(ctx, request.NamespacedName, &instance); err != nil {
		if k8serrors.IsNotFound(err) {
			// Request object not found, could have been deleted after reconcile request.
			// Owned objects are automatically garbage collected. For additional cleanup logic use finalizers.
			// Return and don't requeue
			return reconcile.Result{}, nil
		}
		// Error reading the object - requeue the request.
		return reconcile.Result{}, err
	}

	// Delete phase
	if instance.GetDeletionTimestamp() != nil {
		instance.Status.Phase = v1alpha1.TestPhaseDeleting
	}

	target := instance.DeepCopy()
	targetLog := rlog.ForTest(target)

	actions := []Action{
		NewInitializeAction(),
		NewNoopAction(),
		NewStartAction(),
		NewEvaluateAction(),
		NewMonitorAction(),
	}

	for _, a := range actions {
		a.InjectClient(r.client)
		a.InjectConfig(r.config)
		a.InjectLogger(targetLog)

		if a.CanHandle(target) {
			targetLog.Infof("Invoking action %s", a.Name())

			newTarget, err := a.Handle(ctx, target)
			if err != nil {
				return reconcile.Result{}, err
			}

			if newTarget != nil {
				dgst, err := digest.ComputeForTest(newTarget)
				if err != nil {
					return reconcile.Result{}, err
				}

				newTarget.Status.Digest = dgst

				if err := r.client.Status().Patch(ctx, newTarget, k8sclient.MergeFrom(&instance)); err != nil && k8serrors.IsConflict(err) {
					targetLog.Error(err, "conflict in updating test to status "+string(target.Status.Phase))

					return reconcile.Result{
						Requeue: true,
					}, nil
				} else if err != nil {
					return reconcile.Result{}, err
				}

				if newTarget.Status.Phase != target.Status.Phase {
					targetLog.Info(
						"state transition",
						"phase-from", target.Status.Phase,
						"phase-to", newTarget.Status.Phase,
					)
				}
			}

			// handle one action at time so the resource
			// is always at its latest state
			break
		}
	}

	return reconcile.Result{}, nil
}
