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


## What is the protocol

It's stack based, plus support storing global variables. Commands are split on space except """
- STACK command dumps the content of the stack
- DUP duplicates the top of the stack
- INV inverts the two elements at the top of the stack
- DISPLAY displays the toString of the top of the stack
- INSPECT lists all constructor methods and fields of top of the stack's class
- NULL adds null on stack
- EXIT closes socket
- ~<name> consumes top of stack and put it into <name> variable
- =<name> loads <name> variable at top of stack
- .<name> loads field <name> from top of stack
- :<matcher> assuming there is exactly one matching function, call the function with arguments on the stack
- "xxxx puts the string xxxx on top of the stack
- """ if a line starts with """, everything after """ until new-line is added at the top of stack
- +xx.yy.zz creates an object of type xx.yy.zz with empty parameters constructor
- !<name> will set field <name> of <top of stack> to <second top of stack> and consume both
- 0dyyy add decimal number yyy on top of stack
- 0xyyy add hexadecimal number yyy on top of stack
- 1<name> loads class name

Variables are global across TCP connections.
Stack is reset at every TCP connection.

Check `adb logcat -s NetInterpreter` to understand unexpected behavior (and source code of course).

## Sample usage from computer

Even though the primary target is for GNU/Linux on-smartphone to communicate with Android, samples can be developed from computer for more comfort.

```shell
bash build.sh
adb install -r app.apk && adb shell am start me.phh.netinterpreter/.MainActivity
adb forward tcp:9988 tcp:9988
bash examples/battery.sh
```
