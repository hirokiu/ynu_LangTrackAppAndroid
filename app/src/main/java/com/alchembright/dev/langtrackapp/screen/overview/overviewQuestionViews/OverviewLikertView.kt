package com.alchembright.dev.langtrackapp.screen.overview.overviewQuestionViews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
//import kotlinx.android.synthetic.main.overview_likert_view_layout.view.*
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.OverviewLikertViewLayoutBinding

class OverviewLikertView @JvmOverloads constructor(

    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr)  {

    lateinit var binding: OverviewLikertViewLayoutBinding

    init {
        var inflater = LayoutInflater.from(context)
        binding = OverviewLikertViewLayoutBinding.inflate(inflater, this, true)
    }

    fun setText(question: Question, answer: Int){
        binding.overviewLikertViewDescriptionTextView.text = "${question.likertMin}\n${question.likertMax}"
        binding.overviewLikertViewTextTextView.text = question.text
        setRadiobutton(answer)
    }

    fun setRadiobutton(selected: Int){
        binding.overviewLikertViewRadioButton1.isChecked = false
        binding.overviewLikertViewRadioButton2.isChecked = false
        binding.overviewLikertViewRadioButton3.isChecked = false
        binding.overviewLikertViewRadioButton4.isChecked = false
        binding.overviewLikertViewRadioButton5.isChecked = false
        //binding.overviewLikertViewRadioButtonNA.isChecked = false
        when(selected){
            0 -> binding.overviewLikertViewRadioButton1.isChecked = true
            1 -> binding.overviewLikertViewRadioButton2.isChecked = true
            2 -> binding.overviewLikertViewRadioButton3.isChecked = true
            3 -> binding.overviewLikertViewRadioButton4.isChecked = true
            4 -> binding.overviewLikertViewRadioButton5.isChecked = true
            //5 -> binding.overviewLikertViewRadioButtonNA.isChecked = true
        }
    }
}