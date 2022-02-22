package com.example.meowcat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        btn_register.setOnClickListener {
            clickRegister()
        }

        btn_login.setOnClickListener {
            clickLogin()
        }
    }

    //회원가입 버튼 누를시
    fun clickRegister(){
        var intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    fun clickLogin(){
        auth?.signInWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())?.addOnCompleteListener {
            task ->
            if (task.isSuccessful){
                moveMainPage(task.result.user)
            }else{
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun moveMainPage(user: FirebaseUser?) {
        if (user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}