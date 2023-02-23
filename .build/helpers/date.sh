#!/bin/bash

get_current_date() {
	echo $(date +%Y%m%d)
}

get_modified_date() {
	if [ -f .modified ]
	then
		echo $(date --file=.modified +%Y%m%d)
	else
		echo "00000000"
	fi
}

write_modified() {
	date -Iseconds >.modified
}