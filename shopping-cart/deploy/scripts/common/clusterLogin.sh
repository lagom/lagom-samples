#!/bin/bash

if [[ "$(basename "${0#-}")" = "$(basename "${BASH_SOURCE[0]}")" ]]; then
  echo "This is a bash source library, not a bash script!" >&2
  exit 1
fi

## TODO: don't use a hardcoded username and pwd ENV_VAR (e.g. CP2_PLAY_PASSWORD)

## 1. CODE_VARIANT
## 2. BUILD_TOOL
clusterLogin() {
    CODE_VARIANT="$1"
    BUILD_TOOL="$2"

    # Load some helping functions
    . $COMMON_SCRIPTS_DIR/setupEnv.sh
    setupEnv "$CODE_VARIANT" "$BUILD_TOOL"

    echo "Attempting login to Openshift cluster (this will fail on PR builds)"
    if [ -z ${CP2_PLAY_PASSWORD+x} ]; then echo "CP2_PLAY_PASSWORD is unset."; else echo "CP2_PLAY_PASSWORD is available."; fi
    oc login https://$OPENSHIFT_SERVER --username=play-team --password=$CP2_PLAY_PASSWORD --namespace=$NAMESPACE  || exit 1
}
