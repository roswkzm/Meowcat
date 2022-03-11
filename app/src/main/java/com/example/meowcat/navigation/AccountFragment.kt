package com.example.meowcat.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.meowcat.AddPhotoActivity
import com.example.meowcat.LoginActivity
import com.example.meowcat.MainActivity
import com.example.meowcat.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_account_fragment.*
import kotlinx.android.synthetic.main.activity_account_fragment.view.*
import kotlinx.android.synthetic.main.activity_main.*

class AccountFragment : Fragment(){

    var destinationUid : String? = null
    var currentUid : String ? = null
    var auth : FirebaseAuth? = null
    val adminUid : String = "wJNThceh9nb6jdUjbgWNjsfrnX42"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_account_fragment, container, false)

        destinationUid = arguments?.getString("destinationUid")    // AccountFragment로 이동할 때 받아온 대상의 uid
        auth = FirebaseAuth.getInstance()
        currentUid = auth?.currentUser?.uid     // 내 uid

        if(currentUid == adminUid){
            var mainActivity = (activity as MainActivity)
            mainActivity.btn_addProduct.visibility = View.VISIBLE
            mainActivity.btn_addProduct.setOnClickListener {
                var intent = Intent(context, AddPhotoActivity::class.java)
                startActivity(intent)
            }
        }

        view.btn_logout.setOnClickListener {
            var intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            auth?.signOut()
        }
        return view
    }
}