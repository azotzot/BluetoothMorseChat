package azotzot.bluetoothmorsechat

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NewChatDialogFragment(private val pairedDevices: MutableList<BluetoothDevice>) : DialogFragment() {

    private val TAG = "NewChatDialogFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, "$pairedDevices")
        return view
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val rv = RecyclerView(requireContext())
        with(rv) {
            layoutManager = LinearLayoutManager(context)
            adapter = PairDevicesAdapter(pairedDevices, context, dialog)
            setHasFixedSize(true)
        }

        return with(AlertDialog.Builder(activity)) {
            setTitle("Choose device")
            setView(rv)
            create()

        }
    }
}