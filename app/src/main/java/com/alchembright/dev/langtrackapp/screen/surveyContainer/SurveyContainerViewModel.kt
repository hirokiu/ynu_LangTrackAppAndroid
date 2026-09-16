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

import androidx.lifecycle.ViewModel
import com.alchembright.dev.langtrackapp.data.Repository
import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.data.model.User

class SurveyContainerViewModel (private var repo: Repository): ViewModel() {

    fun getCurrentUser() : User {
        return repo.getCurrentUser()
    }

    fun postAnswer(answers: Map<Int, Answer>){
        repo.postAnswer(answers)
    }
}