package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GifAdapter(private val gifUrls: List<String>) : RecyclerView.Adapter<GifAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.gifImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gif, parent, false)
        return ViewHolder(view)
    }

    // handles gif loading
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gifUrl = gifUrls[position]
        Glide.with(holder.imageView)
            .asGif()
            .load(gifUrl)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return gifUrls.size
    }
}
