package com.example.meowcat.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.meowcat.PickProductActivity
import com.example.meowcat.R
import com.example.meowcat.navigation.model.ContentDTO
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_search_fragment.*
import kotlinx.android.synthetic.main.activity_search_fragment.view.*
import kotlinx.android.synthetic.main.item_home.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

// 품종에 대한 검색을 하는 Fragment

var spinnerList : ArrayList<String>? = null
var arrayAdapter : ArrayAdapter<String>? = null

class SearchFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_search_fragment, container, false)

        Log.d("ㅎㅇㅎㅇ", "코루틴 시작")
        CoroutineScope(Dispatchers.IO).launch {
//            runBlocking {
                val spinnerValue = async { getSpinnerList() }
            Log.d("favoriteList", spinnerValue.toString())

                withContext(Dispatchers.Main){
                    Log.d("ㅎㅇㅎㅇ", "어댑터 생성 전")
                    arrayAdapter = ArrayAdapter(view.context, R.layout.support_simple_spinner_dropdown_item, spinnerValue.await())
                    Log.d("ㅎㅇㅎㅇ", "어댑터 생성 후")
                    view.searchFragment_spinner.adapter = arrayAdapter
                    Log.d("ㅎㅇㅎㅇ", "어탭터 연결 후")
                    view.searchFragment_spinner.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                            view.searchFragment_btn_search.setOnClickListener {
                                view.searchFragment_recyclerView.adapter = SearchFragmentRecyclerViewAdapter(ArrayList(spinnerList)[p2])
                                view.searchFragment_recyclerView.layoutManager = GridLayoutManager(activity, 2)
                            }
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }
                    }
                }
//            }
        }
        Log.d("ㅎㅇㅎㅇ", "코루틴 끝")
        return view
    }
    suspend fun getSpinnerList() : ArrayList<String>{
        spinnerList = arrayListOf()
        Log.d("ㅎㅇㅎㅇ", "Firebase에서 시작")
        // 품종 리스트를 가져와서 arrayList에 저장
        var storeData = FirebaseFirestore.getInstance().collection("information").document("productType").get().await().data!!.keys
        Log.d("ㅎㅇㅎㅇ", "값 넣기 전")
        spinnerList = ArrayList(storeData)
        Log.d("ㅎㅇㅎㅇ", "값 넣기 후")
        Log.d("favoriteList", spinnerList.toString())
        Log.d("ㅎㅇㅎㅇ", "Firebase에서 종료")
        return spinnerList!!
    }
    inner class SearchFragmentRecyclerViewAdapter(curProductType : String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {
            FirebaseFirestore.getInstance().collection("images").whereEqualTo("productType", curProductType)
                .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
                    if (value == null) return@addSnapshotListener

                    for (snapshot in value.documents){
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                        contentUidList.add(snapshot.id)
                        Log.d("ㅂㅇㅂㅇ", contentUidList.toString())
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
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
            // 고양이 사진 클릭시
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
