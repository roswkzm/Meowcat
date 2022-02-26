package com.example.meowcat

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.meowcat.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_pick_product.*
import kotlinx.android.synthetic.main.activity_pick_product.view.*
import kotlinx.android.synthetic.main.item_content_detail.view.*
import kotlinx.android.synthetic.main.item_home.view.*

class ViewpagerAdapter (var destinationUid : String) : RecyclerView.Adapter<ViewpagerAdapter.ViewPagerViewHolder>(){
    var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
    var contentUid : ArrayList<String> = arrayListOf()
    var uid = FirebaseAuth.getInstance().currentUser?.uid

    init {
        FirebaseFirestore.getInstance().collection("images").whereEqualTo("uid", destinationUid)
            .orderBy("timestamp",Query.Direction.DESCENDING).addSnapshotListener { value, error ->
                contentDTOs.clear()
                contentUid.clear()
                if (value == null) return@addSnapshotListener

                for (snapshot in value.documents){
                    contentUid.add(snapshot.id)
                    Log.d("ㅎㅇㅎㅇㅎㅇㅎㅇ", snapshot.toObject(ContentDTO::class.java)!!.toString())
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewpagerAdapter.ViewPagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_pick_product,parent,false)
        return ViewPagerViewHolder(view)
    }

    inner class ViewPagerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onBindViewHolder(holder: ViewpagerAdapter.ViewPagerViewHolder, position: Int) {
        val view = holder.itemView
        // 회원 프사 매핑, 닉네임 매핑
        FirebaseFirestore.getInstance().collection("users").document(destinationUid).addSnapshotListener { value, error ->
            if(value == null) return@addSnapshotListener
            if (value.data != null){
                var url = value.data!!["imageUrl"]
                Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.pickProduct_iv_profile)

                // 회원 닉네임
                view.pickProduct_tv_userId.text = value.data!!["userNickName"].toString()
            }
        }
        // 고양이 이름
        view.pickProduct_tv_productName.text = contentDTOs[position].productName
        // 고양이 성별
        if (contentDTOs[position].productGender == "남") {
            view.pickProduct_iv_productGender.setImageResource(R.drawable.male_symbol)
        } else if (contentDTOs[position].productGender == "여") {
            view.pickProduct_iv_productGender.setImageResource(R.drawable.female_symbol)
        }
        // 고양이 품종
        view.pickProduct_tv_productType.text = contentDTOs[position].productType
        // 고양이 사진
        var url = contentDTOs[position].imageUrl
        Glide.with(view.context).load(url).into(view.pickProduct_iv_productImage)
        // 좋아요 버튼 이미지 초기 세팅
        if (contentDTOs[position].favorites.containsKey(uid)){
            view.pickProduct_iv_favorite.setImageResource(R.drawable.heart)
        }else{
            view.pickProduct_iv_favorite.setImageResource(R.drawable.empty_heart)
        }
        // 좋아요 버튼 클릭시
        view.pickProduct_iv_favorite.setOnClickListener {
            favoriteEvent(position)
        }
        // 좋아요 갯수
        view.pickProduct_tv_favoriteCount.text = "like: ${contentDTOs[position].favoriteCount}"
        // 댓글 버튼
        view.pickProduct_iv_comment.setOnClickListener {
            var intent = Intent(view.context, CommentActivity::class.java)
            intent.putExtra("contentUid", contentUid[position])
            intent.putExtra("destinationUid", destinationUid)
            // viewPager2에서 startActivity를 하고싶으면 context에서 .startActivity를 해야한다.
            view.context.startActivity(intent)
        }
        // 판매자 상품 더보기 버튼 지우기
        view.pickProduct_btn_moreProduct.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return contentDTOs.size
    }

    fun favoriteEvent(position: Int){
        var tsDoc = FirebaseFirestore.getInstance().collection("images").document(contentUid[position])

        FirebaseFirestore.getInstance().runTransaction { transaction ->

            var contentDTO = transaction.get(tsDoc).toObject(ContentDTO::class.java)    // contentDTO에 정보 캐스팅

            if (contentDTO!!.favorites.containsKey(uid)){   // 좋아요를 누른 상태라면
                contentDTO?.favoriteCount = contentDTO.favoriteCount -1
                contentDTO?.favorites.remove(uid)
            }else{      // 좋아요를 누르지 않은 상태라면
                contentDTO?.favoriteCount = contentDTO.favoriteCount +1
                contentDTO?.favorites[uid!!] = true
            }
            transaction.set(tsDoc, contentDTO)
        }
    }
}