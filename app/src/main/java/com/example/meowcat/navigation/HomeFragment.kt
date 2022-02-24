package com.example.meowcat.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.meowcat.R
import com.example.meowcat.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_add_photo.view.*
import kotlinx.android.synthetic.main.activity_home_fragment.view.*
import kotlinx.android.synthetic.main.item_home.view.*

class HomeFragment : Fragment(){
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_home_fragment, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.homeFragment_recyclerView.adapter = HomeFragmentRecyclerViewAdapter()
//        view.homeFragment_recyclerView.layoutManager = LinearLayoutManager(activity)
        view.homeFragment_recyclerView.layoutManager = GridLayoutManager(activity,2)
        return view
    }

    // RecyclerViewAdapter 내부클래스로 생성
    inner class HomeFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()      // 해당 documents의 이름을 담는다.

        init {
            // DB접근해서 데이터 받아오기
            firestore?.collection("images")?.orderBy("timestamp", Query.Direction.DESCENDING)?.addSnapshotListener { value, error ->
                contentDTOs.clear() // contentDTOs 배열 초기화
                if (value == null) return@addSnapshotListener
                // for문으로 스냅샷에 넘어오는 전부를 읽어온다
                for (snapshot in value!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
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
            var viewHolder = (holder as CustomViewHolder).itemView

            viewHolder.homeFragment_content_image.clipToOutline = true

            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewHolder.homeFragment_content_image)

            viewHolder.homeFragment_product_name.text = contentDTOs[position].productName

            if(contentDTOs[position].productGender == "남"){
                viewHolder.homeFragment_product_gender.setImageResource(R.drawable.male_symbol)
            }else if (contentDTOs[position].productGender == "여"){
                viewHolder.homeFragment_product_gender.setImageResource(R.drawable.female_symbol)
            }

            viewHolder.homeFragment_product_type.text = contentDTOs[position].productType
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}