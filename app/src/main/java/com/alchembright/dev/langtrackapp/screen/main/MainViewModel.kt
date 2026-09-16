package com.alchembright.dev.langtrackapp.screen.main

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se
* */

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alchembright.dev.langtrackapp.data.Repository
import com.alchembright.dev.langtrackapp.data.model.Assignment
import com.alchembright.dev.langtrackapp.data.model.Survey
import com.alchembright.dev.langtrackapp.data.model.User

class MainViewModel(repo: Repository): ViewModel() {

    var mRepository: Repository = repo
    var surveyList = mutableListOf<Survey>()
    var surveyListLiveData = MutableLiveData<MutableList<Survey>>()
    var assignmentList = mutableListOf<Assignment>()
    var assignmentListLiveData = MutableLiveData<MutableList<Assignment>>()

    init {
        mRepository.assignmentListLiveData.observeForever {
            assignmentList = it
            assignmentListLiveData.value = it
        }
        /*mRepository.surveyListLiveData.observeForever {
            val activelist = mutableListOf<Survey>()
            val inactivelist = mutableListOf<Survey>()
            for (survey in it){
                /*if (survey.active){//TODO: check if active
                    activelist.add(survey)
                }else{
                    inactivelist.add(survey)
                }*/
            }
            val finallist = mutableListOf<Survey>()
            finallist.addAll(activelist)
            finallist.addAll(inactivelist)
            surveyList = finallist
            surveyListLiveData.value = surveyList
        }*/
    }

    fun surveyOpened(){
        mRepository.surveyOpened()
    }

    fun apiIsAlive(listener: (result: Boolean, theUrl: String?) -> Unit) {
        mRepository.apiIsAlive(listener)
    }

    fun clearAssignmentsList(){
        mRepository.emptyAssignmentsList()
    }

    fun postDeviceToken(){
        mRepository.putDeviceToken()
    }

    fun setSelectedAssignment(assignment: Assignment?){
        mRepository.selectedAssignment = assignment
    }

    fun setIdToken(token: String){
        mRepository.idToken = token
    }

    fun getUserLiveData(): MutableLiveData<User> {
        return mRepository.currentUserLiveData
    }

    fun setCurrentUser(user: User){
        mRepository.setCurrentUser(user)
    }

    fun getCurrentUser() : User{
        return mRepository.getCurrentUser()
    }

    fun getAssignments(){
        mRepository.getAssignments()
    }

    fun setStagingUrl(useStaging: Boolean){
        mRepository.setStagingUrl(useStaging)
    }

    fun isInStagingUrl(): Boolean{
        return mRepository.isInStaging()
    }

    fun getTeamUserNames(callback: (result: Map<String,String>) -> Unit){
        mRepository.getTeamUsernames(callback)
    }
}