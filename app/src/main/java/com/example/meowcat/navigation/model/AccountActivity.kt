package com.example.meowcat.navigation.model

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.meowcat.AddPhotoActivity
import com.example.meowcat.LoginActivity
import com.example.meowcat.PICK_IMAGE_FROM_ALBUM
import com.example.meowcat.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {

    val adminUid : String = "wJNThceh9nb6jdUjbgWNjsfrnX42"
    var destinationUid : String? = null
    var uid : String? = null
    val PICK_USER_PROFILE_FORM_ALBUM = 10
    var photoUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        uid = FirebaseAuth.getInstance().currentUser?.uid   // 현재 나의 계정
        destinationUid = intent.getStringExtra("destinationUid")        // 누구의 계정에 접근했는지

        // 회원의 프로필사진과 상단바 이름을 가져옴
        getProfileImageAndUserName()

        account_recyclerView.adapter = AccountActivityRecyclerViewAdapter()
        account_recyclerView.layoutManager = GridLayoutManager(this,3)


        // 내자신의 계정에 접근했을 경우
        if (uid == destinationUid){
            // 로그아웃 버튼으로 변하게 된다.
            account_btn_logout.text = "로그 아웃"
            account_btn_logout.setOnClickListener {
                var intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                FirebaseAuth.getInstance()?.signOut()
            }
             //계정 아이디 띄워주는 부분 삭제
            account_toolbar_username.visibility = View.GONE
            // 프로필 사진 변경 기능
            account_iv_profile.setOnClickListener {
                var getUserProfileImg = Intent(Intent.ACTION_PICK)
                getUserProfileImg.type = "image/*"
                startActivityForResult(getUserProfileImg, PICK_USER_PROFILE_FORM_ALBUM)
            }

            // 내 아이디가 판매자일 경우
            if (uid == adminUid){
                account_btn_addProduct.visibility = View.VISIBLE
                account_btn_addProduct.setOnClickListener {
                    var intent = Intent(this, AddPhotoActivity::class.java)
                    startActivity(intent)
                }
            }
        }else{      // 다른 사람의 계정에 접근했을 경우

        }


    }

    // 프로필 사진 교체시 앨범에 접근하여 사진정보를 photoUri에 담음
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_USER_PROFILE_FORM_ALBUM){
            if (resultCode == Activity.RESULT_OK){
                // 사진객체 photoUri에 저장
                photoUri = data?.data
                var storageRef = FirebaseStorage.getInstance().reference.child("userProfiles").child(destinationUid!!)
                // FireStorage에 사진이 저장되었다면
                storageRef.putFile(photoUri!!).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                    return@continueWithTask storageRef.downloadUrl
                }.addOnSuccessListener { uri ->
                    // 그 사진의 downloadUrl을 Firestore에 저장
                    FirebaseFirestore.getInstance().collection("users").document(destinationUid!!).update("imageUrl",uri.toString()).addOnSuccessListener {
                        Toast.makeText(this,"사진이 변경될때까지 잠시 기다려주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                finish()
            }
        }
    }

    fun getProfileImageAndUserName(){
        FirebaseFirestore.getInstance().collection("users").document(destinationUid!!).addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener
            if (value.data != null){
                // 상단 툴바 회원 닉네임 연결
                account_toolbar_username.text = value.data!!["userNickName"].toString()
                // 회원 프로필 사진 연결
                var url = value.data!!["imageUrl"]
                Glide.with(this).load(url).apply(RequestOptions().circleCrop()).into(account_iv_profile)
            }
        }
    }

    inner class AccountActivityRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()

        init {
            FirebaseFirestore.getInstance().collection("images").whereEqualTo("uid",destinationUid)
                .orderBy("timestamp",Query.Direction.DESCENDING).addSnapshotListener { value, error ->
                    if (value == null)return@addSnapshotListener
                    contentDTOs.clear()
                    for (snapshot in value.documents){
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var imageView = ImageView(parent.context)
            // 가로 세로폭 3분의 1의 imageView가 차지함
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageView
            // 각각의 imageView에 사진 매칭
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}