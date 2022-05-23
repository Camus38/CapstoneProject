package com.hansung2022.capstoneday1zipkok.BoardImage

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hansung2022.capstoneday1zipkok.R

class MultiImageAdapter(private val items: MutableList<Uri>, val context: Context) :
    RecyclerView.Adapter<MultiImageAdapter.ViewHolder>() {

    //
    interface ItemClick{
        fun onClick(view: View, position: Int)
    }
    var itemClick : MultiImageAdapter.ItemClick? = null


    interface ImageClick{
        fun onClick(view: View, position: Int)
    }
    var imageClick : MultiImageAdapter.ImageClick? = null

    class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
        private var view : View = v
        var image = v.findViewById<ImageView>(R.id.image)
        var ivDelete = v.findViewById<ImageView>(R.id.iv_delete)

        fun bind(listener: View.OnClickListener, item : String){
            view.setOnClickListener(listener)
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.multi_image_item,parent,false)
        return ViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        Glide.with(context).load(item)
            .override(100,100)
            .into(holder.image)

        if(itemClick != null){
            holder.ivDelete.setOnClickListener {
                itemClick?.onClick(it, position)
            }

        }

        if(imageClick != null){
            holder.image.setOnClickListener {
                imageClick?.onClick(it, position)
            }

        }
    }

    override fun getItemCount() = items.size

}