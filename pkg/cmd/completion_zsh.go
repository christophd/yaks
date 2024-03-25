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

	"github.com/spf13/cobra"
)

// ******************************
//
//
//
// ******************************

const zshCompletionCmdLongDescription = `
To configure your zsh shell to load completions for each session add to your zshrc

if [ $commands[yaks] ]; then
  source <(yaks completion zsh)
fi
`

// ******************************
//
// COMMAND
//
// ******************************

func newCmdCompletionZsh(root *cobra.Command) *cobra.Command {
	return &cobra.Command{
		Use:   "zsh",
		Short: "Generates zsh completion scripts",
		Long:  zshCompletionCmdLongDescription,
		Run: func(_ *cobra.Command, _ []string) {
			err := root.GenZshCompletion(root.OutOrStdout())
			if err != nil {
				fmt.Print(err.Error())
			}
		},
		Annotations: map[string]string{
			offlineCommandLabel: "true",
		},
	}
}

func configureKnownZshCompletions(command *cobra.Command) {
}
