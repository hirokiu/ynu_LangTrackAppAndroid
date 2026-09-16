package com.alchembright.dev.langtrackapp.screen.overview.overviewQuestionViews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.LinearLayout
//import kotlinx.android.synthetic.main.overview_multiple_choice_view_layout.view.*
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.OverviewMultipleChoiceViewLayoutBinding

class OverviewMultipleChoiceView @JvmOverloads constructor(

    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr)  {

    lateinit var binding: OverviewMultipleChoiceViewLayoutBinding

    init {
        var inflater = LayoutInflater.from(context)
        binding = OverviewMultipleChoiceViewLayoutBinding.inflate(inflater, this, true)
    }

    fun setText(question: Question, choices: List<String>){
        binding.overviewMultipleViewTextTextView.text = question.text
        if (question.multipleChoisesAnswers != null) {
            for (answer in question.multipleChoisesAnswers!!) {
                val checkbox = CheckBox(binding.overviewMultipleViewChoices.context)
                checkbox.text = answer
                checkbox.isClickable = false
                if (choices.contains(answer)){
                    checkbox.textSize = 17F
                    checkbox.setTextColor(resources.getColor(R.color.lta_blue,null))
                    checkbox.isChecked = true
                }else{
                    checkbox.textSize = 15F
                    checkbox.setTextColor(resources.getColor(R.color.lta_text,null))
                    checkbox.isChecked = false
                }
                binding.overviewMultipleViewChoices.addView(checkbox)
            }
        }
    }
}