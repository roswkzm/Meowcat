package com.example.meowcat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        btn_register.setOnClickListener {
            try {
                auth?.createUserWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())?.addOnCompleteListener {
                    task ->
                    if (task.isSuccessful){
//                        // 이메일 링크 인증
//                        auth?.currentUser
//                            ?.sendEmailVerification()
//                            ?.addOnCompleteListener { verifiTask ->
//                                if (verifiTask.isSuccessful) {
//                                    Toast.makeText(this, "메일로 인증링크를 발송하였습니다.", Toast.LENGTH_SHORT).show()
//                                    var intent = Intent(this, LoginActivity::class.java)
//                                    startActivity(intent)
//                                    finish()
//                                } else {
//                                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
//                                }
//                            }
                    }else if(task.exception?.message.isNullOrEmpty()){
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e:Exception){
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

}