package com.example.meowcat

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class LoginActivity : AppCompatActivity() {

    var auth : FirebaseAuth? = null
    var callbackManager : CallbackManager? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // 회원가입 버튼 클릭시
        btn_register.setOnClickListener {
            clickRegister()
        }

        // 이메일 로그인 버튼 클릭시
        btn_login.setOnClickListener {
            clickLogin()
        }

        // 구글 로그인
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("289493244962-fjvpq2s2jlv0hmh237k2h4m5qm0deg4p.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        // 구글 로그인 버튼 클릭시
        btn_google_login.setOnClickListener {
            googleLogin()
        }

        callbackManager = CallbackManager.Factory.create()

        // 페이스북 로그인 버튼 클릭시
        btn_facebook_login.setOnClickListener {
            facebookLogin()
        }
    }

    // facebook 로그인할 때 hash Key 값을 확인하는 함
    //EcAVbaHgZgQrXJBJP2XazZ86mAY=
    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if (result!!.isSuccess){
                var account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }

    fun facebookLogin(){
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccessToken(result?.accessToken)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }

        })
    }

    fun handleFacebookAccessToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)?.addOnCompleteListener {
                task ->
            if (task.isSuccessful){
                moveMainPage(task.result.user)
            }else{
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 구글 로그인 버튼 누를시
    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    // 구글정보를 firebase에 저장
    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener {
                task ->
            if (task.isSuccessful){
                moveMainPage(task.result.user)
            }else{
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
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
                // 만약 이메일 링크 인증을 한 회원일 경우
                if (auth?.currentUser?.isEmailVerified!!) {
                    moveMainPage(task.result.user)
                } else {
                    Toast.makeText(this, "${et_email.text.toString()}에 접속하여 인증을 해주세요.", Toast.LENGTH_SHORT).show()
                }
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

//    override fun onStart() {
//        super.onStart()
//        moveMainPage(auth?.currentUser)
//    }
}