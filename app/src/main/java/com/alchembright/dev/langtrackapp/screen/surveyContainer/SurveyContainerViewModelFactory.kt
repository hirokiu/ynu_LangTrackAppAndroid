package com.alchembright.dev.langtrackapp.screen.surveyContainer

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se

* Viktor Czyżewski
* RSE Team
* University of York
* */

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alchembright.dev.langtrackapp.data.RepositoryFactory

class SurveyContainerViewModelFactory(var context: Context): ViewModelProvider.Factory {

    private val mMainViewModel =  SurveyContainerViewModel(RepositoryFactory.getRepository(context))

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = mMainViewModel as T
}