#!/bin/bash

getprop() {
    (
        echo "\"$1 1android.os.SystemProperties :get(java.lang.String)"
        echo 'DISPLAY'
        echo 'EXIT'
    ) | nc -v localhost 9988
}
