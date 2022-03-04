package com.example.meowcat

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.meowcat.navigation.model.ChatDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_message.*
import kotlin.math.log

class MessageActivity : AppCompatActivity() {

    var destinationUid : String? = null
    var uid : String? = null
    var chatRoomUid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        // 지금 내가 대화를 건 상대(대화를 당하는 아이디)
        destinationUid = intent.getStringExtra("destinationUid")
        // 대화 신청을 건 아이디(대화를 요규한 아이디)
        uid = FirebaseAuth.getInstance().currentUser?.uid

        Log.d("ㅎㅇㅎㅇ체크쳇룸시작", chatRoomUid.toString())
        checkChatRoom()     // 방이 있는지 없는지 체크한다.
        Log.d("ㅎㅇㅎㅇ체크쳇룸끝", chatRoomUid.toString())

        messageActivity_btn_send.setOnClickListener {       // 전송버튼 누를시
            var chatDTO : ChatDTO = ChatDTO()
            chatDTO.users.add(destinationUid!!)
            chatDTO.users.add(uid!!)

            if (chatRoomUid == null){       // 만약 방이 없는 상태라면
                Log.d("ㅎㅇㅎㅇ이프문 시작", chatRoomUid.toString())
                messageActivity_btn_send.isEnabled = false
                FirebaseFirestore.getInstance().collection("chatRooms").document().get().addOnSuccessListener { task ->
                    chatRoomUid = task.id
                    Log.d("ㅎㅇㅎㅇ", chatRoomUid!!)
                    FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomUid!!).set(chatDTO)
                    setResult(Activity.RESULT_OK)
                    Log.d("ㅎㅇㅎㅇ이프문 끝", chatRoomUid.toString())
                }
            }else{      // 만약 방이 이미 있었다면
                Log.d("ㅎㅇㅎㅇ엘스문 시작", chatRoomUid.toString())
                var messageDTO = ChatDTO.MessageDTO()
                messageDTO.uid = uid
                messageDTO.message = messageActivity_et_message.text.toString()
                messageDTO.timestamp = System.currentTimeMillis()

                FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomUid!!).collection("comments").document().set(messageDTO)

                messageActivity_et_message.setText("")
                Log.d("ㅎㅇㅎㅇ엘스문 끝", chatRoomUid.toString())
            }
        }

    }

    fun checkChatRoom() {
        FirebaseFirestore.getInstance().collection("chatRooms").whereArrayContainsAny("users", listOf(uid!!, destinationUid!!))
            .get().addOnCompleteListener { value ->
                if (value.isSuccessful){
                    for (document in value.result) {
                        chatRoomUid = document.id
                        Log.d("ㅎㅇㅎㅇ체크쳇룸", chatRoomUid.toString())
                    }
                }
            }
    }

//    inner class MessageRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
//        var messageRef = FirebaseFirestore.getInstance().collection("chatRooms").document().collection("comments").document()
//        var userRef = FirebaseFirestore.getInstance().collection("chatRooms").document().collection("users")
//
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_message,parent,false)
//            return CustomViewHolder(view)
//        }
//
//        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//
//        }
//
//        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//
//        }
//
//        override fun getItemCount(): Int {
//
//        }
//
//    }
}