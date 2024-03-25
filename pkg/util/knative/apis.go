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

package knative

import (
	"k8s.io/apimachinery/pkg/runtime/schema"
)

var (
	// KnownChannelKinds are known channel kinds belonging to Knative.
	KnownChannelKinds = []GroupVersionKindResource{
		{
			GroupVersionKind: schema.GroupVersionKind{
				Kind:    "Channel",
				Group:   "messaging.knative.dev",
				Version: "v1",
			},
			Resource: "channels",
		},
		{
			GroupVersionKind: schema.GroupVersionKind{
				Kind:    "Channel",
				Group:   "messaging.knative.dev",
				Version: "v1beta1",
			},
			Resource: "channels",
		},
		{
			GroupVersionKind: schema.GroupVersionKind{
				Kind:    "InMemoryChannel",
				Group:   "messaging.knative.dev",
				Version: "v1",
			},
			Resource: "inmemorychannels",
		},
		{
			GroupVersionKind: schema.GroupVersionKind{
				Kind:    "InMemoryChannel",
				Group:   "messaging.knative.dev",
				Version: "v1beta1",
			},
			Resource: "inmemorychannels",
		},
	}

	// KnownEndpointKinds are known endpoint kinds belonging to Knative.
	KnownEndpointKinds = []GroupVersionKindResource{
		{
			GroupVersionKind: schema.GroupVersionKind{
				Kind:    "Service",
				Group:   "serving.knative.dev",
				Version: "v1",
			},
			Resource: "services",
		},
		{
			GroupVersionKind: schema.GroupVersionKind{
				Kind:    "Service",
				Group:   "serving.knative.dev",
				Version: "v1beta1",
			},
			Resource: "services",
		},
	}

	// KnownBrokerKinds are known broker kinds belonging to Knative.
	KnownBrokerKinds = []GroupVersionKindResource{
		{
			GroupVersionKind: schema.GroupVersionKind{
				Kind:    "Broker",
				Group:   "eventing.knative.dev",
				Version: "v1",
			},
			Resource: "brokers",
		},
		{
			GroupVersionKind: schema.GroupVersionKind{
				Kind:    "Broker",
				Group:   "eventing.knative.dev",
				Version: "v1beta1",
			},
			Resource: "brokers",
		},
	}

	// RequiredKinds are Knative kinds used by YAKS for testing Knative eventing.
	// They must be present on the cluster.
	RequiredKinds = []GroupVersionKindResource{
		{
			GroupVersionKind: schema.GroupVersionKind{
				Kind:    "Broker",
				Group:   "eventing.knative.dev",
				Version: "v1",
			},
			Resource: "brokers",
		},
	}
)

// GroupVersionKindResource --.
type GroupVersionKindResource struct {
	schema.GroupVersionKind
	Resource string
}

func init() {
	// Channels are also endpoints
	KnownEndpointKinds = append(KnownEndpointKinds, KnownChannelKinds...)
	// Let's add the brokers as last
	KnownEndpointKinds = append(KnownEndpointKinds, KnownBrokerKinds...)
}
