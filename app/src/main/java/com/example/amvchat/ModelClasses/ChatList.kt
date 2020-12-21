package com.example.amvchat.ModelClasses

class ChatList {

    private var id:String = ""

    constructor()
    constructor(id: String) {
        this.id = id
    }

    fun getid():String?{
        return id
    }

    fun  id(id:String){
        this.id = id
    }

}