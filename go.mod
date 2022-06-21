module github.com/citrusframework/yaks

go 1.16

require (
	github.com/Masterminds/semver v1.5.0
	github.com/container-tools/snap v0.0.8
	github.com/gertd/go-pluralize v0.2.0
	github.com/go-logr/logr v1.2.2
	github.com/google/uuid v1.3.0
	github.com/mitchellh/go-homedir v1.1.0
	github.com/mitchellh/mapstructure v1.5.0
	github.com/openshift/api v3.9.1-0.20190927182313-d4a64ec2cbd8+incompatible
	github.com/operator-framework/api v0.13.0
	github.com/pkg/errors v0.9.1
	github.com/prometheus-operator/prometheus-operator/pkg/apis/monitoring v0.50.0
	github.com/rs/xid v1.4.0
	github.com/shurcooL/httpfs v0.0.0-20190707220628-8d4bc4ba7749
	github.com/shurcooL/vfsgen v0.0.0-20181202132449-6a9ea43bcacd
	github.com/sirupsen/logrus v1.8.1
	github.com/spf13/cobra v1.4.0
	github.com/spf13/pflag v1.0.5
	github.com/spf13/viper v1.12.0
	github.com/stoewer/go-strcase v1.2.0
	github.com/stretchr/testify v1.7.2
	go.uber.org/multierr v1.6.0
	gopkg.in/yaml.v2 v2.4.0
	gotest.tools/v3 v3.1.0
	k8s.io/api v0.22.5
	k8s.io/apiextensions-apiserver v0.22.5
	k8s.io/apimachinery v0.22.5
	k8s.io/client-go v0.22.5
	k8s.io/code-generator v0.22.5 // indirect
	k8s.io/klog/v2 v2.40.1
	k8s.io/kubectl v0.22.5
	knative.dev/eventing v0.29.0
	sigs.k8s.io/controller-runtime v0.10.3
)

replace (
	github.com/docker/docker => github.com/moby/moby v0.7.3-0.20190826074503-38ab9da00309 // Required by Helm
	github.com/go-logr/logr => github.com/go-logr/logr v0.4.0
	k8s.io/klog/v2 => k8s.io/klog/v2 v2.9.0
)
