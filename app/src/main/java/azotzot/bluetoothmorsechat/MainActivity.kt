package azotzot.bluetoothmorsechat

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private val NAME_SECURE = "ChatService"


    private val setDevices = mutableSetOf<BluetoothDevice>()
    

    private lateinit var pairedDevices: MutableList<BluetoothDevice>
    private lateinit var serverSocket: BluetoothServerSocket
    private lateinit var clientSocket: BluetoothSocket
    private lateinit var chatService: ChatService

    private val REQUEST_ENABLE_BT = 0

    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        pairedDevices = bluetoothAdapter.bondedDevices.toMutableList()

//        startService(Intent(this, ChatService::class.java))

        sendButton.setOnClickListener {
            send()
        }

    }

    override fun onStart() {
        super.onStart()


//        var pairsDevicesString = ""

//        pairedDevices.forEach {
//            pairsDevicesString += "Name: ${it.name} Address: ${it.address} \n"
//        }
//        pairsDevices.text = pairsDevicesString


    }

    override fun onDestroy() {
        super.onDestroy()
//        stopService(Intent(this, ChatService::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_connect, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId) {
            R.id.createChat -> {
                val chosenDialog = NewChatFragment()
                chosenDialog.show(supportFragmentManager,"newChatDialog")
//                true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun send() {
        Toast.makeText(this, "Send message", Toast.LENGTH_SHORT).show()
    }


    class NewChatFragment: DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(activity).setTitle("Choose device")
                    .setView(R.layout.choose_devices_dialog).create()
        }
    }
}
