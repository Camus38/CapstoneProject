package com.hansung2022.capstoneday1zipkok.BoardImage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hansung2022.capstoneday1zipkok.R

class MultiImageReadAdapter(private val items: MutableList<String>, val context: Context) :
    RecyclerView.Adapter<MultiImageReadAdapter.ViewHolder>() {


    interface ImageClick {
        fun onClick(view: View, position: Int)
    }

    var imageClick: MultiImageReadAdapter.ImageClick? = null

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        var image = v.findViewById<ImageView>(R.id.image_read)


        fun bind(listener: View.OnClickListener, item: String) {
            view.setOnClickListener(listener)
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
            .inflate(R.layout.multi_image_read_item, parent, false)
        return ViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        Glide.with(context)
            .load(item)
            .override(100, 100)
            .into(holder.image)



        if (imageClick != null) {
            holder.image.setOnClickListener {
                imageClick?.onClick(it, position)
            }

        }
    }

    override fun getItemCount() = items.size

}