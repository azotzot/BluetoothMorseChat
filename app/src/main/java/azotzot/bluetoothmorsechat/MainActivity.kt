package azotzot.bluetoothmorsechat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var pairedDevices: MutableList<BluetoothDevice>
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val REQUEST_ENABLE_BT = 0

    private val COMMAND_LISTEN = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(Intent(this, ChatService::class.java))

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//        if (!bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//        }

        pairedDevices = bluetoothAdapter.bondedDevices.toMutableList()

//        startService(Intent(this, ChatService::class.java))

        sendButton.setOnClickListener {
            send()
        }

    }


    fun send() {
        Toast.makeText(this, "Send message", Toast.LENGTH_SHORT).show()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_connect, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId) {
            R.id.connectTo -> {
                val chosenDialog = NewChatDialogFragment(pairedDevices)
                chosenDialog.show(supportFragmentManager,"newChatDialog")
            }
            R.id.listenConnect -> {
                startService(Intent(this, ChatService::class.java).putExtra("commant", COMMAND_LISTEN))
                Toast.makeText(this, "Wait connection...", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()

        stopService(Intent(this, ChatService::class.java))

    }
}
