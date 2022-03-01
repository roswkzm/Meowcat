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
            runBlocking {
                val spinnerValue = async { getSpinnerList() }

                withContext(Dispatchers.Main){
                    Log.d("ㅎㅇㅎㅇ", "어댑터 생성 전")
                    arrayAdapter = ArrayAdapter(view.context, R.layout.support_simple_spinner_dropdown_item, spinnerValue.await())
                    Log.d("ㅎㅇㅎㅇ", "어댑터 생성 후")
                    view.searchFragment_spinner.adapter = arrayAdapter
                    Log.d("ㅎㅇㅎㅇ", "어탭터 연결 후")
                }
            }
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
        Log.d("ㅎㅇㅎㅇ", "Firebase에서 종료")
        return spinnerList!!
    }
}
