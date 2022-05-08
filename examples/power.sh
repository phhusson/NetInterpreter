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

rebootDevice() {
    (
        echo 'DUP ~context'
        echo '"power INV :getSystemService(java.lang.String) ~power'
        echo '" =power :reboot(java.lang.String)'
        echo 'EXIT'
    ) | nc -v localhost 9988
}

wakelock() {
    (
        echo 'DUP ~context'
        echo '"power INV :getSystemService(java.lang.String) ~power'
        echo '=power .PARTIAL_WAKE_LOCK ~PARTIAL_WAKE_LOCK'
        echo "\"$1" ' =PARTIAL_WAKE_LOCK =power :newWakeLock(int,java.lang.String)' "~wakelock.$1"
        echo "=wakelock.$1" 'INSPECT :acquire() STACK'
        echo 'EXIT'
    ) | nc -v localhost 9988
}

freewakelock() {
    (
        echo "=wakelock.$1 :release()"
        echo 'EXIT'
    ) | nc -v localhost 9988
}
