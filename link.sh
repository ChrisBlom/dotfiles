#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

DOT="${DIR}/dot"

function link-dot-file {
    local F=$1
    echo "Linking dot/$F to ~/.${F}"
    ln -f -s "${DOT}/$F" "${HOME}/.${F}"
}

for i in ${DOT}/* ; do
    link-dot-file $(basename "$i")
done

ln -f -s ${DIR}/com.docker.machine.default.plist ~/Library/LaunchAgents/com.docker.machine.default.plist
