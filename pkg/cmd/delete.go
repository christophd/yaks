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

package cmd

import (
	"context"
	"errors"
	"fmt"
	"strconv"

	"github.com/citrusframework/yaks/pkg/apis/yaks/v1alpha1"
	"github.com/citrusframework/yaks/pkg/util/kubernetes"
	"github.com/spf13/cobra"
	k8errors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	k8sclient "sigs.k8s.io/controller-runtime/pkg/client"
)

// newCmdDelete --.
func newCmdDelete(rootCmdOptions *RootCmdOptions) (*cobra.Command, *deleteCmdOptions) {
	options := deleteCmdOptions{
		RootCmdOptions: rootCmdOptions,
	}
	cmd := cobra.Command{
		Use:     "delete [test1] [test2] ...",
		Short:   "Delete tests",
		Long:    "Delete tests by name from given namespace.",
		PreRunE: decode(&options),
		RunE: func(command *cobra.Command, args []string) error {
			if err := options.validateArgs(command, args); err != nil {
				return err
			}
			if err := options.run(args); err != nil {
				fmt.Println(err.Error())
			}

			return nil
		},
	}

	cmd.Flags().BoolP("all", "a", false, "Delete all tests")

	return &cmd, &options
}

type deleteCmdOptions struct {
	*RootCmdOptions
	DeleteAll bool `mapstructure:"all"`
}

func (o *deleteCmdOptions) validateArgs(_ *cobra.Command, args []string) error {
	if o.DeleteAll && len(args) > 0 {
		return errors.New("invalid combination: both all flag and named tests are set")
	}

	if !o.DeleteAll && len(args) == 0 {
		return errors.New("invalid combination: neither all flag nor named tests are set")
	}

	return nil
}

func (o *deleteCmdOptions) run(args []string) error {
	c, err := o.GetCmdClient()
	if err != nil {
		return err
	}

	namespace := o.Namespace

	if len(args) != 0 && !o.DeleteAll {
		for _, arg := range args {
			name := kubernetes.SanitizeName(arg)

			if err := deleteTest(o.Context, c, namespace, name); err != nil {
				return err
			}
		}
	} else if o.DeleteAll {
		if err := deleteAllTests(o.Context, c, namespace, o.Verbose); err != nil {
			return err
		}
	}

	return nil
}

func deleteTest(ctx context.Context, c k8sclient.Client, namespace string, name string) error {
	test := v1alpha1.Test{
		TypeMeta: metav1.TypeMeta{
			APIVersion: v1alpha1.SchemeGroupVersion.String(),
			Kind:       v1alpha1.TestKind,
		},
		ObjectMeta: metav1.ObjectMeta{
			Namespace: namespace,
			Name:      name,
		},
	}
	err := c.Delete(ctx, &test)
	if err != nil {
		if k8errors.IsNotFound(err) {
			fmt.Println("Test " + name + " not found. Skipped.")
		} else {
			return err
		}
	} else {
		fmt.Println("Test " + name + " deleted")
	}

	return nil
}

func deleteAllTests(ctx context.Context, c k8sclient.Client, namespace string, verbose bool) error {
	testList := v1alpha1.TestList{
		TypeMeta: metav1.TypeMeta{
			APIVersion: v1alpha1.SchemeGroupVersion.String(),
			Kind:       v1alpha1.TestKind,
		},
	}

	// Looks like Operator SDK doesn't support deletion of all objects with one command
	err := c.List(ctx, &testList, k8sclient.InNamespace(namespace))
	if err != nil {
		return err
	}
	for _, test := range testList.Items {
		test := test // pin
		err := c.Delete(ctx, &test)
		if err != nil {
			return err
		}

		if verbose {
			fmt.Println("Test " + test.Name + " deleted")
		}
	}
	if len(testList.Items) == 0 {
		fmt.Printf("No tests found in namespace '%s'\n", namespace)
	} else {
		fmt.Println(strconv.Itoa(len(testList.Items)) + " test(s) deleted")
	}

	return nil
}
