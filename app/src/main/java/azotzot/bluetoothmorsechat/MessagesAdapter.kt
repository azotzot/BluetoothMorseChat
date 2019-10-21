package azotzot.bluetoothmorsechat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.message_item.view.*

class MessagesAdapter(private val messages: MutableList<Message>, private val context: Context) : RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder>() {

    init {
        Log.d("Message", "adapter create")
    }
    private val TAG = "MessagesAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        Log.d(TAG, "onCreateViewHolder")
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessagesViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        Log.d(TAG, "$messages")
        holder.content.text = messages[position].content
        holder.mesSender.text = messages[position].mesSender
    }

    override fun getItemCount() = messages.size

    class MessagesViewHolder(
        itemView: View,
        context: Context
    ): RecyclerView.ViewHolder(itemView) {
        private val TAG = "MessagesViewHolder"

        var content: TextView = itemView.content
        var mesSender: TextView = itemView.mesSender

        init {

            Log.d(TAG, "init MessagesViewHolder")
//            itemView.setOnClickListener {
//
//                Toast.makeText( context, "", Toast.LENGTH_SHORT).show()
//
//            }

        }

    }

}
