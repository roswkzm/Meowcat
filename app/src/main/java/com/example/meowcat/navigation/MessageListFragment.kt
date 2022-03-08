package com.example.meowcat.navigation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.meowcat.R
import com.example.meowcat.navigation.model.ChatDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_message_fragment.*
import kotlinx.android.synthetic.main.activity_message_fragment.view.*
import kotlinx.android.synthetic.main.item_chat_list.view.*
import kotlinx.android.synthetic.main.item_message.view.*
import kotlin.math.log

class MessageListFragment : Fragment(){

    val uid : String = FirebaseAuth.getInstance().currentUser!!.uid
    var chatRoomUid : ArrayList<String> = arrayListOf()
    var destinationUsers : ArrayList<String> = arrayListOf()
    var chatDTOs : ArrayList<ChatDTO> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_message_fragment, container, false)

        getChatRoomList()

        return view
    }

    fun getChatRoomList(){
        FirebaseFirestore.getInstance().collection("chatRooms").whereArrayContains("users", uid).addSnapshotListener { value, error ->
            if (value == null) return@addSnapshotListener
            for (snapshot in value.documents){
                chatRoomUid.add(snapshot.id)
                chatDTOs.add(snapshot.toObject(ChatDTO::class.java)!!)  // 유저 정보를 담는다
            }
            for (i in 0 until value.documents.size){
                chatDTOs[i].users.remove(uid)       // 유저 정보에서 내정보를 지우니 상대방이 누군지 판단 가능하게 함
                Log.d("ㅎㅇㅎㅇ0", chatDTOs[i].users.toString())
            }
            Log.d("ㅎㅇㅎㅇ0", "어댑터 연결 전")
            messageListFragment_recyclerView.adapter = MessageListRecyclerViewAdapter()
            Log.d("ㅎㅇㅎㅇ0", "어댑터 연결 후")
            messageListFragment_recyclerView.layoutManager = LinearLayoutManager(activity)
            Log.d("ㅎㅇㅎㅇ0", "레이아웃 연결 후")
        }
    }

    inner class MessageListRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        init {
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_list,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = holder.itemView

            // 보낸사람 프사와 이름 설정
            FirebaseFirestore.getInstance().collection("users").document(chatDTOs[position].users[0]).get()
                .addOnCompleteListener { value ->
                    if (value.isSuccessful){
                        var url = value.result!!["imageUrl"]
                        Glide.with(viewHolder.context).load(url).apply(RequestOptions().circleCrop()).into(viewHolder.item_chat_list_userProfile)
                        viewHolder.item_chat_list_userNickName.text = value.result!!["userNickName"].toString()
                    }
                }
        }

        override fun getItemCount(): Int {
            return chatRoomUid.size
        }

    }
}