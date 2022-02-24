package com.example.meowcat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.meowcat.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_pick_product.*

var contentUid : String? = null
var destinationUid : String? = null
var destinationUserId : String? = null
class PickProductActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_product)

        // HomeFragment에서 사진 클릭시 넘어오는 사진의 document id
        contentUid = intent.getStringExtra("contentUidList")
        destinationUid = intent.getStringExtra("destinationUid")
        destinationUserId = intent.getStringExtra("destinationUserId")

        FirebaseFirestore.getInstance().collection("images").document(contentUid!!).get().addOnSuccessListener { value ->
            if (value == null) return@addOnSuccessListener

            // 고양이 사진
            Glide.with(this).load(value.data!!["imageUrl"]).into(pickProduct_iv_productImage)
            // 고양이 이름
            pickProduct_tv_productName.text = " 이름 : ${value.data!!["productName"].toString()}"
            // 고양이 성별
            if (value.data!!["productGender"] == "남"){
                pickProduct_iv_productGender.setImageResource(R.drawable.male_symbol)
            }else if(value.data!!["productGender"] == "여"){
                pickProduct_iv_productGender.setImageResource(R.drawable.female_symbol)
            }
            // 고양이 종류
            pickProduct_tv_productType.text = " 종류 : ${value.data!!["productType"].toString()}"
            // 회원 아이디
            pickProduct_tv_userId.text = destinationUserId

        }
    }
}