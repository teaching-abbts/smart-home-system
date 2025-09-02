#!/bin/bash

# Detect operating system
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || "$OSTYPE" == "win32" ]]; then
  # Windows
  echo "Installing CA certificate on Windows..."
  certutil -addstore -f "ROOT" src\\main\\vue-project\\certs\\rootCA.pem
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
  # Linux
  echo "Installing CA certificate on Linux..."
  sudo cp ./src/main/vue-project/certs/rootCA.crt /usr/local/share/ca-certificates/
  sudo update-ca-certificates
else
  echo "Unsupported operating system: $OSTYPE"
  exit 1
fi

echo "CA certificate installation completed."
