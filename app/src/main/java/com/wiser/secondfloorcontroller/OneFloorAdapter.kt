package com.wiser.secondfloorcontroller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class OneFloorAdapter : RecyclerView.Adapter<OneFloorAdapter.OneFloorHolder>() {


    class OneFloorHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneFloorHolder {
        return OneFloorHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.one_floor_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: OneFloorHolder, position: Int) {
        holder.itemView?.setOnClickListener {
            holder.itemView.context?.apply {
                Toast.makeText(this,"位置：--->>$position",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getItemCount(): Int = 50
}