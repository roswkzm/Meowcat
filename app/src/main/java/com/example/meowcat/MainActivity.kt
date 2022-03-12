package com.example.meowcat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.meowcat.navigation.*
import com.example.meowcat.navigation.model.AccountActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    var waitTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        bottom_navigation.selectedItemId = R.id.action_home

        val auth = FirebaseAuth.getInstance()


        FirebaseFirestore.getInstance().collection("users").document(auth.currentUser!!.uid).get().addOnCompleteListener { task ->
            if(task.isSuccessful){
                if (task.result.exists()){
                    return@addOnCompleteListener
                }else{
                    var intent = Intent(this, AddUserActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_home -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_content, HomeFragment()).commit()
                return true
            }
            R.id.action_search -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_content, SearchFragment()).commit()
                return true
            }
            R.id.action_favorite -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_content, FavoriteFragment()).commit()
                return true
            }
            R.id.action_message -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_content, MessageListFragment()).commit()
                return true
            }
            R.id.action_account -> {
                var intent = Intent(this, AccountActivity::class.java)
                intent.putExtra("destinationUid", FirebaseAuth.getInstance().currentUser?.uid)
                startActivity(intent)
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        if(System.currentTimeMillis() - waitTime >=1500 ) {
            waitTime = System.currentTimeMillis()
            Toast.makeText(this,"뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        } else {
            finish() // 액티비티 종료
        }
    }
}