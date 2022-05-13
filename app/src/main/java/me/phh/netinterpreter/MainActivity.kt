package me.phh.netinterpreter

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.dx.stock.ProxyBuilder
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.net.Inet6Address
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executor
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    val TAG = "NetInterpreter"
    val variablesLock = Any()
    val variables = java.util.HashMap<String, Any?>()
    val doneCallbacksLock = Object()
    val doneCallbacks = HashMap<String, Array<Any?>>()

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
                if(w == "EXIT") {
                    input.close()
                    output.close()
                    socket.close()
                    break
                } else if(w == "STACK") {
                    output.write("Current stack content:\n")
                    for (v in stack) {
                        output.write(" - ${v.toString()}\n")
                    }
                } else if(w == "NEW_BYTE_ARRAY") {
                    val size = stack.removeLast() as Integer
                    stack.add(ByteArray(size.toInt()))
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
                    val c = if(v is Class<*>) v else v!!.javaClass
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
                } else if(w == "DROP") {
                    stack.removeLast()
                } else if(w.startsWith(".")) {
                    val v = stack.removeLast()
                    val fieldName = w.substring(1)
                    val field = if(v is Class<*>) v.getField(fieldName) else v!!.javaClass.getField(fieldName)
                    field.isAccessible = true
                    if(v is Class<*>) {
                        stack.add(field.get(null))
                    } else {
                        stack.add(field.get(v))
                    }
                } else if(w.startsWith("\"")) {
                    val str = w.substring(1)
                    stack.add(str)
                } else if(w.startsWith(":")) {
                    val v = stack.removeLast()
                    val ui = w[1] == '1'
                    val methodName = if(ui) w.substring(2) else w.substring(1)
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
                    if(ui) {
                        runOnUiThread {
                            method.invoke(obj, *parameters)
                        }
                    } else {
                        val res = method.invoke(obj, *parameters)
                        stack.add(res)
                    }
                } else if(w.startsWith("+")) {
                    val v = w.substring(1)
                    val cl = stack.removeLast() as Class<*>
                    val constructors = cl.constructors.filter { it.toString().contains(v) }
                    if(constructors.size != 1) {
                        Log.d(TAG, "Non-unique matching constructors")
                        for(m in constructors) {
                            Log.d(TAG, " - ${m.toString()}")
                        }
                        throw Exception("Non-unique matching constructors")
                    }
                    val constr = constructors[0]
                    constr.isAccessible = true
                    val parameters = Array<Any?>(constr.parameterCount, { null })
                    for(i in 0 until constr.parameterCount) {
                        parameters[i] = stack.removeLast()
                    }
                    stack.add(constr.newInstance(*parameters))
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
                } else if(w.startsWith("[")) {
                    val off = Integer.parseInt(w.substring(1))
                    val o = stack.removeLast()
                    if(o is LongArray) {
                        stack.add(o[off])
                    } else if(o is IntArray) {
                        stack.add(o[off])
                    } else if(o is FloatArray) {
                        stack.add(o[off])
                    } else if(o is Array<*>) {
                        stack.add(o[off])
                    } else {
                        throw Exception("Unknown type of array $o")
                    }
                } else if(w.startsWith("90")) {
                    val className = w.substring(2)
                    val cl = Class.forName(className)
                    val handler = object: InvocationHandler {
                        override fun invoke(proxy: Any, method: Method, args: Array<Any?>): Any? {
                            synchronized(doneCallbacksLock) {
                                val key = className + "." + method.name
                                Log.d(TAG, "Got key \"$key\"")
                                doneCallbacks[key] = args
                                doneCallbacksLock.notifyAll()
                            }
                            try {
                                val result = ProxyBuilder.callSuper(proxy, method, *args)
                                Log.d(
                                    TAG, "Method: " + method.getName() + " args: "
                                            + args.toList() + " result: " + result
                                );
                                return result
                            } catch(t: java.lang.AbstractMethodError) {
                                Log.d(TAG, "Method: ${method.name} args ${args.toList()}")
                                Log.d(TAG, "Couldn't call super, returning null")
                                return null
                            }
                        }

                    }

                    stack.add(
                        ProxyBuilder
                            .forClass(cl)
                            .dexCache(getDir("dx", MODE_PRIVATE))
                            .handler(handler)
                            .build())
                } else if(w.startsWith("91")) {
                    val v = w.substring(2)
                    synchronized(doneCallbacksLock) {
                        doneCallbacks.remove(v)
                    }
                } else if(w.startsWith("92")) {
                    val v = w.substring(2)
                    val result = synchronized(doneCallbacksLock) {
                        while(doneCallbacks.getOrDefault(v, null) == null) {
                            doneCallbacksLock.wait()
                        }
                        doneCallbacks[v]
                    }
                    stack.add(result)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun initVariables() {
        variables["context"] = this
        variables["handler"] = Handler(HandlerThread("HandlerThread").also {it.start() }.looper)
        val androidHandler = variables["handler"] as Handler
        variables["executor"]  = Executor { p0 -> androidHandler.post(p0) }
    }

    fun server() {
        initVariables()
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

        thread {
            server()
        }
    }
}