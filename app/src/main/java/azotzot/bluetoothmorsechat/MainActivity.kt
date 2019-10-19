package azotzot.bluetoothmorsechat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import azotzot.bluetoothmorsechat.Constants.Companion.CHANGE_UI
import azotzot.bluetoothmorsechat.Constants.Companion.MESSAGE_READ
import azotzot.bluetoothmorsechat.Constants.Companion.MESSAGE_TOAST
import azotzot.bluetoothmorsechat.Constants.Companion.REQUEST_ENABLE_BT
import kotlinx.android.synthetic.main.activity_main.*
import azotzot.bluetoothmorsechat.Constants.Companion.CONNECT_FAIL
import azotzot.bluetoothmorsechat.Constants.Companion.MESSAGE_WRITE
import java.nio.charset.Charset
import azotzot.bluetoothmorsechat.Message as MyMessage


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var pairedDevices: MutableList<BluetoothDevice>
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var mainHandler: MainHandler
    private lateinit var chatService: ChatService

    private val messages = mutableListOf<MyMessage>(MyMessage("debug","test"))


    private lateinit var messagesAdapter: MessagesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        pairedDevices = bluetoothAdapter.bondedDevices.toMutableList()
        mainHandler = MainHandler()

        chatService = ChatService(this, mainHandler).apply { start() }

        messagesAdapter = MessagesAdapter(messages, this@MainActivity)

        messagesList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                this.reverseLayout = true
            }
            adapter = messagesAdapter
        }

        sendButton.setOnClickListener {
            val message = editMessage.text.toString()
            if (message.isNotEmpty())
                send(message)
        }
    }


    private fun send(message: String) {
//        val message = "test"
        val send = message.toByteArray(Charsets.UTF_8)
        chatService.write(send)
        editMessage.text.clear()
        Toast.makeText(this, "Send: $message", Toast.LENGTH_SHORT).show()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_connect, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.connectTo -> {
                val adapter = PairDevicesAdapter(pairedDevices, this, chatService)
                val chosenDialog = NewChatDialogFragment(adapter)
                chosenDialog.show(supportFragmentManager, "newChatDialog")
            }
            R.id.listenConnect -> {
                chatService.listenConnect()
                Toast.makeText(this, "Wait connection...", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        chatService.disconnect()

    }

    @SuppressLint("HandlerLeak")
    inner class MainHandler : Handler() {
//        private val TAG = "MainHandler"
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                CHANGE_UI -> {
                    val ttl = msg.obj as String
                    Log.d(TAG, "Change Main Title to $ttl")
                    title = ttl
                }
                MESSAGE_TOAST -> {
                    Log.d(TAG, msg.data.getString("toast"))
                    Toast.makeText(this@MainActivity, msg.data.getString("toast"),Toast.LENGTH_SHORT).show()
                }
                MESSAGE_READ -> {
//                    Log.d(TAG, msg.data.getString("toast"))
                    val readBuf = msg.data.getByteArray("mmBuffer")
                    // construct a string from the valid bytes in the buffer
                    var readMessage: String = ""
                    if (readBuf != null) {
//                        readMessage = String(readBuf, 0, msg.arg1)
                        readMessage = String(readBuf,0,msg.arg1)
                    }
                    messages.add(0,MyMessage(msg.data.getString("sender"),readMessage))
                    messagesAdapter.notifyItemInserted(0)
                    Toast.makeText(this@MainActivity, readMessage,Toast.LENGTH_SHORT).show()
                }
                MESSAGE_WRITE -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf)
                    messages.add(0,MyMessage("Me",readMessage))
                    messagesAdapter.notifyItemInserted(0)

                }
                CONNECT_FAIL -> {
                    Toast.makeText(this@MainActivity, "Connect failed, check second device",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}