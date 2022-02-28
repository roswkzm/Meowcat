package com.example.meowcat.navigation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.fragment.app.Fragment
import com.example.meowcat.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_search_fragment.*
import kotlinx.android.synthetic.main.activity_search_fragment.view.*

// 품종에 대한 검색을 하는 Fragment

var spinnerList : ArrayList<String>? = null
var arrayAdapter : ArrayAdapter<String>? = null

class SearchFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_search_fragment, container, false)

        spinnerList = arrayListOf()

        // 품종 리스트를 가져와서 arrayList에 저장
        FirebaseFirestore.getInstance().collection("information").document("productType").get().addOnSuccessListener { task ->
            if (task == null) return@addOnSuccessListener
            if (task.data == null) return@addOnSuccessListener
            Log.d("ㅎㅇㅎㅇ", "Firebase에서 값 저장 전")
            spinnerList = ArrayList(task.data!!.keys)     // task.data!!.keys는 MutableSet 형식임으로 배열로 변환
            Log.d("ㅎㅇㅎㅇ", "Firebase에서 값저장후")
        }

        Log.d("ㅎㅇㅎㅇ", "어댑터 생성 전")
        arrayAdapter = ArrayAdapter(view.context, R.layout.support_simple_spinner_dropdown_item, spinnerList!!)
        Log.d("ㅎㅇㅎㅇ", "어댑터 생성 후")
        view.searchFragment_spinner.adapter = arrayAdapter
        Log.d("ㅎㅇㅎㅇ", "어탭터 연결 후")
        return view
    }
}