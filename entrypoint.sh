#! /bin/bash
cd /tsa-gbif-tool && rm -f /tsa-gbif-tool/target/universal/stage/RUNNING_PID && sbt start -Dsbt.ci=true