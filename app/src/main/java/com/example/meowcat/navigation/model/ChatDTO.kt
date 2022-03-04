package com.example.meowcat.navigation.model

import java.util.HashMap

data class ChatDTO(
    var users: ArrayList<String> = arrayListOf()
)
{
    data class MessageDTO(
    var uid : String? = null,
    var message : String? = null,
    var timestamp : Long? = null
    )
}