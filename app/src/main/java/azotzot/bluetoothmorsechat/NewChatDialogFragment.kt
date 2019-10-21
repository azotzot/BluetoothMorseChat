package azotzot.bluetoothmorsechat

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NewChatDialogFragment(private val pairDevicesAdapter: PairDevicesAdapter) : DialogFragment() {

    private val TAG = "NewChatDialogFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, "$pairDevicesAdapter")
        return view
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val rv = RecyclerView(requireContext())
        with(rv) {
            layoutManager = LinearLayoutManager(context)
            adapter = pairDevicesAdapter
            setHasFixedSize(true)
        }

        return with(AlertDialog.Builder(activity)) {
            setTitle("Choose device")
            setView(rv)
            create()

        }
    }
}