package com.alchembright.dev.langtrackapp.screen.login

import androidx.lifecycle.ViewModel
import com.alchembright.dev.langtrackapp.data.Repository
import com.alchembright.dev.langtrackapp.data.model.User

class LoginViewModel(repo: Repository): ViewModel() {

    var mRepository: Repository = repo

    fun setCurrentUser(user: User){
        mRepository.setCurrentUser(user)
    }

    fun putDeviceToken(){
        mRepository.putDeviceToken()
    }
}