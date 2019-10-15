package azotzot.bluetoothmorsechat

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.devices_item.view.*

class PairDevicesAdapter(private val pairedDevices: MutableList<BluetoothDevice>, private val context: Context) : RecyclerView.Adapter<PairDevicesAdapter.DevicesViewHolder>() {

    private val TAG = "PairDevicesAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.devices_item, parent, false)
        return DevicesViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        holder.deviceName.text = pairedDevices[position].name
        holder.deviceAddress.text = pairedDevices[position].address
    }

    override fun getItemCount() = pairedDevices.size

    class DevicesViewHolder(itemView: View, context: Context): RecyclerView.ViewHolder(itemView) {
        private val TAG = "DevicesViewHolder"

        var deviceName: TextView = itemView.deviceName
        var deviceAddress: TextView = itemView.deviceAddress

        init {
            itemView.setOnClickListener {
                val pos = this.layoutPosition
                if (pos != RecyclerView.NO_POSITION) {
                    //пока ничего
                }
                Log.d(TAG, "Click position $pos")

                Toast.makeText( context, "Connecting...", Toast.LENGTH_SHORT).show()
            }

        }

    }

}
