package com.example.shunyachatstuff

data class ChatModel (
    var viewType : Int,
    var duration : String,
    var seekPos : Int,
    var message : String,
    var file : String,
    var wasPlaying : Boolean
)


