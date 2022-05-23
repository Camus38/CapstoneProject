package com.hansung2022.capstoneday1zipkok.Tip

data class TipModel(
    var content: String ="", //대분류
    var middleCategory : String = "", //중분류
    var title: String = "", //제목

    var webUrl : String = "", //웹 페이지 주소
    var imageUrl: String = "", //섬네일 웹 주소



)