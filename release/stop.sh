#!/bin/sh
ps -ef | grep "db-pitcher-custom-1.0.0.jar" | grep -v grep | awk '{print $2}' | xargs kill
