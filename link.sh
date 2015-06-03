#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

ln -s ${DIR}/config    ${HOME}/.config
ln -s ${DIR}/lein      ${HOME}/.lein/
ln -s ${DIR}/tmux.conf ${HOME}/.tmux.conf
ln -s ${DIR}/gitconfig ${HOME}/.gitconfig
