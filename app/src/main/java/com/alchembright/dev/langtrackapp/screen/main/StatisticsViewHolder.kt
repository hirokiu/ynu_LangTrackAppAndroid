package com.alchembright.dev.langtrackapp.screen.main

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.alchembright.dev.langtrackapp.R
import java.util.*
import kotlin.math.round


class StatisticsViewHolder(theItemView: View) : RecyclerView.ViewHolder(theItemView) {

    private var cellLayout: ConstraintLayout = itemView.findViewById(R.id.statisticsCellLayout)
    private var progressBar: ProgressBar = itemView.findViewById(R.id.statisticProgressbar)
    private var progressBackgroundBar: ProgressBar = itemView.findViewById(R.id.statisticBackgroundProgressbar)
    private var percentText: TextView = itemView.findViewById(R.id.statisticPercentTextView)
    private var statisticsText: TextView = itemView.findViewById(R.id.statisticsTextView)
    private var statisticsEmojiText: TextView = itemView.findViewById(R.id.statisticsEmojiTextView)

    fun bind(answered: Int, unanswered: Int){
        setChartAndEmoji(answered,unanswered)
        //statisticsText.text = "Du har besvarat " +answered + " av dina " + (answered + unanswered) + "enkäter"
        /*statisticsText.text = statisticsText.context.getString(R.string.youHaveAnswered) +answered + statisticsText.context.getString(
                    R.string.ofYour) + (answered + unanswered) + statisticsText.context.getString(R.string.surveys)*/

        //Turkish is LTR but the variables are mixed... :-)
        if ( Locale.getDefault().language.equals("tr")){
            statisticsText.text = statisticsText.context.getString(R.string.youHaveAnsweredWithFormate, (answered + unanswered).toString(), answered.toString())
        }else {
            //All other languages are handled by localization changes
            statisticsText.text =
                statisticsText.context.getString(R.string.youHaveAnsweredWithFormate,
                    answered.toString(),
                    (answered + unanswered).toString())
        }
    }

    private fun setChartAndEmoji(answered: Int, unanswered: Int){
        if (unanswered != 0) {
            val percent = 100 * (answered.toDouble() / (answered + unanswered).toDouble())
            val percentRounded = round(10 * percent) / 10
            percentText.text = "$percentRounded%"
            if (percentRounded >= 90){
                statisticsEmojiText.text = "🌟"
                setColor("green")
            }else if (percentRounded >= 80){
                statisticsEmojiText.text = "😁"
                setColor("green")
            }else if (percentRounded >= 50){
                statisticsEmojiText.text = "👍"
                setColor("yellow")
            }else if (percentRounded >= 25){
                statisticsEmojiText.text = "😏"
                setColor("brown")
            }else{
                statisticsEmojiText.text = "😬"
                setColor("gray")
            }
            progressBar.progress = percentRounded.toInt()
        }else if (answered != 0){
            statisticsEmojiText.text = "🌟"
            setColor("green")
            percentText.text = "100%"
            progressBar.progress = 100
        }
    }
    fun setColor(color: String){
        var theColor = statisticsEmojiText.context.getColor(R.color.lta_green)
        when (color){
            "green" -> theColor = statisticsEmojiText.context.getColor(R.color.lta_green)
            "yellow" -> theColor = statisticsEmojiText.context.getColor(R.color.lta_yellow)
            "brown" -> theColor = statisticsEmojiText.context.getColor(R.color.lta_brown)
            "gray" -> theColor = statisticsEmojiText.context.getColor(R.color.lta_grey)
        }
        progressBar.progressTintList = ColorStateList.valueOf(theColor)
    }

    companion object {
        fun newInstance(parent: ViewGroup):StatisticsViewHolder{
            return StatisticsViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.recycle_statistics_row,
                    parent,
                    false
                )
            )
        }
    }
}