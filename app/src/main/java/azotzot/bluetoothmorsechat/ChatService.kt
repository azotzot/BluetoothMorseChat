package azotzot.bluetoothmorsechat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import azotzot.bluetoothmorsechat.Constants.Companion.CHANGE_TITLE
import azotzot.bluetoothmorsechat.Constants.Companion.CHANGE_UI
import azotzot.bluetoothmorsechat.Constants.Companion.CLOSE_DIALOG_FRAGMENT
import azotzot.bluetoothmorsechat.Constants.Companion.CONNECT_FAIL
import azotzot.bluetoothmorsechat.Constants.Companion.MESSAGE_READ
import azotzot.bluetoothmorsechat.Constants.Companion.MESSAGE_TOAST
import azotzot.bluetoothmorsechat.Constants.Companion.MESSAGE_WRITE
import azotzot.bluetoothmorsechat.Constants.Companion.SEND_FAILED
import azotzot.bluetoothmorsechat.Constants.Companion.SEND_MESSAGE
import azotzot.bluetoothmorsechat.Constants.Companion.SEND_SUCCESS
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class ChatService(private val context: Context,
                  private val mainHandler: Handler,
                  private val bluetoothAdapter: BluetoothAdapter) {

    private val TAG = "ChatService"

    private val APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val NAME_SECURE = "ChatService"

    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mSecureAcceptThread: AcceptThread? = null

    val STATE_NONE = 0
    val STATE_LISTEN = 1
    val STATE_CONNECTING = 2
    val STATE_CONNECTED = 3

    private var mState = STATE_NONE
        @Synchronized get

    fun start() {
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }
//  Попытка подключения к удаленному устройству
    @Synchronized fun connect(device: BluetoothDevice) {
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        mConnectThread = ConnectThread(device)
        mConnectThread!!.start()
        mainHandler.obtainMessage(CHANGE_UI, CLOSE_DIALOG_FRAGMENT,-1).sendToTarget()
        mState = STATE_CONNECTING

    }
//  Активация ожидания подключения
    @Synchronized fun listenConnect() {
//        mState = STATE_LISTEN
//        if(serverSocket !=  null) {
//            serverSocket?.close()
//            serverSocket = null
//        }
        mSecureAcceptThread = AcceptThread()
        mSecureAcceptThread!!.start()
        mState = STATE_CONNECTING

    }
//  Смена названия в action bar на имя подключенного устройства
    private fun setUITitle(deviceName: String) {
        Log.d(TAG, "Change Main Title to $deviceName")
        mainHandler.obtainMessage(CHANGE_UI,CHANGE_TITLE,-1, deviceName).sendToTarget()

    }
//  Включение менеджера сеанса подключения
    @Synchronized private fun connected(socket: BluetoothSocket) {
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        mConnectedThread = ConnectedThread(socket)
        mConnectedThread!!.start()
        mState = STATE_CONNECTED
        setUITitle(socket.remoteDevice.name)

    }
//  Отправка сообщения
    fun write(msg: ByteArray) {
        val r: ConnectedThread?
        synchronized(this) {
            if(mState != STATE_CONNECTED) {
                mainHandler.obtainMessage(MESSAGE_TOAST, SEND_FAILED).sendToTarget()
                return
            }
            r = this.mConnectedThread!!
        }
        if (r != null) {
            if (r.isAlive) {
                r.write(msg)
                mainHandler.obtainMessage(MESSAGE_TOAST, SEND_SUCCESS).sendToTarget()
                return
            }
        }
        mainHandler.obtainMessage(MESSAGE_TOAST, SEND_FAILED).sendToTarget()
    }
//  Поток ожидания подключения
    private inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, APP_UUID)
        }

        override fun run() {
            name="AcceptThread"
            Log.d(TAG, "AcceptThread start: $serverSocket")

            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    serverSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    connected(it)
                    shouldLoop = false
                }
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
//  Поток попытки подключения
    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {

        private val clientSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(APP_UUID)
        }
//        private lateinit var clientSocket: BluetoothSocket

        override fun run() {
            name="ConnectThread"
            Log.d(TAG, "ConnectThread")
            bluetoothAdapter.cancelDiscovery()
//            clientSocket = device.createRfcommSocketToServiceRecord(APP_UUID)

            with(clientSocket) {

                try {
                    this?.connect()
                    this?.let { connected(it) }
                    Log.d(TAG, "Connect to ${device.name} success")
                } catch (e: IOException) {
                    Log.e(TAG, "unable to close() socket during connection failure", e)
                    mainHandler.obtainMessage(CONNECT_FAIL).sendToTarget()
                }

            }
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
//  Поток менеджера подключения
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

//        private val TAG = "ConnectedThread"

        private lateinit var mmInStream: InputStream
        private lateinit var mmOutStream: OutputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            name="ConnectedThread"
            if (this::mmInStream.isInitialized) {
                mmInStream.close()
            }
            if (this::mmOutStream.isInitialized) {
                mmOutStream.close()
            }
            mmInStream = mmSocket.inputStream
            mmOutStream = mmSocket.outputStream

            Log.d(TAG, "Connected thread start")

            while (true) {

                val numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }


                val readMsg = mainHandler.obtainMessage(MESSAGE_READ, numBytes,-1)
                val bundle = Bundle().apply {
                    putString("sender", mmSocket.remoteDevice.name)
                    putByteArray("mmBuffer", mmBuffer)
                }
                readMsg.data = bundle
                readMsg.sendToTarget()
            }
        }


        fun write(bytes: ByteArray) {
            Log.d(TAG, "message $bytes")
            try {
                mmOutStream.write(bytes)
                Log.d(TAG, bytes.toString())
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)


                val writeErrorMsg = mainHandler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                mainHandler.sendMessage(writeErrorMsg)
                return
            }


            val writtenMsg = mainHandler.obtainMessage(
                MESSAGE_WRITE, -1, -1, bytes)
            writtenMsg.sendToTarget()
        }


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

    fun disconnect() {
        mConnectThread?.cancel()
        mConnectedThread?.cancel()
        mSecureAcceptThread?.cancel()
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

