#!/bin/bash

string() {
    echo '"""'"$1"
}

connectNewWifi() {
    ssid="$1"
    psk="$2"
    (
        # Store context in var `context`
        echo 'DUP ~context'
        # Grab wifi service and store it in `wifi`
        echo '"wifi INV :getSystemService(java.lang.String) ~wifi'
        # Create a wifi configuration
        echo '1android.net.wifi.WifiConfiguration +() ~wificonfig'
        # Set SSID (must be encoded as "SSID" for Android)
        string "\"$ssid\""
            echo '=wificonfig !SSID'
        # Set PSK (must be encoded as "PSK" for Android)
        string "\"$psk\""
            echo '=wificonfig !preSharedKey'
        # Add network. Network id is returned on stack
        echo '=wificonfig =wifi :addNetwork(android.net.wifi.WifiConfiguration) ~wifiid'
        # Connect to network using the previously returned wifi id
        echo 'NULL =wifiid =wifi :connect(int,android.net.wifi.WifiManager$ActionListener)'
        echo 'STACK'
        #Close connection
        echo 'EXIT'
    ) | nc -v localhost 9988
}
