#!/bin/bash

# Convert Vue development certificates to a Java Keystore
# Assumes you have openssl and keytool installed

# Paths to your certificates
CERT_PATH="./src/main/vue-project/certs/cert.pem"
KEY_PATH="./src/main/vue-project/certs/dev.pem"
KEYSTORE_PATH="./keystore.jks"

# Create a PKCS12 file from the certificate and key
openssl pkcs12 -export -in "$CERT_PATH" -inkey "$KEY_PATH" -out "./keystore.p12" -name "sampleAlias" -password pass:foobar

# Import the PKCS12 file into a Java Keystore
keytool -importkeystore -destkeystore "$KEYSTORE_PATH" -srckeystore "./keystore.p12" -srcstoretype PKCS12 -srcstorepass foobar -deststorepass foobar

# Clean up
rm "./keystore.p12"
