#!/bin/bash

set -e

if [ -d /usr/lib/jvm/java-8-openjdk-amd64/bin/ ];then
export PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin/:$PATH
fi

if [ -z "$ANDROID_HOME" ];then
    export ANDROID_HOME=$PWD/sdk
fi

./gradlew assembleDebug
LD_LIBRARY_PATH=./signapk/ java -jar signapk/signapk.jar keys/platform.x509.pem keys/platform.pk8 ./app/build/outputs/apk/debug/app-debug.apk app.apk
