#!/bin/bash
[[ -f ss.jar ]] || ./build.sh
echo "Running java -jar ss.jar $*"
java -jar ss.jar $*
