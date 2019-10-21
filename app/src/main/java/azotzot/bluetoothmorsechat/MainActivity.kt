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
import azotzot.bluetoothmorsechat.Constants.Companion.ENTER_TYPE_PRESS
import azotzot.bluetoothmorsechat.Constants.Companion.MESSAGE_WRITE
import azotzot.bluetoothmorsechat.Constants.Companion.SEND_FAILED
import azotzot.bluetoothmorsechat.Constants.Companion.SEND_SUCCESS
import azotzot.bluetoothmorsechat.MorseDecoder.Companion.en
import azotzot.bluetoothmorsechat.MorseDecoder.Companion.ru
import java.util.*
import azotzot.bluetoothmorsechat.Message as MyMessage
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"


    private lateinit var pairedDevices: MutableList<BluetoothDevice>
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var mainHandler: MainHandler = MainHandler()
    private lateinit var chatService: ChatService
    private lateinit var morseDecoder: MorseDecoder

    private var enterType = ENTER_TYPE_PRESS

    private val messages = mutableListOf<MyMessage>(MyMessage("debug","test"))


    private lateinit var messagesAdapter: MessagesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        editMessage.showSoftInputOnFocus = false
        morseDecoder= MorseDecoder(if (Locale.getDefault().language == "ru") ru else en)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        pairedDevices = bluetoothAdapter.bondedDevices.toMutableList()

        messagesAdapter = MessagesAdapter(messages, this@MainActivity)
        messagesList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                this.reverseLayout = true
            }
            adapter = messagesAdapter
            setOnTouchListener { v, event ->
                editMessage.clearFocus()
                true
            }
        }


        sendButton.setOnClickListener {

            val message = editMessage.text.toString()
            if (message.isNotEmpty())
                send(message)
        }

        chatService = ChatService(this, mainHandler, bluetoothAdapter).apply { start() }
    }


    private fun send(message: String) {
//        val message = "test"
        val send = message.toByteArray(Charsets.UTF_8)
        chatService.write(send)
        editMessage.text.clear()
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyLongPress(keyCode, event)
    }

    fun morseDecode() {
        val text = morseString.text.toString()
        val decoded = morseDecoder.decodeChar(text.substring(0, text.length - 1))
        if (decoded == "") {
            Toast.makeText(this, "wrong code", Toast.LENGTH_SHORT).show()
            return
        }
        editMessage.append(decoded)
        morseString.editableText.clear()
//        Toast.makeText(this, "new letter", Toast.LENGTH_SHORT).show()
    }
    fun space() {
//        morseDecode()
        editMessage.append(" ")
        morseString.editableText.clear()
//        Toast.makeText(this, "space enter", Toast.LENGTH_SHORT).show()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {

        when(event?.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (event.repeatCount < 2) {
                    if (event.action == KeyEvent.ACTION_DOWN && !event.isLongPress) {
//                    Toast.makeText(this, "volume up pressed", Toast.LENGTH_SHORT).show()
                        Log.i(TAG, "click")
                        morseString.editableText.append("*")
                    } else if (event.isLongPress) {
                        Log.i(TAG, "long")
                        space()
                    }
                }
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event.repeatCount < 2) {
                    if (event.action == KeyEvent.ACTION_DOWN && !event.isLongPress) {
//                    Toast.makeText(this, "volume down pressed", Toast.LENGTH_SHORT).show()
                        morseString.editableText.append("-")
                    } else if (event.isLongPress) {
                        morseDecode()
                    }
                }
            }
            KeyEvent.KEYCODE_BACK -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
//                    Toast.makeText(this, "back pressed", Toast.LENGTH_SHORT).show()
                    val morseCodeLength = morseString.text.length
                    if (editMessage.isFocused) {
                        val editTextLength = editMessage.text.length
                        if(editMessage.text.isNotEmpty())
                            editMessage.editableText.delete(editTextLength-1, editTextLength)
                    } else if (morseCodeLength != 0) {
                        morseString.editableText.delete(morseCodeLength-1, morseCodeLength)
                    }
                }
            }
            else -> {
                return super.dispatchKeyEvent(event)
            }
        }
        return true
//        return super.dispatchKeyEvent(event)
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



    @SuppressLint("HandlerLeak")
    inner class MainHandler : Handler() {
//        private val TAG = "MainHandler"

        private val context = this@MainActivity
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                CHANGE_UI -> {
                    val ttl = msg.obj as String
                    Log.d(TAG, "Change Main Title to $ttl")
                    title = ttl
                }
                MESSAGE_TOAST -> {
                    when(msg.arg1) {
                        SEND_SUCCESS -> {
                            Toast.makeText(context, "Send", Toast.LENGTH_SHORT).show()
                        }
                        SEND_FAILED -> {
                            Toast.makeText(context , "check your connection", Toast.LENGTH_SHORT).show()
                        }
                    }
//                    Log.d(TAG, msg.data.getString("toast"))
//                    Toast.makeText(context, msg.data.getString("toast"),Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, readMessage,Toast.LENGTH_SHORT).show()
                }
                MESSAGE_WRITE -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf)
                    messages.add(0,MyMessage("Me",readMessage))
                    messagesAdapter.notifyItemInserted(0)

                }
                CONNECT_FAIL -> {
                    Toast.makeText(context, "Connect failed, check second device",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        chatService.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
//        chatService.disconnect()

    }
}