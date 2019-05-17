#!/bin/bash

## TODO: don't use a hardcoded username and pwd ENV_VAR (e.g. CP2_PLAY_PASSWORD)

# Recognize the environment
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# Load some helping functions
. $COMMON_SCRIPTS_DIR/setupEnv.sh

echo "Attempting login to Openshift cluster (this will fail on PR builds)"
if [ -z ${CP2_PLAY_PASSWORD+x} ]; then echo "CP2_PLAY_PASSWORD is unset."; else echo "CP2_PLAY_PASSWORD is available."; fi
oc login https://$OPENSHIFT_SERVER --username=play-team --password=$CP2_PLAY_PASSWORD  || exit 1
