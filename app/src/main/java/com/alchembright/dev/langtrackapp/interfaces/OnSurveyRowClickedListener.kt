package com.alchembright.dev.langtrackapp.interfaces

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se

* Viktor Czyżewski
* RSE Team
* University of York
* */

import com.alchembright.dev.langtrackapp.data.model.Assignment

interface OnSurveyRowClickedListener {
    fun rowClicked(item: Assignment)
}