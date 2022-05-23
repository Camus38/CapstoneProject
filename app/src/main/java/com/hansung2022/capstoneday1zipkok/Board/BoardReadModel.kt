package com.hansung2022.capstoneday1zipkok.Board

class BoardReadModel(
    var uid: String = "",
    var nickname: String = "",
    var title: String = "",
    var msg: String = "",
    var createdAt: Long = 0,
    var imageCount : Int = 0,
    var imageNameList : MutableList<String> = mutableListOf()
)