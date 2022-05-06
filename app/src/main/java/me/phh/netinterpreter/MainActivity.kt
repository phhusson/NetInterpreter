package me.phh.netinterpreter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.net.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    val TAG = "NetInterpreter"
    val variablesLock = Any()
    val variables = java.util.HashMap<String, Any?>()

    fun handleClient(socket: Socket) {
        val input = socket.getInputStream().bufferedReader()
        val output = socket.getOutputStream().bufferedWriter()

        var stack = mutableListOf<Any?>(this)
        while(!socket.isClosed) {
            output.flush()
            val line = input.readLine()
            if(line.startsWith("\"\"\"")) {
                val w = line.substring(3)
                Log.d(TAG, "Adding \"$w\"")
                stack.add(w)
                continue
            }
            val words = line.split(" ")
            for(w in words) {
                Log.d(TAG, "Executing $w")
                if(w == "STACK") {
                    output.write("Current stack content:\n")
                    for(v in stack) {
                        output.write(" - ${v.toString()}\n")
                    }
                } else if(w == "DUP") {
                    stack.add(stack.last())
                } else if(w == "INV") {
                    val a = stack.removeLast()
                    val b = stack.removeLast()
                    stack.add(a)
                    stack.add(b)
                } else if(w == "DISPLAY") {
                    output.write(stack.last().toString())
                    output.newLine()
                } else if(w.startsWith("=")) {
                    synchronized(variablesLock) {
                        stack.add(variables[w.substring(1)]!!)
                    }
                } else if(w.startsWith("~")) {
                    synchronized(variablesLock) {
                        variables[w.substring(1)] = stack.removeLast()
                    }
                } else if(w == "INSPECT") {
                    val v = stack.last()
                    val c = v!!.javaClass
                    output.write("Type is $c\n")
                    output.write("Constructors:\n")
                    for (constructor in c.constructors) {
                        output.write(" - ${constructor.toString()}\n")
                    }
                    output.write("Methods:\n")
                    for (method in c.methods) {
                        output.write(" - ${method.toString()}\n")
                    }
                    output.write("Fields:\n")
                    for (field in c.fields) {
                        output.write(" - ${field.toString()}\n")
                    }
                } else if(w == "NULL") {
                    stack.add(null)
                } else if(w.startsWith(".")) {
                    val v = stack.removeLast()
                    val fieldName = w.substring(1)
                    val field = v!!.javaClass.getField(fieldName)
                    field.isAccessible = true
                    stack.add(field.get(v)!!)
                } else if(w.startsWith("\"")) {
                    val str = w.substring(1)
                    stack.add(str)
                } else if(w.startsWith(":")) {
                    val v = stack.removeLast()
                    val methodName = w.substring(1)
                    Log.d(TAG, "Executing function on top of $v")
                    val obj = if(v is Class<*>) null else v
                    val cl = if(v is Class<*>) v else v!!.javaClass
                    val methods = cl.methods.filter { it.toString().contains(methodName) }
                    if(methods.size != 1) {
                        Log.d(TAG, "Non-unique matching methods")
                        for(m in methods) {
                            Log.d(TAG, " - ${m.toString()}")
                        }
                        throw Exception("Non-unique matching methods")
                    }
                    val method = methods[0]
                    val parameters = Array<Any?>(method.parameterCount, { null })
                    for(i in 0 until method.parameterCount) {
                        parameters[i] = stack.removeLast()
                    }
                    val res = method.invoke(obj, *parameters)
                    stack.add(res)
                } else if(w.startsWith("+")) {
                    val v = w.substring(1)
                    val cl = Class.forName(v)
                    val constr = cl.getConstructor()
                    constr.isAccessible = true
                    stack.add(constr.newInstance())
                } else if(w.startsWith("!")) {
                    val fieldName = w.substring(1)
                    val obj = stack.removeLast()!!
                    val value = stack.removeLast()
                    val field = obj.javaClass.getField(fieldName)
                    field.isAccessible = true
                    field.set(obj, value)
                } else if(w.startsWith("0")) {
                    val v = w.substring(2)
                    val base = if(w[1] == 'd') 10 else 16
                    stack.add(Integer.parseInt(v, base))
                } else if(w.startsWith("1")) {
                    val v = w.substring(1)
                    stack.add(Class.forName(v))
                }
            }
        }
    }

    fun server() {
        val serverSocket = ServerSocket(9988, 20, Inet6Address.getLocalHost())

        while(!serverSocket.isClosed) {
            val client = serverSocket.accept()
            thread {
                try {
                    handleClient(client)
                } catch(t: Throwable) {
                    Log.d("NetInterpreter", "Client made us crash", t)
                    client.close()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        thread {
            server()
        }
    }
}