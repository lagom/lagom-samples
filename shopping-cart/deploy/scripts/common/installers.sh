#!/bin/bash

if [[ "$(basename "${0#-}")" = "$(basename "${BASH_SOURCE[0]}")" ]]; then
  echo "This is a bash source library, not a bash script!" >&2
  exit 1
fi

installOC() {
    if [ -z "$(command -v oc)" -a $TRAVIS ]
    then
        (
            mkdir tmp
            cd tmp || {
                echo "Failed to cd into tmp"
                exit 1
            }
            echo "Installing oc 3.11"
            curl -OL https://github.com/openshift/origin/releases/download/v3.11.0/openshift-origin-client-tools-v3.11.0-0cbc58b-linux-64bit.tar.gz
            echo "4b0f07428ba854174c58d2e38287e5402964c9a9355f6c359d1242efd0990da3  openshift-origin-client-tools-v3.11.0-0cbc58b-linux-64bit.tar.gz" | sha256sum -c
            tar --strip 1 -xvzf openshift-origin-client-tools-v3.11.0-0cbc58b-linux-64bit.tar.gz && chmod +x oc && sudo cp oc /usr/local/bin/ && rm oc
        )
        rm -rf tmp/
    else
        echo "! oc already installed."
    fi
    oc version && echo "oc CLI installed successfully"
}

installKustomize() {
    if [ -z "$(command -v kustomize)" -a $TRAVIS ]
    then
        (
            mkdir tmp
            cd tmp || {
                echo "Failed to cd into tmp"
                exit 1
            }
            echo "Installing kustomize 2.0.3"
            curl -OL https://github.com/kubernetes-sigs/kustomize/releases/download/v2.0.3/kustomize_2.0.3_linux_amd64
            echo "a04d79a013827c9ebb0abfe9d41cbcedf507a0310386c8d9a7efec7a36f9d7a3  kustomize_2.0.3_linux_amd64" | sha256sum -c
            chmod +x kustomize_2.0.3_linux_amd64 && sudo cp kustomize_2.0.3_linux_amd64 /usr/local/bin/kustomize && rm kustomize_2.0.3_linux_amd64
        )
        rm -rf tmp/
    else
        echo "! kustomize already installed."
    fi
    kustomize version && echo "kustomize CLI installed successfully"
}
