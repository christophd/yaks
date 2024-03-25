#!/bin/bash

#
# Copyright the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Exit if any error occurs
# Fail on a single failed command in a pipeline (if supported)
set -o pipefail

# Save global script args, use "help" as default
if [ -z "$1" ]; then
    ARGS=("help")
else
    ARGS=("$@")
fi

# Fail on error and undefined vars (please don't use global vars, but evaluation of functions for return values)
set -eu

location=$(dirname $0)
working_dir=$(realpath ${location}/../)

source "$location/util/common_funcs"
source "$location/util/build_funcs"
source "$location/util/version_funcs"

release_version=$(get_release_version "$working_dir/java")
check_error $release_version

# Calculate common maven options
maven_opts="$(extract_maven_opts)"

cd $working_dir

build_artifacts "$working_dir" "$release_version" "$maven_opts"
