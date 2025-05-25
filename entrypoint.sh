#!/bin/bash

if [ -f "/certs/default_authkit_private_key.pem" ]; then
    echo "default cert file found, will not generate certificate."
else
    echo "default cert file is not found, will generate default certificates."
    openssl genrsa -out default_authkit_private_key.pem 4096
    openssl rsa -pubout -in default_authkit_private_key.pem -out default_authkit_public_key.pem
    openssl pkcs8 -topk8 -in default_authkit_private_key.pem -inform pem -out default_authkit_private_key_pkcs8.pem -outform pem -nocrypt
fi

java -jar /app/frontend-server.jar &
java -jar /app/gateway.jar &
java -jar /app/app.jar &

wait -n
exit $?
