#!/bin/bash
script="${BASH_SOURCE##*/}"
cmd="${script%.*}"

if ! type -p ${cmd} >/dev/null; then
	echo ${cmd} not found
	exit
fi
