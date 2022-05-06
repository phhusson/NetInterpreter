#!/bin/bash

goToSleep() {
    (
        # Store context in var `context`
        echo 'DUP ~context'
        echo '"power INV :getSystemService(java.lang.String) ~power'
        echo '1android.os.SystemClock :uptimeMillis() STACK =power :goToSleep(long)'

        # Close connection
        echo 'EXIT'
    ) | nc -v localhost 9988
}

# Untested yet since it requires charger disconnected
enablePowerSaveModeEnabled() {
    (
        echo 'DUP ~context'
        echo '"power INV :getSystemService(java.lang.String) ~power'
        echo '"true 1java.lang.Boolean :parseBoolean ~true'
        echo '"false 1java.lang.Boolean :parseBoolean ~false'
        echo '=power =true STACK'
        echo ':setPowerSaveModeEnabled(boolean)'
        echo 'EXIT'
    ) | nc -v localhost 9988
}
