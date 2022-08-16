package com.example.kotlinmaps.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmaps.databinding.RecyclerRowBinding
import com.example.kotlinmaps.model.place
import com.example.kotlinmaps.view.MapsActivity

class PlaceAdapter (val placeList: List<place>):RecyclerView.Adapter<PlaceAdapter.PlaceHOlder>() {
    class PlaceHOlder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHOlder {
        val binding_row = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaceHOlder(binding_row)
    }

    override fun onBindViewHolder(holder: PlaceHOlder, position: Int) {
        holder.binding.recyclerviewText.text = placeList.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,MapsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("info","old")
            intent.putExtra("selectPlaces",placeList.get(position)) as? place
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return placeList.size
    }
}