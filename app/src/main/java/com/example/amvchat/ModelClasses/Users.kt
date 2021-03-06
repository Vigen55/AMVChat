package com.example.amvchat.ModelClasses

class Users {

    private var uid:String = ""
    private var username:String = ""
    private var profile:String = ""
    private var cover:String = ""
    private var status:String = ""
    private var search:String = ""
    private var facebook:String = ""
    private var instagram:String = ""
    private var website:String = ""

    constructor()
    constructor(
        uid: String,
        username: String,
        profile: String,
        cover: String,
        status: String,
        search: String,
        facebook: String,
        instagram: String,
        website: String
    ) {
        this.uid = uid
        this.username = username
        this.profile = profile
        this.cover = cover
        this.status = status
        this.search = search
        this.facebook = facebook
        this.instagram = instagram
        this.website = website
    }

    fun getUID():String?{
        return uid
    }

    fun  setUID(uid:String){
        this.uid = uid
    }
    fun getUserName():String?{
        return username
    }

    fun  setUserName(username:String){
        this.username = username
    }
    fun getProfile():String?{
        return profile
    }

    fun  setProfile(profile:String){
        this.profile = profile
    }
    fun getcover():String?{
        return cover
    }

    fun  setcover(cover:String){
        this.cover = cover
    }
    fun getstatus():String?{
        return status
    }

    fun  setstatus(status:String){
        this.status = status
    }
    fun getsearch():String?{
        return search
    }

    fun  setsearch(search:String){
        this.search = search
    }
    fun getfacebook():String?{
        return facebook
    }

    fun  setfacebook(facebook:String){
        this.facebook = facebook
    }
    fun getinstagram():String?{
        return instagram
    }

    fun  setinstagram(instagram:String){
        this.instagram = instagram
    }
     fun getwebsite():String?{
        return website
    }

    fun  setwebsite(website:String){
        this.website = website
    }




}