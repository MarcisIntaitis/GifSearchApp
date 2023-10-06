package com.example.myapplication

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GifAdapter(private val gifUrls: MutableList<String> = mutableListOf()) : RecyclerView.Adapter<GifAdapter.ViewHolder>() {

    private val data = mutableListOf<String>()
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

    fun updateData(newData: List<String>) {
        val diffCallback = GifDiffCallback(gifUrls, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        gifUrls.clear()
        gifUrls.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }
}
