#!/usr/bin/env bash

if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_63c142cb3b3e_key -iv $encrypted_63c142cb3b3e_iv -in codesigning.asc.enc -out codesigning.asc -d
    gpg --fast-import ci/codesigning.asc
    docker login -u $DOCKER_USERNAME -p $DOCKER_PASSPHRASE ;
fi