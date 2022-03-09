package com.example.meowcat.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.meowcat.MessageActivity
import com.example.meowcat.R
import com.example.meowcat.navigation.model.ChatDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_message_fragment.view.*
import kotlinx.android.synthetic.main.item_chat_list.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageListFragment : Fragment(){

    val uid : String = FirebaseAuth.getInstance().currentUser!!.uid
    var chatRoomUids : ArrayList<String> = arrayListOf()
    var chatDTOs : ArrayList<ChatDTO> = arrayListOf()
    var messageDTOs : ArrayList<ChatDTO.MessageDTO> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_message_fragment, container, false)


        CoroutineScope(Dispatchers.IO).launch {
            var getChatRoomUid = async { setChatRoomUid() }.await()
            var getLastMessage = async { setLastMessage(getChatRoomUid) }.await()


            withContext(Dispatchers.Main){
                Log.d("ㅎㅇㅎㅇ0", "어댑터 연결 전")
                view.messageListFragment_recyclerView.adapter = MessageListRecyclerView(getLastMessage)
                Log.d("ㅎㅇㅎㅇ0", "어댑터 연결 후")
                view.messageListFragment_recyclerView.layoutManager = LinearLayoutManager(activity)
                Log.d("ㅎㅇㅎㅇ0", "레이아웃 연결 후")

            }
        }

        return view
    }

    suspend fun setChatRoomUid() : ArrayList<String>{
        chatRoomUids.clear()
        chatDTOs.clear()
        var documents = FirebaseFirestore.getInstance().collection("chatRooms").whereArrayContains("users",uid).get().await().documents
        for (snapshot in documents){
            Log.d("ㅎㅇㅎㅇ", snapshot.id)
            chatRoomUids.add(snapshot.id)
            Log.d("ㅎㅇㅎㅇ", snapshot.data!!["users"].toString())
            chatDTOs.add(snapshot.toObject(ChatDTO::class.java)!!)
        }
        Log.d("ㅎㅇㅎㅇ", "이름 삭제 전")
        for (i in 0 until chatRoomUids.size){
            chatDTOs[i].users.remove(uid)
            Log.d("ㅎㅇㅎㅇ", chatDTOs[i].toString())
        }
        Log.d("ㅎㅇㅎㅇ", "이름 삭제 후")
        Log.d("ㅎㅇㅎㅇchatDTOs 완결", chatDTOs.toString())
        return chatRoomUids
    }

    suspend fun setLastMessage(chatRoomUid : ArrayList<String>) : ArrayList<ChatDTO.MessageDTO>{
        messageDTOs.clear()
        for (i in 0 until chatRoomUid.size){
            var documents = FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomUid[i]).collection("comments")
                .orderBy("timestamp",Query.Direction.DESCENDING).limit(1).get().await().documents
            for (snapshot in documents){
                messageDTOs.add(snapshot.toObject(ChatDTO.MessageDTO::class.java)!!)
                Log.d("ㅎㅇㅎㅇmessageDTOs 단건조회", messageDTOs.toString())
            }
        }
        Log.d("ㅎㅇㅎㅇmessageDTOs 완료", messageDTOs.toString())
        return messageDTOs
    }

    inner class MessageListRecyclerView(lastMessageDTO : ArrayList<ChatDTO.MessageDTO>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var lastMessage : ArrayList<ChatDTO.MessageDTO> = arrayListOf()

        init {
            lastMessage = lastMessageDTO
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
            // 마지막 메시지
            viewHolder.item_chat_list_lastMessage.text = lastMessage[position].message

            val dateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm", Locale("ko","KR"))
            val sendTime = dateFormat.format(Date(lastMessage[position].timestamp!!))

            viewHolder.item_chat_list_timeStamp.text = sendTime

            // 해당영역 클릭시 체팅방 입장
            viewHolder.setOnClickListener {
                var intent = Intent(activity, MessageActivity::class.java)
                intent.putExtra("destinationUid", chatDTOs[position].users[0])
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return chatRoomUids.size
        }

    }
}