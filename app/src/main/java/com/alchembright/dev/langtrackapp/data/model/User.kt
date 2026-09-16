package com.alchembright.dev.langtrackapp.data.model

class User {
    var id = ""
    var userName = ""
    var userEmail = ""


    constructor(id: String = "", name: String = "", mail: String = ""){
        this.id = id
        this.userName = name
        this.userEmail = mail
    }
}