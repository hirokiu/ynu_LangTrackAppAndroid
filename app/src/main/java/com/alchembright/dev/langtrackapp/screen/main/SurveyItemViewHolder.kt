package com.alchembright.dev.langtrackapp.screen.main

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se
* */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Assignment
import com.alchembright.dev.langtrackapp.interfaces.OnSurveyRowClickedListener
import com.alchembright.dev.langtrackapp.util.formatToReadable
import com.alchembright.dev.langtrackapp.util.toDate

class SurveyItemViewHolder(theItemView: View,
                           onRowClickedListener: OnSurveyRowClickedListener
): RecyclerView.ViewHolder(theItemView) {

    private var task: TextView = itemView.findViewById(R.id.surveyRecyclerTitleTextView)
    private var date: TextView = itemView.findViewById(R.id.surveyRecyclerDateTextView)
    private var answeredTextView: TextView = itemView.findViewById(R.id.surveyRecyclerAnsweredTextView)
    private var cellLayout: ConstraintLayout = itemView.findViewById(R.id.surveyCellLayout)
    private var indicator: View = itemView.findViewById(R.id.surveyRecyclerIndicatorView)
    private lateinit var item: Assignment

    init {
        theItemView.setOnClickListener { onRowClickedListener.rowClicked(item) }
    }

    fun bind(item: Assignment, pos: Int){
        this.item = item
        task.text = this.item.survey.title
        date.text = item.publishAt.toDate()?.formatToReadable(date.context.getString(R.string.dateFormat)) ?: date.context.getString(R.string.noDate)
        if (item.dataset != null) {
            answeredTextView.text = answeredTextView.context.getString(R.string.answered)
            indicator.background = indicator.context.getDrawable(R.drawable.recycler_indicator_background_green)
        }
            else {
            answeredTextView.text = answeredTextView.context.getString(R.string.unanswered)
            indicator.background = indicator.context.getDrawable(R.drawable.recycler_indicator_background_gray)
        }
    }

    fun getItem(): Assignment {
        return item
    }


    companion object {
        fun newInstance(parent: ViewGroup,
                        onRowClickedListener: OnSurveyRowClickedListener):SurveyItemViewHolder{
            return SurveyItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.recycle_surveyitem_row,
                    parent,
                    false
                ),
                onRowClickedListener
            )
        }
    }
}