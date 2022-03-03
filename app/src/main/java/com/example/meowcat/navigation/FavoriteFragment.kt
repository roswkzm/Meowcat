package com.example.meowcat.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.meowcat.PickProductActivity
import com.example.meowcat.R
import com.example.meowcat.contentUid
import com.example.meowcat.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_favorite_fragment.view.*
import kotlinx.android.synthetic.main.item_home.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class FavoriteFragment : Fragment(){

    var uid : String? = null
    // 좋아요 누른 상품의 documents 값이 담긴다
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_favorite_fragment, container, false)
        // hashMapOf(uid to true)
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.favoriteFragment_recyclerView.adapter = FavoriteProductRecyclerViewAdapter()
        view.favoriteFragment_recyclerView.layoutManager = GridLayoutManager(activity,2)

        return view
    }

    // 좋아요 누른 게시물에 따른 정보를 가져와서 recyclerView로 뿌림
    inner class FavoriteProductRecyclerViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {
            FirebaseFirestore.getInstance().collection("images").whereArrayContains("favorites", uid!!).addSnapshotListener { value, error ->
                if (value == null) return@addSnapshotListener
                contentDTOs.clear()
                contentUidList.clear()
                for (snapshot in value.documents){
                    contentUidList.add(snapshot.id)
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    Log.d("ㅎㅇㅎㅇ", contentUidList.toString())
                    Log.d("ㅎㅇㅎㅇ", contentDTOs.toString())
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_home,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = holder.itemView
            // 고양이 사진
            viewHolder.homeFragment_content_image.clipToOutline = true
            Glide.with(viewHolder.context).load(contentDTOs[position].imageUrl).into(viewHolder.homeFragment_content_image)
            // 고양이 이름
            viewHolder.homeFragment_product_name.text = contentDTOs[position].productName
            // 고양이 성별
            if (contentDTOs[position].productGender == "남"){
                viewHolder.homeFragment_product_gender.setImageResource(R.drawable.male_symbol)
            }else{
                viewHolder.homeFragment_product_gender.setImageResource(R.drawable.female_symbol)
            }
            // 고양이 품종
            viewHolder.homeFragment_product_type.text = contentDTOs[position].productType

            viewHolder.homeFragment_content_image.setOnClickListener {
                var intent = Intent(context, PickProductActivity::class.java)
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                intent.putExtra("contentUidList", contentUidList[position])
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}