#!/bin/bash

java -jar /app/frontend-server.jar &
java -jar /app/gateway.jar &
java -jar /app/app.jar &

wait -n
exit $?
