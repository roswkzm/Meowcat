package com.example.meowcat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.meowcat.navigation.model.AccountActivity
import com.example.meowcat.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentActivity : AppCompatActivity() {

    var contentUid : String? = null
    var destinationUid : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")

        comment_recyclerView.adapter = CommentRecyclerViewAdapter()
        comment_recyclerView.layoutManager = LinearLayoutManager(this)

        comment_btn_send.setOnClickListener {
            var comment = ContentDTO.Comment()

            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.comment = comment_et_message.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments")
                .document().set(comment)

            comment_et_message.setText("")
        }
    }

    inner class CommentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var comments : ArrayList<ContentDTO.Comment> = arrayListOf()

        // init?????? ????????? addsnapshot?????? ?????? ????????? ????????? ?????? bindViewHolder?????? ??????
        init {
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments").orderBy("timestamp",Query.Direction.ASCENDING)
                .addSnapshotListener { value, error ->
                    comments.clear()
                    if(value == null) return@addSnapshotListener

                    // snapshot??? documents ????????? ?????? ????????????
                    for (snapshot in value.documents){
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)       // for????????? ???????????? comments??? ??????
                    }
                    notifyDataSetChanged()  // ?????? ??????
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.commentItem_tv_message.text = comments[position].comment

            FirebaseFirestore.getInstance().collection("users").document(comments[position].uid!!).get()
                .addOnCompleteListener { value ->
                    if (value.isSuccessful){
                        var url = value.result!!["imageUrl"]
                        Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentItem_iv_profile)
                        view.commentItem_tv_userNickName.text = value.result!!["userNickName"].toString()
                        view.commentItem_iv_profile.setOnClickListener {
                            var intent = Intent(this@CommentActivity, AccountActivity::class.java)
                            intent.putExtra("destinationUid", value.result!!["uid"].toString())
                            startActivity(intent)
                        }
                    }
                }
        }

        override fun getItemCount(): Int {
            return comments.size
        }

    }
}