package azotzot.bluetoothmorsechat

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import java.util.*

class ChatService : Service() {

    private val TAG = "ChatService"

    private val APP_UUID = UUID.fromString("b7e7378a-ed1b-11e9-81b4-2a2ae2dbcce4")
//    private val CLIENT_UUID = UUID.fromString("b15c5ce8-1ff8-45d6-b0b4-56f650922c04")

    private val NAME_SECURE = "ChatService"
    private val REQUEST_ENABLE_BT = 0


    private val CONNECT = 0
    private val COMMAND_LISTEN = 1


    private lateinit var serverSocket: BluetoothServerSocket
    private lateinit var clientSocket: BluetoothSocket

    private lateinit var bluetoothAdapter: BluetoothAdapter

    private lateinit var bluetoothThread: BluetoothThread

    override fun onCreate() {
        super.onCreate()
        bluetoothThread = BluetoothThread("bluetoothThread")
        bluetoothThread.prepareHandler()

        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, APP_UUID)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bluetoothThread.start()

        if (intent != null) {
            bluetoothThread.postTask(Runnable {
                when (intent.getIntExtra("command", 0)) {

                    CONNECT -> {
                        connectToDevice(intent)
                    }

                    COMMAND_LISTEN-> {
                        startListenConnection()
                    }

                }
            })
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun connectToDevice(intent: Intent) {
        val devicePosition = intent.getIntExtra("devicePosition",0)
        val connectionDevice =  bluetoothAdapter.bondedDevices.toMutableList()[devicePosition]
        if (!clientSocket.isConnected) {
            clientSocket = connectionDevice.createRfcommSocketToServiceRecord(APP_UUID)
            clientSocket.connect()

        } else {
            ////ЕСЛИ ПОДКЛЮЧЕН ТО СОХРАНИТЬ ЧЕНИТЬ И ПОДКЛЮЧИТЬСЯ СНОВА
        }
    }


    private fun startListenConnection() {
            serverSocket.accept()
    }


    override fun onDestroy() {
        super.onDestroy()
        bluetoothThread.quit()

    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}

class BluetoothThread(name: String): HandlerThread(name) {
    private lateinit var bluetoothHandler: Handler

    fun prepareHandler() {
        bluetoothHandler = Handler(looper)
    }

    fun postTask(task: Runnable) {
        bluetoothHandler.post(task)
    }

}

