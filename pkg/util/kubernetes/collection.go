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

package kubernetes

import (
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	"k8s.io/apimachinery/pkg/runtime"
	ctrl "sigs.k8s.io/controller-runtime/pkg/client"
)

// A Collection is a container of Kubernetes resources.
type Collection struct {
	items []ctrl.Object
}

// NewCollection creates a new empty collection.
func NewCollection(objects ...ctrl.Object) *Collection {
	collection := Collection{
		items: make([]ctrl.Object, 0, len(objects)),
	}

	collection.items = append(collection.items, objects...)

	return &collection
}

// Size returns the number of resources belonging to the collection.
func (c *Collection) Size() int {
	return len(c.items)
}

// Items returns all resources belonging to the collection.
func (c *Collection) Items() []ctrl.Object {
	return c.items
}

// AsKubernetesList returns all resources wrapped in a Kubernetes list.
func (c *Collection) AsKubernetesList() *corev1.List {
	lst := corev1.List{
		TypeMeta: metav1.TypeMeta{
			Kind:       "List",
			APIVersion: "v1",
		},
		Items: make([]runtime.RawExtension, 0, len(c.items)),
	}
	for _, res := range c.items {
		raw := runtime.RawExtension{
			Object: res,
		}
		lst.Items = append(lst.Items, raw)
	}
	return &lst
}

// Add adds a resource to the collection.
func (c *Collection) Add(resource ctrl.Object) {
	c.items = append(c.items, resource)
}

// AddAll adds all resources to the collection.
func (c *Collection) AddAll(resource []ctrl.Object) {
	c.items = append(c.items, resource...)
}

// VisitDeployment executes the visitor function on all Deployment resources.
func (c *Collection) VisitDeployment(visitor func(*appsv1.Deployment)) {
	c.Visit(func(res ctrl.Object) {
		if conv, ok := res.(*appsv1.Deployment); ok {
			visitor(conv)
		}
	})
}

// GetDeployment returns a Deployment that matches the given function.
func (c *Collection) GetDeployment(filter func(*appsv1.Deployment) bool) *appsv1.Deployment {
	var retValue *appsv1.Deployment
	c.VisitDeployment(func(re *appsv1.Deployment) {
		if filter(re) {
			retValue = re
		}
	})
	return retValue
}

// HasDeployment returns true if a deployment matching the given condition is present.
func (c *Collection) HasDeployment(filter func(*appsv1.Deployment) bool) bool {
	return c.GetDeployment(filter) != nil
}

// RemoveDeployment removes and returns a Deployment that matches the given function.
func (c *Collection) RemoveDeployment(filter func(*appsv1.Deployment) bool) *appsv1.Deployment {
	res := c.Remove(func(res ctrl.Object) bool {
		if conv, ok := res.(*appsv1.Deployment); ok {
			return filter(conv)
		}
		return false
	})
	if res == nil {
		return nil
	}
	deploy, ok := res.(*appsv1.Deployment)
	if !ok {
		return nil
	}

	return deploy
}

// VisitConfigMap executes the visitor function on all ConfigMap resources.
func (c *Collection) VisitConfigMap(visitor func(*corev1.ConfigMap)) {
	c.Visit(func(res ctrl.Object) {
		if conv, ok := res.(*corev1.ConfigMap); ok {
			visitor(conv)
		}
	})
}

// GetConfigMap returns a ConfigMap that matches the given function.
func (c *Collection) GetConfigMap(filter func(*corev1.ConfigMap) bool) *corev1.ConfigMap {
	var retValue *corev1.ConfigMap
	c.VisitConfigMap(func(re *corev1.ConfigMap) {
		if filter(re) {
			retValue = re
		}
	})
	return retValue
}

// RemoveConfigMap removes and returns a ConfigMap that matches the given function.
func (c *Collection) RemoveConfigMap(filter func(*corev1.ConfigMap) bool) *corev1.ConfigMap {
	res := c.Remove(func(res ctrl.Object) bool {
		if conv, ok := res.(*corev1.ConfigMap); ok {
			return filter(conv)
		}
		return false
	})
	if res == nil {
		return nil
	}
	cfgMap, ok := res.(*corev1.ConfigMap)
	if !ok {
		return nil
	}

	return cfgMap
}

// VisitService executes the visitor function on all Service resources.
func (c *Collection) VisitService(visitor func(*corev1.Service)) {
	c.Visit(func(res ctrl.Object) {
		if conv, ok := res.(*corev1.Service); ok {
			visitor(conv)
		}
	})
}

// GetService returns a Service that matches the given function.
func (c *Collection) GetService(filter func(*corev1.Service) bool) *corev1.Service {
	var retValue *corev1.Service
	c.VisitService(func(re *corev1.Service) {
		if filter(re) {
			retValue = re
		}
	})
	return retValue
}

// VisitContainer executes the visitor function on all Containers inside deployments or other resources.
func (c *Collection) VisitContainer(visitor func(container *corev1.Container)) {
	c.VisitDeployment(func(d *appsv1.Deployment) {
		for idx := range d.Spec.Template.Spec.Containers {
			c := &d.Spec.Template.Spec.Containers[idx]
			visitor(c)
		}
	})
}

// VisitMetaObject executes the visitor function on all meta.Object resources.
func (c *Collection) VisitMetaObject(visitor func(metav1.Object)) {
	c.Visit(func(res ctrl.Object) {
		if conv, ok := res.(metav1.Object); ok {
			visitor(conv)
		}
	})
}

// Visit executes the visitor function on all resources.
func (c *Collection) Visit(visitor func(ctrl.Object)) {
	for _, res := range c.items {
		visitor(res)
	}
}

// Remove removes the given element from the collection and returns it.
func (c *Collection) Remove(selector func(ctrl.Object) bool) ctrl.Object {
	for idx, res := range c.items {
		if selector(res) {
			c.items = append(c.items[0:idx], c.items[idx+1:]...)
			return res
		}
	}
	return nil
}
