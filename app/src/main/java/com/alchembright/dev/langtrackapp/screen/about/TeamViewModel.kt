package com.alchembright.dev.langtrackapp.screen.about

import androidx.lifecycle.ViewModel
import com.alchembright.dev.langtrackapp.data.Repository
import com.alchembright.dev.langtrackapp.data.model.ContactInfo
import com.alchembright.dev.langtrackapp.data.model.TeamMember

class TeamViewModel(repo: Repository): ViewModel(){

    var mRepository: Repository = repo

    fun getContactInfo(callback: (result: List<ContactInfo>) -> Unit) {
        mRepository.getContactInfo(callback)
    }

    fun getTeamsText(callback: (result: List<TeamMember>) -> Unit) {
        mRepository.getTeamsText(callback)
    }
}