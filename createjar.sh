#!/usr/bin/env bash
cd text-ui-test
./runtest.sh
cd ..
cd bin
jar cfe Duke.jar duke.Duke duke/*.class duke/command/*.class duke/task/*.class duke/parser/*.class