package azotzot.bluetoothmorsechat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.nfc.Tag
import android.os.Bundle
import android.os.Handler
import android.util.Log
import azotzot.bluetoothmorsechat.Constants.Companion.CHANGE_UI
import azotzot.bluetoothmorsechat.Constants.Companion.MESSAGE_READ
import azotzot.bluetoothmorsechat.Constants.Companion.MESSAGE_TOAST
import azotzot.bluetoothmorsechat.Constants.Companion.MESSAGE_WRITE
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


@Suppress("NOT_A_FUNCTION_LABEL_WARNING")
class ChatService(
    private val context: Context,
    private val mainHandler: Handler
) {

    private val TAG = "ChatService"

//    private val APP_UUID = UUID.fromString("b7e7378a-ed1b-11e9-81b4-2a2ae2dbcce4")
    private val APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val NAME_SECURE = "ChatService"

    private lateinit var serverSocket: BluetoothServerSocket
    private lateinit var clientSocket: BluetoothSocket

    private lateinit var bluetoothAdapter: BluetoothAdapter

    private lateinit var mConnectThread: ConnectThread
    private lateinit var mConnectedThread: ConnectedThread
    private lateinit var mSecureAcceptThread: AcceptThread

    val STATE_NONE = 0
    val STATE_LISTEN = 1
    val STATE_CONNECTING = 2
    val STATE_CONNECTED = 3
    private var mState = STATE_NONE
        @Synchronized get

    fun start() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, APP_UUID)

    }

    @Synchronized fun connect(device: BluetoothDevice) {
//        if (this::clientSocket.isInitialized) {
//            if (clientSocket.isConnected) {
//                if (clientSocket.remoteDevice == device) return
//            }
//        }
        mConnectThread = ConnectThread(device)
        mConnectThread.start()
//        mState = STATE_CONNECTING
        setUITitle(device.name)
    }

    fun listenConnect() {
//        mState = STATE_LISTEN
        mSecureAcceptThread = AcceptThread()
        mSecureAcceptThread.start()

    }

    private fun setUITitle(deviceName: String) {
        Log.d(TAG, "Change Main Title to $deviceName")
        mainHandler.obtainMessage(CHANGE_UI,-1,-1, deviceName).sendToTarget()

    }

    @Synchronized private fun connected(socket: BluetoothSocket) {
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread.start()
        mConnectedThread.write("test".toByteArray())
        setUITitle(socket.remoteDevice.name)
    }

    fun write(msg: ByteArray) {
        val r: ConnectedThread
        synchronized(this) {
            if(mState != STATE_CONNECTED) return
            r = mConnectedThread
        }
        if (mConnectedThread.isAlive) {
            mConnectedThread.write(msg)
        }
    }

    private inner class AcceptThread : Thread() {

//        private val TAG = "AcceptThread"

//        private val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
//            bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, APP_UUID)
//        }
//        private val serverSocket: BluetoothServerSocket? = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, APP_UUID)

        override fun run() {
            name="AcceptThread"
//            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, APP_UUID)
            Log.d(TAG, "AcceptThread start: $serverSocket")
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    serverSocket.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    connected(it)
//                    ConnectedThread(it).start()
                    serverSocket.close()
                    shouldLoop = false
                }
            }
            cancel()
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                serverSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {

        private val clientSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(APP_UUID)
        }

        override fun run() {
            name="ConnectThread"
//            clientSocket = device.createRfcommSocketToServiceRecord(APP_UUID)

            bluetoothAdapter.cancelDiscovery()
            Log.d(TAG, "ConnectThread")

            clientSocket.use { socket ->


                socket?.connect()


                socket?.let {
                    ConnectedThread(it).start()
                    Log.d(TAG, "Connect to ${device.name} success")
                }
            }
            cancel()
        }

        fun cancel() {
            Log.d(TAG, "connect thread cancel")
            try {
                clientSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

//        private val TAG = "ConnectedThread"

        private lateinit var mmInStream: InputStream
        private lateinit var mmOutStream: OutputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            name="ConnectedThread"
            mmInStream = mmSocket.inputStream
            mmOutStream = mmSocket.outputStream
            var numBytes: Int // bytes returned from read()
            Log.d(TAG, "Connected thread start")

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                    Log.d(TAG,mmInStream.toString())
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

                // Send the obtained bytes to the UI activity.
                val readMsg = mainHandler.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    mmBuffer)
                readMsg.sendToTarget()
            }
            cancel()
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            Log.d(TAG, "message $bytes")
            try {
                mmOutStream.write(bytes)
                Log.d(TAG, bytes.toString())
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = mainHandler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                mainHandler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = mainHandler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            Log.d(TAG, "connected thread cancel")
            try {
                mmInStream.close()
                mmOutStream.close()
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

}


//
//class BluetoothThread(name: String) : HandlerThread(name) {
//    private lateinit var bluetoothHandler: Handler
//
//    fun prepareHandler() {
//        bluetoothHandler = Handler(looper)
//    }
//
//    fun postTask(task: Runnable) {
//        bluetoothHandler.post(task)
//    }
//
//}

