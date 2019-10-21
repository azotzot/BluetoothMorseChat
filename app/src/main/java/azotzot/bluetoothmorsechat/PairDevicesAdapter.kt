package azotzot.bluetoothmorsechat

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import azotzot.bluetoothmorsechat.Constants.Companion.COMMAND_CONNECT
import kotlinx.android.synthetic.main.devices_item.view.*

class PairDevicesAdapter(private val pairedDevices: MutableList<BluetoothDevice>,
                         private val context: Context,
                         private val chatService: ChatService) : RecyclerView.Adapter<PairDevicesAdapter.DevicesViewHolder>() {
    private val TAG = "PairDevicesAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        Log.d(TAG, "onCreateViewHolder")
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.devices_item, parent, false)
        return DevicesViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        Log.d(TAG, "$pairedDevices")
        holder.deviceName.text = pairedDevices[position].name
        holder.deviceAddress.text = pairedDevices[position].address
    }

    override fun getItemCount() = pairedDevices.size

    inner class DevicesViewHolder(
        itemView: View,
        context: Context
    ): RecyclerView.ViewHolder(itemView) {
        private val TAG = "DevicesViewHolder"

        var deviceName: TextView = itemView.deviceName
        var deviceAddress: TextView = itemView.deviceAddress

        init {

            Log.d(TAG, "init devicesViewHolder")
            itemView.setOnClickListener {
                val pos = this.layoutPosition
                if (pos != RecyclerView.NO_POSITION) {
                    //пока ничего
                }
                Log.d(TAG, "Click position $pos")

                chatService.connect(pairedDevices[pos])

//                dialog?.dismiss() /////СДЕЛАЬ ЧТОБ РАБОТАЛО

                Toast.makeText( context, "Connecting...", Toast.LENGTH_SHORT).show()

            }

        }

    }

}
