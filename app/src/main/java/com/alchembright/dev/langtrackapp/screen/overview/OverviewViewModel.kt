package com.alchembright.dev.langtrackapp.screen.overview

import androidx.lifecycle.ViewModel
import com.alchembright.dev.langtrackapp.data.Repository

class OverviewViewModel(repo: Repository): ViewModel() {
    var mRepository: Repository = repo

}