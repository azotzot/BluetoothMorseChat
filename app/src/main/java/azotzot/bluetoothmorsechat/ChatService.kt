package azotzot.bluetoothmorsechat

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.util.*

class ChatService(context: Context) : Service() {


    private val SERVER_UUID = UUID.fromString("b7e7378a-ed1b-11e9-81b4-2a2ae2dbcce4")
    private val CLIENT_UUID = UUID.fromString("b15c5ce8-1ff8-45d6-b0b4-56f650922c04")

//    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate() {
        super.onCreate()
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

//        if (!bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//        }

//        thread {
//
//
//        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}
