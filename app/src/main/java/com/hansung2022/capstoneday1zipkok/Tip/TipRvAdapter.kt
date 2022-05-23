package com.hansung2022.capstoneday1zipkok.Tip

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hansung2022.capstoneday1zipkok.R
import com.hansung2022.capstoneday1zipkok.databinding.TipRvItemBinding

//Glide 사용하려고 context 추가함, TipListActivity에도 baseContext 추가 해야 됨
class TipRvAdapter(val context: Context, val items: ArrayList<TipModel>, val myuid: String) :
    RecyclerView.Adapter<TipRvAdapter.ViewHolder>() {

    private val database = Firebase.database

    //클릭 -> 웹으로 이동
    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null

    interface LikeClick {
        fun onClick(view: View, position: Int)
    }

    var likeClick: LikeClick? = null


    inner class ViewHolder(val binding: TipRvItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TipRvAdapter.ViewHolder {

        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.tip_rv_item, viewGroup, false)

        return ViewHolder(TipRvItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: TipRvAdapter.ViewHolder, position: Int) {

        val item = items[position]

        //클릭 -> 웹으로 이동
        if (itemClick != null) {
            holder.binding.ivTip.setOnClickListener {
                itemClick?.onClick(it, position)
            }
            holder.binding.tvTipTitle.setOnClickListener {
                itemClick?.onClick(it, position)
            }
        }

        if (likeClick != null) {
            holder.binding.ivTipLike.setOnClickListener {
                likeClick?.onClick(it, position)
            }
        }

        //좋아요 개수 체크
        database.getReference("contentLike").child(item.content).child(item.middleCategory)
            .child(item.title).get().addOnSuccessListener {

                var valueList: ArrayList<String> = arrayListOf()
                valueList.add(it.value.toString())
                if (it.value.toString().equals("null")) {
                    Log.d("hahaha", "널이에요")
                }


                //좋아요 개수
                var count = 0

                //받은 it.value.toString()이 "null"이면
                if (it.value.toString().equals("null")) {
                    holder.binding.tvTipLike.text = "0"

                }
                //null이 아니면 카운트
                else {
                    for (time in valueList[0].split(",")) {
                        count++
                    }
                    holder.binding.tvTipLike.text = count.toString()
                }


            }

        //좋아요 했는지 체크
        database.getReference("contentLike").child(item.content).child(item.middleCategory)
            .child(item.title).child(myuid).get().addOnSuccessListener {
                Log.d("hahaha", it.toString())

                if(it.value.toString().equals("null")){
                    holder.binding.ivTipLike.setImageResource(R.drawable.ic_baseline_thumb_up_24)
                }
                else{
                    holder.binding.ivTipLike.setImageResource(R.drawable.ic_baseline_thumb_up_24_color)
                }

            }


        //holder.binding.ivTipLike.setImageResource(R.drawable.ic_baseline_thumb_up_24_color)


        holder.binding.tvTipTitle.text = item.title


        holder.binding.tvMiddleCategory.text = "#${item.middleCategory}"

        val imageViewArea = holder.binding.ivTip
        //Glide -> 현재 context넣어주고, load에 item에 있는 imageUrl넣어주고, imageViewArea에 내보내기
        Glide.with(context)
            .load(item.imageUrl)
            .into(imageViewArea)

    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


}