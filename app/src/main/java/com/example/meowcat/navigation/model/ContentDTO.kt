package com.example.meowcat.navigation.model

data class ContentDTO(      // 게시글 DTO
    var imageUrl : String? = null,      // 사진 주소
    var productName : String? = null,   // 고양이 이름
    var productGender : String? = null, // 고양이 성별
    var productType : String? = null,   // 고양이 품종
    var productExplain : String? = null,    // 고양이 설명
    var uid : String? = null,           // 판매자 uid
    var userId : String? = null,        // 판매자 Email
    var timestamp : Long? = null,       // 상품등록시간
    var favoriteCount : Int = 0,        // 상품 좋아요 갯수
    var favorites : MutableMap<String, Boolean> = HashMap()     // 상품 좋아요 중복 방지

)
{
    // 회원 덧글 DTO
    data class Comment(
        var uid : String? = null,
        var userId : String? = null,
        var comment : String? = null,
        var timestamp : Long? = null
    )
}