package com.devstree.product.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devstree.product.R
import com.devstree.product.googlePlacesApi.PlacesListClass

class PlacesRecyclerviewAdapter(
    var list: ArrayList<PlacesListClass>,
    private val listener: OnClickListener
) :
    RecyclerView.Adapter<PlacesRecyclerviewAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.getContext())
        val listItem: View =
            layoutInflater.inflate(R.layout.addreses_list_rv_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.setText(list[position].description?.split("\\s".toRegex())?.get(0) ?: "")
        holder.textView.setText(list[position].description)
        Log.d("ListData", "onBindViewHolder: ${list[position].description}")
        holder.itemView.setOnClickListener {
            listener.onClick(list[position].place_id)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView
        var name: TextView

        init {
            textView = itemView.findViewById(R.id.address_name2)
            name = itemView.findViewById(R.id.address_name1)
        }
    }

    interface OnClickListener {
        fun onClick(places_id: String?)
    }
}