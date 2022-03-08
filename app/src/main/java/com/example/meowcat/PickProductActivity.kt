package com.example.meowcat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.TextKeyListener.clear
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.meowcat.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
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

        // 회원 프사 매핑, 닉네임 매핑
        FirebaseFirestore.getInstance().collection("users").document(destinationUid!!).addSnapshotListener { value, error ->
            if(value == null) return@addSnapshotListener
            if (value.data != null){
                var url = value.data!!["imageUrl"]
                Glide.with(this).load(url).apply(RequestOptions().circleCrop()).into(pickProduct_iv_profile)

                // 회원 닉네임
                pickProduct_tv_userId.text = " ${value.data!!["userNickName"]} 님의 게시물"
            }
        }



        FirebaseFirestore.getInstance().collection("images").document(contentUid!!).addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener

            if (value != null) {

                // Glide에 이미지가 들어가기 전에 OnBackPressd가 실행되어 이미지가 올라갈 공간이 없을경우에 대한 오류 방지
                if (this.isFinishing){
                    return@addSnapshotListener
                }
                // 고양이 사진
                Glide.with(this).load(value.data?.get("imageUrl")).into(pickProduct_iv_productImage)
                // 고양이 이름
                pickProduct_tv_productName.text = " 이름 : ${value.data!!["productName"].toString()}"
                // 고양이 성별
                if (value.data!!["productGender"] == "남") {
                    pickProduct_iv_productGender.setImageResource(R.drawable.male_symbol)
                } else if (value.data!!["productGender"] == "여") {
                    pickProduct_iv_productGender.setImageResource(R.drawable.female_symbol)
                }
                // 고양이 종류
                pickProduct_tv_productType.text = " 종류 : ${value.data!!["productType"].toString()}"
                // 고양이 설명
                pickProduct_tv_productExplain.text = value.data!!["productExplain"].toString()
                // 좋아요 개수
                pickProduct_tv_favoriteCount.text =
                    "like: ${value.data!!["favoriteCount"]}"
                // 회원이 이미 좋아요를 누르고있다면
                if (value.data!!["favorites"].toString()
                        .contains(FirebaseAuth.getInstance().currentUser!!.uid)
                ) {
                    pickProduct_iv_favorite.setImageResource(R.drawable.heart)
                } else {
                    pickProduct_iv_favorite.setImageResource(R.drawable.empty_heart)
                }
                // 메시지 버튼 클릭시
                pickProduct_iv_message.setOnClickListener {
                    if (FirebaseAuth.getInstance().currentUser!!.uid == value.data!!["uid"].toString()){
                        Toast.makeText(this,"내가 판매중인 상품입니다.", Toast.LENGTH_SHORT).show()
                    }else{
                        var intent = Intent(this, MessageActivity::class.java)
                        intent.putExtra("destinationUid", value.data!!["uid"].toString())
                        startActivity(intent)
                    }
                }
            }
        }

        // 좋아요버튼 클릭시
        pickProduct_iv_favorite.setOnClickListener {
            favoriteEvent()
        }

        // 댓글버튼 클릭시
        pickProduct_iv_comment.setOnClickListener {
            var intent = Intent(this,CommentActivity::class.java)
            intent.putExtra("contentUid", contentUid)
            intent.putExtra("destinationUid", destinationUid)
            startActivity(intent)
        }

        // 판매자 상품 더보기 버튼 클릭시
        pickProduct_btn_moreProduct.setOnClickListener {
            var intent = Intent(this, ContentDetailActivity::class.java)
            intent.putExtra("destinationUid", destinationUid)
            startActivity(intent)
        }
    }

    fun favoriteEvent(){
        // tsDoc -> 저장소 접근
        var tsDoc = FirebaseFirestore.getInstance().collection("images")?.document(contentUid!!)

        // 데이터를 입력하기 위해 transaction을 불러온다
        FirebaseFirestore.getInstance().runTransaction { transaction ->

            var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)      // contentDTO에 값을 캐스팅


            //회원이 이미 좋아요를 누른상태
            if (contentDTO!!.favorites.contains(FirebaseAuth.getInstance().currentUser!!.uid)){
                contentDTO?.favoriteCount = contentDTO?.favoriteCount -1        // 좋아요 -1
                contentDTO?.favorites.remove(FirebaseAuth.getInstance().currentUser!!.uid)      // 좋아요누른회원에서 제거
                pickProduct_iv_favorite.setImageResource(R.drawable.empty_heart)
            }else{  // 좋아요를 누르지 않았다면
                contentDTO?.favoriteCount = contentDTO?.favoriteCount +1    // 좋아요 +1
                contentDTO?.favorites.add(FirebaseAuth.getInstance().currentUser!!.uid)      // 좋아요누른 회원 추가
                pickProduct_iv_favorite.setImageResource(R.drawable.heart)
            }
            // 트랜잭션의 결과를 서버로 되돌려준다.
            transaction.set(tsDoc, contentDTO)
       }
    }
}