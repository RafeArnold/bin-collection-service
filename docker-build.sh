#!/bin/bash

set -eo pipefail

version=$1
bash gradlew clean
bash gradlew build
docker build --build-arg PORT=8080 --build-arg VERSION=$version-SNAPSHOT -t bin-collection-service:$version .
docker save -o bin-collection-service-$version.tar bin-collection-service:$version
tar cvzf bin-collection-service-$version.tar.gz bin-collection-service-$version.tar
rm bin-collection-service-$version.tar
