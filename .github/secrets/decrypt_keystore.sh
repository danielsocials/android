#!/bin/sh

gpg --quiet --batch --yes --decrypt --passphrase="$ENCRYPT_PASS" --output ./release.keystore ./.github/secrets/release.keystore.gpg
