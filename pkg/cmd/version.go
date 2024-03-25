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
	"fmt"

	"github.com/citrusframework/yaks/pkg/util/defaults"
	"github.com/spf13/cobra"
)

// VersionVariant may be overridden at build time.
var VersionVariant = ""

func newCmdVersion() *cobra.Command {
	return &cobra.Command{
		Use:   "version",
		Short: "Display version information",
		Run: func(_ *cobra.Command, _ []string) {
			if VersionVariant != "" {
				fmt.Printf("YAKS %s %s\n", VersionVariant, defaults.Version)
			} else {
				fmt.Printf("YAKS %s\n", defaults.Version)
			}
		},
		Annotations: map[string]string{
			offlineCommandLabel: "true",
		},
	}
}
