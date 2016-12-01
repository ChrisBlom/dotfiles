#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

ln -s ${DIR}/config    ${HOME}/.config
ln -s ${DIR}/lein      ${HOME}/.lein/
ln -s ${DIR}/tmux.conf ${HOME}/.tmux.conf
ln -s ${DIR}/git/gitconfig ${HOME}/.gitconfig
ln -s ${DIR}/curlrc ${HOME}/.curlrc
ln -s ${DIR}/csirc ${HOME}/.csirc
ln -s ${DIR}/com.docker.machine.default.plist ~/Library/LaunchAgents/com.docker.machine.default.plist
