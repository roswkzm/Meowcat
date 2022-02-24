package com.example.meowcat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.meowcat.navigation.model.ContentDTO
import com.example.meowcat.navigation.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_add_user.*

val PICK_IMAGE_FROM_ALBUM = 0
var storage : FirebaseStorage? = null
var photoUri : Uri? = null
var auth : FirebaseAuth? = null
var firestore : FirebaseFirestore? = null
class AddUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        // Firebase 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        addUser_iv_addPhoto.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }

        addUser_btn_upload.setOnClickListener {
            userUpload()
        }

    }

    // 사진첩에서 골라온 사진 ImageView에 바인딩
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                addUser_iv_addPhoto.setImageURI(photoUri)
            }else{
                finish()
            }
        }
    }

    fun userUpload() {
        // Storage에 사진 업로드
        var curuid = auth?.currentUser?.uid

        var storageRef = storage?.reference?.child("userProfiles")?.child(curuid!!)

        if (addUser_iv_addPhoto == null || addUser_et_userNickName == null){
            Toast.makeText(this, "정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
        }else{
            storageRef?.putFile(photoUri!!)?.addOnCompleteListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->

                    var userDTO = UserDTO()

                    userDTO.imageUrl = uri.toString()                                 // 회원 프사
                    userDTO.uid = curuid                              // 회원 uid
                    userDTO.userId = auth?.currentUser?.email                           // 회원 아이디
                    userDTO.userNickName = addUser_et_userNickName.text.toString()      // 회원 닉네임

                    firestore?.collection("users")?.document(curuid!!)?.set(userDTO)

                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

}