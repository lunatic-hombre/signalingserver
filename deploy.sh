#!/bin/bash
server=test.bardly.io
[[ -f ss.jar ]] || ./build.sh
rsync -rv --delete lib root@${server}:~/cm9k
rsync -rv --delete resources root@${server}:~/cm9k
scp ss.jar root@${server}:~/cm9k
