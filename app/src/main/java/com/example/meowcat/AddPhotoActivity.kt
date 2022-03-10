package com.example.meowcat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.meowcat.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class AddPhotoActivity : AppCompatActivity() {
    val PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        // Firebase 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        iv_addPhotoImage.setOnClickListener {
            // 앨범접근
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }

        addphoto_btn_upload.setOnClickListener {
            productTypeUpload()
            contentUpload()
        }
    }

    // 사진첩에서 골라온 사진 ImageView에 바인딩
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                iv_addPhotoImage.setImageURI(photoUri)
            }else{
                finish()
            }
        }
    }
    fun productTypeUpload(){
        var productType : MutableMap<String, Boolean> = HashMap()
        FirebaseFirestore.getInstance().collection("information").document("productType").get().addOnSuccessListener { task ->
            if (task == null) {
                Log.d("ㅎㅇㅎㅇ", "null")
                return@addOnSuccessListener
            }
            if (task.data == null){
                Log.d("ㅎㅇㅎㅇ", "데이터 눌")
                productType[et_productType.text.toString()] = true
                FirebaseFirestore.getInstance().collection("information").document("productType").set(productType)
                return@addOnSuccessListener
            }
            if (task.data?.containsKey(et_productType.text.toString()) == true){
                Log.d("ㅎㅇㅎㅇ", "if")
                return@addOnSuccessListener
            }else{
                Log.d("ㅎㅇㅎㅇ", "else")
                productType[et_productType.text.toString()] = true
                FirebaseFirestore.getInstance().collection("information").document("productType").set(productType,
                    SetOptions.merge())
            }
        }
    }

    fun contentUpload(){
        // 파일이름 매칭
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "Image_" + timestamp + "_.png"

        // Storage에 사진 업로드
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)
        storageRef?.putFile(photoUri!!)?.addOnCompleteListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->

                var contentDTO = ContentDTO()

                contentDTO.imageUrl = uri.toString()                                // 사진 주소
                contentDTO.imageName = imageFileName                                // 사진 이름
                contentDTO.uid = auth?.currentUser?.uid                             // 회원 uid
                contentDTO.userId = auth?.currentUser?.email                        // 회원 email
                contentDTO.timestamp = System.currentTimeMillis()                   // 사진 등록 시간
                contentDTO.productName = et_productName.text.toString()             // 동물 이름
                contentDTO.productGender = et_productGender.text.toString()         // 동물 성별
                contentDTO.productType = et_productType.text.toString()             // 동물 품종
                contentDTO.productExplain = et_productExplain.text.toString()       // 동물 설명

                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

}