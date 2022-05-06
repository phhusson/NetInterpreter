#!/bin/bash

goToSleep() {
    (
        # Store context in var `context`
        echo 'DUP ~context'
        # Grab battery service and store it in `battery`
        echo '"power INV :getSystemService(java.lang.String) ~power'
        echo '1android.os.SystemClock :uptimeMillis() STACK =power :goToSleep(long)'

        # Close connection
        echo ':dewodoeiwoijdeowiijdoewi'
    ) | nc -v localhost 9988
}

goToSleep
