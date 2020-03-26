#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

if [ $# -ne 1 ]; then
    echo "Error invoking embed_resources.sh: directory argument required"
    exit 1
fi

location=$(dirname $0)
destdir=$location/../$1
destfile=$location/../$1/resources.go

cat > $destfile << EOM
/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

// Code generated by hack/embed_resources.sh. DO NOT EDIT.

package deploy

var Resources map[string]string

func init() {
	Resources = make(map[string]string)

EOM

for f in $(ls $destdir | grep ".yaml" | sort); do
	printf "\tResources[\"$f\"] =\n\t\t\`\n" >> $destfile
	cat $destdir/$f >> $destfile
	printf "\n\`\n" >> $destfile
done

for f in $(ls $destdir/crds | grep ".yaml" | sort); do
	printf "\tResources[\"crds/$f\"] =\n\t\t\`\n" >> $destfile
	cat $destdir/crds/$f >> $destfile
	printf "\n\`\n" >> $destfile
done

printf "\n}\n" >> $destfile