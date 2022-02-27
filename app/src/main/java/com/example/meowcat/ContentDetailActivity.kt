package com.example.meowcat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.meowcat.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_content_detail.*
import kotlinx.android.synthetic.main.activity_pick_product.*

class ContentDetailActivity : AppCompatActivity() {
    var destinationUid : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_detail)

        destinationUid = intent.getStringExtra("destinationUid")

        val adapter = ViewpagerAdapter(destinationUid!!)

        contentDetail_viewpager.offscreenPageLimit = 1      // 현재 페이지를 기준으로 얼마만큼 떨어진 page를 미리 생성할것인가
        contentDetail_viewpager.adapter = adapter

        // 페이지 이동 애니메이션
        var transform = CompositePageTransformer()
        transform.addTransformer(MarginPageTransformer(8))

        transform.addTransformer(ViewPager2.PageTransformer{ view: View, fl: Float ->
            var v = 1-Math.abs(fl)
            view.scaleY = 0.8f + v * 0.2f
        })
        contentDetail_viewpager.setPageTransformer(transform)

    }
}