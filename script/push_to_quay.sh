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

if [ $# -ne 1 ]; then
    echo "Error invoking push_to_quay.sh: version required"
    exit 1
fi
if [ -z "$QUAY_USERNAME" ]; then
	echo "Environment variable QUAY_USERNAME missing"
	exit 2
fi
if [ -z "$QUAY_PASSWORD" ]; then
	echo "Environment variable QUAY_PASSWORD missing"
	exit 3
fi

VERSION=$1

PACKAGE=yaks
ORGANIZATION=citrusframework

location=$(dirname $0)
rootdir=$(realpath ${location}/..)

export AUTH_TOKEN=$(curl -sH "Content-Type: application/json" -XPOST https://quay.io/cnr/api/v1/users/login -d '{"user": {"username": "'"${QUAY_USERNAME}"'", "password": "'"${QUAY_PASSWORD}"'"}}' | jq -r '.token')

operator-courier --verbose push $rootdir/deploy/olm-catalog/${PACKAGE}/ ${ORGANIZATION} ${PACKAGE} ${VERSION} "$AUTH_TOKEN"
