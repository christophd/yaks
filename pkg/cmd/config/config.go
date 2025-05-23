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

package config

const (
	DefaultTimeout  = "30m"
	DefaultAppLabel = "app=yaks"
)

var DefaultVersions = Versions{
	"citrus.version":           "4.6.0",
	"camel.version":            "4.11.0",
	"apache.camel.version":     "4.11.0",
	"spring.version":           "6.2.6",
	"cucumber.version":         "7.22.1",
	"postgresql.version":       "42.7.5",
	"testcontainers.version":   "1.21.0",
	"aws-java-sdk2.version":    "2.31.36",
	"activemq.version":         "6.1.6",
	"activemq.artemis.version": "2.41.0",
}

type RunConfig struct {
	BaseDir string       `yaml:"baseDir"`
	Config  Config       `yaml:"config"`
	Pre     []StepConfig `yaml:"pre"`
	Post    []StepConfig `yaml:"post"`
}

type Config struct {
	Recursive bool            `yaml:"recursive"`
	Timeout   string          `yaml:"timeout"`
	Namespace NamespaceConfig `yaml:"namespace"`
	Operator  OperatorConfig  `yaml:"operator"`
	Runtime   RuntimeConfig   `yaml:"runtime"`
	Dump      DumpConfig      `yaml:"dump"`
}

type StepConfig struct {
	Run     string `yaml:"run"`
	Script  string `yaml:"script"`
	Name    string `yaml:"name"`
	Timeout string `yaml:"timeout"`
	If      string `yaml:"if"`
}

type RuntimeConfig struct {
	Cucumber       CucumberConfig       `yaml:"cucumber"`
	Selenium       SeleniumConfig       `yaml:"selenium"`
	TestContainers TestContainersConfig `yaml:"testcontainers"`
	Resources      []string             `yaml:"resources"`
	Settings       SettingsConfig       `yaml:"settings"`
	Env            []EnvConfig          `yaml:"env"`
	Secrets        []string             `yaml:"secrets"`
	ClusterType    string               `yaml:"clusterType"`
}

type CucumberConfig struct {
	Tags    []string `yaml:"tags"`
	Glue    []string `yaml:"glue"`
	Options string   `yaml:"options"`
}

type SeleniumConfig struct {
	Enabled   bool        `yaml:"enabled"`
	Image     string      `yaml:"image"`
	NoVNC     bool        `yaml:"noVNC"`
	RunAsUser int         `yaml:"runAsUser"`
	Env       []EnvConfig `yaml:"env"`
}

type TestContainersConfig struct {
	Enabled   bool   `yaml:"enabled"`
	Image     string `yaml:"image"`
	RunAsUser int    `yaml:"runAsUser"`
}

type EnvConfig struct {
	Name  string `yaml:"name"`
	Value string `yaml:"value"`
}

type SettingsConfig struct {
	Repositories       []RepositoryConfig `yaml:"repositories"`
	PluginRepositories []RepositoryConfig `yaml:"pluginRepositories"`
	Dependencies       []DependencyConfig `yaml:"dependencies"`
	Loggers            []LoggerConfig     `yaml:"loggers"`
}

type RepositoryConfig struct {
	ID        string       `yaml:"id"`
	Name      string       `yaml:"name,omitempty"`
	URL       string       `yaml:"url"`
	Layout    string       `yaml:"layout,omitempty"`
	Releases  PolicyConfig `yaml:"releases,omitempty"`
	Snapshots PolicyConfig `yaml:"snapshots,omitempty"`
}

type PolicyConfig struct {
	Enabled      string `yaml:"enabled,omitempty"`
	UpdatePolicy string `yaml:"updatePolicy,omitempty"`
}

type DependencyConfig struct {
	GroupID    string `yaml:"groupId"`
	ArtifactID string `yaml:"artifactId"`
	Version    string `yaml:"version"`
}

type LoggerConfig struct {
	Name  string `yaml:"name"`
	Level string `yaml:"level"`
}

type NamespaceConfig struct {
	Name       string `yaml:"name"`
	Temporary  bool   `yaml:"temporary"`
	AutoRemove bool   `yaml:"autoRemove"`
}

type OperatorConfig struct {
	Namespace string   `yaml:"namespace"`
	Roles     []string `yaml:"roles"`
}

type DumpConfig struct {
	Enabled    bool     `yaml:"enabled"`
	FailedOnly bool     `yaml:"failedOnly"`
	Append     bool     `yaml:"append"`
	Directory  string   `yaml:"directory"`
	File       string   `yaml:"file"`
	Lines      int      `yaml:"lines"`
	Includes   []string `yaml:"includes"`
}

type Versions map[string]string
