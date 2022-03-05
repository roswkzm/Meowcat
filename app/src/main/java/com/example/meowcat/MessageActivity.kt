package com.example.meowcat

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.meowcat.navigation.model.ChatDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_message.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log

class MessageActivity : AppCompatActivity() {

    var destinationUid : String? = null
    var uid : String? = null
    var chatRoomUid : String? = null
    var messages : ArrayList<ChatDTO.MessageDTO> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        // 지금 내가 대화를 건 상대(대화를 당하는 아이디)
        destinationUid = intent.getStringExtra("destinationUid")    // 오바마의 uid
        // 대화 신청을 건 아이디(대화를 요규한 아이디)
        uid = FirebaseAuth.getInstance().currentUser?.uid

        // 누구와 채팅을 하는지 보여줄 imageView와 이름 출력하기
        FirebaseFirestore.getInstance().collection("users").document(destinationUid!!).get().addOnCompleteListener { value ->
            if (value.isSuccessful){
                var url = value.result.data!!["imageUrl"]
                // 누구와 채팅을 하는지 보여줄 imageView와 이름 출력하기
                Glide.with(this).load(url).apply(RequestOptions().circleCrop()).into(messageActivity_userImage)
                messageActivity_userName.text = "${value.result.data!!["userNickName"]}님과의 대화"
            }
        }

        checkChatRoom()     // 방이 있는지 없는지 체크한다.

        messageActivity_btn_send.setOnClickListener {       // 전송버튼 누를시
            var chatDTO : ChatDTO = ChatDTO()
            chatDTO.users.add(destinationUid!!)
            chatDTO.users.add(uid!!)

            if (chatRoomUid == null){       // 만약 방이 없는 상태라면 방을 생성한다.
                Log.d("ㅎㅇㅎㅇ이프문 시작", chatRoomUid.toString())
                FirebaseFirestore.getInstance().collection("chatRooms").document().get().addOnSuccessListener { task ->
                    chatRoomUid = task.id
                    Log.d("ㅎㅇㅎㅇ", chatRoomUid!!)
                    FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomUid!!).set(chatDTO)
                    setResult(Activity.RESULT_OK)
                    Log.d("ㅎㅇㅎㅇ이프문 끝", chatRoomUid.toString())
                    checkChatRoom()     // 다시 방이 있는지 검사한다.
                }
            }else{      // 만약 방이 이미 있었다면 메시지를 보낸다.
                if (messageActivity_et_message.text.toString() == ""){  // 메시지가 빈칸이라면
                    Toast.makeText(this,"메시지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }else{
                    var messageDTO = ChatDTO.MessageDTO()
                    messageDTO.uid = uid
                    messageDTO.message = messageActivity_et_message.text.toString()
                    messageDTO.timestamp = System.currentTimeMillis()

                    FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomUid!!).collection("comments").document().set(messageDTO)
                    // 메시지 전송후 text 비우기
                    messageActivity_et_message.setText("")
                }
            }
        }
    }

    fun checkChatRoom() {
        // 이부분에서 users에 uid와 destination이 둘다 일치해야 하는데 whereArrayContains 함수는 or절로만 지원해서 둘중에 하나라도 있으면 연결된다
        // 문제 해결방안 -> DB에서 Data를 가져올때 2가지를 비교해야한다. uid & destinationUid
        // 먼저 uid가 포함되어있는걸 찾은 후에 정보를 가져오고 그중에서 destinationUid가 포함되있는것만 고르는 로직으로 변경
        FirebaseFirestore.getInstance().collection("chatRooms").whereArrayContains("users",uid!!)
            .get().addOnCompleteListener { value ->
                if (value.isSuccessful){
                    for (document in value.result) {
                        if (document.data["users"].toString().contains(destinationUid!!)) {
                            chatRoomUid = document.id
                            Log.d("ㅎㅇㅎㅇ2222", document.data["users"].toString().contains(destinationUid!!).toString())
                            Log.d("ㅎㅇㅎㅇ체크쳇룸", chatRoomUid.toString())
                            Log.d("ㅎㅇㅎㅇ", "어댑터 연결 전")
                            messageActivity_recyclerView.adapter = MessageRecyclerViewAdapter()
                            Log.d("ㅎㅇㅎㅇ", "어댑터 연결 후")
                            messageActivity_recyclerView.layoutManager = LinearLayoutManager(this)
                            Log.d("ㅎㅇㅎㅇ", "레이아웃 매니저 연결 후 ")
                        }
                    }
                }
            }
    }

    inner class MessageRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        init {
            FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomUid!!).collection("comments").orderBy("timestamp",Query.Direction.ASCENDING).addSnapshotListener { value, error ->
                if (value == null) return@addSnapshotListener
                messages.clear()
                for (snapshot in value.documents){
                    messages.add(snapshot.toObject(ChatDTO.MessageDTO::class.java)!!)
                }
                notifyDataSetChanged()
                messageActivity_recyclerView.scrollToPosition(messages.size-1)      // 메시지가 들어오면 그 메시지를 바로 볼수있도록함
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_message,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = holder.itemView

            // 내가 보낸 메시지일 경우
            if (messages[position].uid == uid){
                viewHolder.messageItem_textView_message.text = messages[position].message
                viewHolder.messageItem_textView_message.setBackgroundResource(R.drawable.rightbubble)
                viewHolder.messageItem_linearlayout_destination.visibility = View.INVISIBLE
                viewHolder.messageItem_linearlayout_main.gravity = Gravity.RIGHT    // 내가보낸말 우측정렬
                // 몇명읽었는지 구현해야함
            }else{  // 상대방이 보낸 메시지일 경우
                // 보낸사람 프사와 이름 설정
                FirebaseFirestore.getInstance().collection("users").document(messages[position].uid!!).get()
                    .addOnCompleteListener { value ->
                        if (value.isSuccessful){
                            var url = value.result!!["imageUrl"]
                            Glide.with(viewHolder.context).load(url).apply(RequestOptions().circleCrop()).into(viewHolder.messageItem_imageview_profile)
                            viewHolder.messageItem_textview_name.text = value.result!!["userNickName"].toString()
                        }
                    }
                viewHolder.messageItem_textView_message.text = messages[position].message
                viewHolder.messageItem_textView_message.setBackgroundResource(R.drawable.leftbubble)
                viewHolder.messageItem_linearlayout_destination.visibility = View.VISIBLE
                viewHolder.messageItem_linearlayout_main.gravity = Gravity.LEFT    // 상대가 보낸말 좌측정렬
                // 몇명읽었는지 구현해야함
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm", Locale("ko","KR"))
            val sendTime = dateFormat.format(Date(messages[position].timestamp!!))
            // 메시지 보낸 시간
            viewHolder.messageItem_textview_timestamp.text = sendTime

            // 메시지의 가독성을 높이기 위해 message가 5개이상으로 쌓이게되면 키보드에 맞게 latout을 밀어 가려지는 부분이 없도록 만든다.
            if (messages.size > 5){
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            }
        }

        override fun getItemCount(): Int {
            return messages.size
        }

    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}