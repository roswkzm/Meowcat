package com.example.meowcat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.meowcat.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.item_content_detail.view.*
import kotlinx.android.synthetic.main.item_home.view.*

class ViewpagerAdapter (val contentDTOs : ArrayList<ContentDTO>) : RecyclerView.Adapter<ViewpagerAdapter.ViewPagerViewHolder>(){

    inner class ViewPagerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewpagerAdapter.ViewPagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_content_detail,parent,false)
        return ViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewpagerAdapter.ViewPagerViewHolder, position: Int) {
        val curItem = contentDTOs[position]
//        contentDetail_iv_profile
        holder.itemView.contentDetail_tv_userId.text = contentDTOs[position].userId
        holder.itemView.contentDetail_tv_productName.text = contentDTOs[position].productName
        holder.itemView.contentDetail_tv_productType.text = contentDTOs[position].productType
        holder.itemView.contentDetail_iv_productImage.setImageURI(contentDTOs[position].imageUrl!!.toUri())
        if(contentDTOs[position].productGender == "남"){
            holder.itemView.contentDetail_iv_productGender.setImageResource(R.drawable.male_symbol)
        }else if (contentDTOs[position].productGender == "여"){
            holder.itemView.contentDetail_iv_productGender.setImageResource(R.drawable.female_symbol)
        }
//        contentDetail_iv_favorite
//        contentDetail_tv_favoriteCount
//        contentDetail_iv_comment
    }

    override fun getItemCount(): Int {
        return contentDTOs.size
    }
}