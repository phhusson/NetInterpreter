#!/bin/bash

# This script asks Android for current battery capacity and displays it on stdout
batteryProperty() {
    propName="$1"
    propType="$2"
    (
        # Store context in var `context`
        echo 'DUP ~context'
        # Grab battery service and store it in `battery`
        echo '"batterymanager INV :getSystemService(java.lang.String) ~battery'
        # Grab property id
        echo '=battery .'$propName
        # Get property value capacity
        echo '=battery :get'$propType'Property'
        echo 'DISPLAY'

        # Close connection
        echo 'EXIT'
    ) | nc -v localhost 9988
}

batteryLevel() {
    batteryProperty BATTERY_PROPERTY_CAPACITY Int
}

batteryCurrentNow() {
    batteryProperty BATTERY_PROPERTY_CURRENT_NOW Int
}

batteryStatus() {
    batteryProperty BATTERY_PROPERTY_STATUS Int
}
