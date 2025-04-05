package com.devstree.product.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devstree.product.databinding.AllAddresesListRvItemBinding
import com.devstree.product.model.AddressSuggestModel


class AllLocationAdapter(
    private val context: Context?,
    private val listener: OnClickListener
) :
    RecyclerView.Adapter<AllLocationAdapter.ViewHolder>() {
    private var list: List<AddressSuggestModel>? = arrayListOf()

    //  private var itemPos = -1
    private var fullAddress = ""

    inner class ViewHolder(binding: AllAddresesListRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var binding = binding
        fun bind(data: AddressSuggestModel, position: Int) {

            binding.apply {
                addressName1.text = data.placeName
                addressName2.text = data.address
                if (data.distance != null && data.distance != 0.0){
                    distance.text = "${data.distance.toString()}km"
                }else{
                    distance.text = ""
                }
            }
            binding.deleteIcon.setOnClickListener {
                listener.onDelete(data.id)
            }
            binding.updateIcon.setOnClickListener {
                listener.onUpdate(data)
            }

        }

    }

    override fun getItemCount(): Int {
        if (list?.size!! < 1){
            return 0
        } else{
            return list?.size!!
        }
    }

    fun setList(list: List<AddressSuggestModel>?) {
        this.list = list
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AllAddresesListRvItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list!![position], position)
    }

    interface OnClickListener {
        fun onDelete(
            id: Int
        )
        fun onUpdate(data: AddressSuggestModel)
    }
}