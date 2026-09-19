package com.alchembright.dev.langtrackapp.screen.main

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se

* Viktor Czyżewski
* RSE Team
* University of York
* */

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alchembright.dev.langtrackapp.data.model.Assignment
import com.alchembright.dev.langtrackapp.data.model.Survey
import com.alchembright.dev.langtrackapp.interfaces.OnExpiredListener
import com.alchembright.dev.langtrackapp.interfaces.OnSurveyRowClickedListener
import kotlin.collections.ArrayList

class SurveyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val EXPIRED_ASSIGNMENT = 0
    private val ACTIVE_ASSIGNMENT = 1
    private val STATISTICS = 2
    private val STATISTICS_VIEW = "statistics"
    private lateinit var onRowClickedListener: OnSurveyRowClickedListener
    //private var items: List<Survey> = ArrayList()
    private var items: List<Assignment> = ArrayList()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return when (p1){
            ACTIVE_ASSIGNMENT -> ActiveViewHolder.newInstance(p0,onRowClickedListener)
            STATISTICS -> StatisticsViewHolder.newInstance(p0)
            else -> SurveyItemViewHolder.newInstance(p0,onRowClickedListener)
        }

    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is ActiveViewHolder){
            holder.removeCallbacks()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getTask(position: Int): Assignment {
        return items[position]
    }

    fun getNumberOfAnswered(): Int = (items.filter { it.dataset != null }).size

    fun getNumberOfUnanswered(): Int {
        val count = (items.filter { it.dataset == null }).size - 1
        return if (count <= 0)  0 else count
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is ActiveViewHolder){
            holder.removeCallbacks()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val assignment = items[position]
        if (holder !is StatisticsViewHolder) holder.itemView.setBackgroundColor(
            androidx.core.content.ContextCompat.getColor(holder.itemView.context,
                if (position % 2 == 0) com.alchembright.dev.langtrackapp.R.color.kirokun_surface else com.alchembright.dev.langtrackapp.R.color.kirokun_surface_alternate))
        when(holder){
            is ActiveViewHolder -> {
                holder.bind(assignment, object: OnExpiredListener{
                    override fun assignmentExpired() {
                        notifyDataSetChanged()
                    }
                })
                holder.setCallbacks()
            }
            is SurveyItemViewHolder -> {
                holder.bind(assignment)
            }
            is StatisticsViewHolder -> {
                holder.bind(getNumberOfAnswered(),getNumberOfUnanswered())
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        val theAssignment = getTask(position)
        return when {
            theAssignment.createdAt == STATISTICS_VIEW -> {
                STATISTICS
            }
            theAssignment.isActive() -> {
                ACTIVE_ASSIGNMENT
            }
            else -> {
                EXPIRED_ASSIGNMENT
            }
        }
    }


    fun setAssignments(items: List<Assignment>){
        val activeAssignment = items.filter { it.isActive() }
        if (items.isNotEmpty() &&
            (items.filter { it.createdAt == STATISTICS_VIEW }).isEmpty()){
            val statisticsAssignment = Assignment(
                survey = Survey(
                    name = "",
                    id = "",
                    title = "",
                    questions = null,
                    answer = null,
                    updatedAt = "",
                    createdAt = ""
                ),
                updatedAt = "",
                createdAt = STATISTICS_VIEW,
                userId = "",
                dataset = null,
                publishAt = "",
                expireAt = "",
                id = ""
            )
            val itemsWithStatistics = mutableListOf<Assignment>()
            itemsWithStatistics.addAll(items)
            itemsWithStatistics.add(0, statisticsAssignment)
            this.items = itemsWithStatistics
            notifyDataSetChanged()
        }else {
            this.items = items
            notifyDataSetChanged()
        }
    }

    fun setOnRowClickedListener(onRowClickedListener: OnSurveyRowClickedListener){
        this.onRowClickedListener = onRowClickedListener
    }
}