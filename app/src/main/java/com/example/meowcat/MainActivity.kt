package com.example.meowcat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.meowcat.navigation.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    var waitTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        bottom_navigation.selectedItemId = R.id.action_home
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setToolbarDefault()
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
                supportFragmentManager.beginTransaction().replace(R.id.main_content, MessageFragment()).commit()
                return true
            }
            R.id.action_account -> {
                //자신의 account인지 알아보기 위한 uid 넘기기
                var bundle = Bundle()
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid", uid)
                AccountFragment().arguments = bundle

                supportFragmentManager.beginTransaction().replace(R.id.main_content, AccountFragment()).commit()
                return true
            }
        }
        return false
    }

    fun setToolbarDefault(){
        toolbar_btn_back.visibility = View.GONE
        toolbar_username.visibility = View.GONE
        toolbar_title_image.visibility = View.VISIBLE
        btn_addProduct.visibility = View.GONE
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