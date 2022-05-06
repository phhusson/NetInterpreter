## What is this?

This is an Android application that will execute commands received over TCP port 9988, which includes automatic introspection to call any Anddroid java API over telnet. Also, it includes exploration tooling, and a nano-language to be able to do small stuff.

## Sounds gibberish. What can this be used f or?

The goal is to be able to do simple Android things from "Linux" environments. This application is launched Android-side, and then everything can be scripted Linux-side. The Android APIs are non-trivial, so several examples are provided.

## What can we do with this?

Since all Android API are accessible it can do anything, but here are some examples that you can do from the comfort of a shell or a python script:
- Install Android application
- Setup WiFi SSID/PSK
- Take a picture on the camera
- Put phone in airplane mode
- Launch apps
- Play music file

There are some limitations though, because the protocol is text-based. For instance, you can't transmit audio over this socket.
