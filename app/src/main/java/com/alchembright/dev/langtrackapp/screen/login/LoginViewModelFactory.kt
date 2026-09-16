package com.alchembright.dev.langtrackapp.screen.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alchembright.dev.langtrackapp.data.RepositoryFactory

class LoginViewModelFactory(var context: Context): ViewModelProvider.Factory {

    val mMainViewModel =  LoginViewModel(RepositoryFactory.getRepository(context))

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = LoginViewModel(RepositoryFactory.getRepository(context)) as T
}