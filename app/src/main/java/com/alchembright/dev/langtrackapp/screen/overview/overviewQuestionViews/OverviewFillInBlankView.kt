package com.alchembright.dev.langtrackapp.screen.overview.overviewQuestionViews

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
//import kotlinx.android.synthetic.main.overview_fill_in_blank_view_layout.view.*
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.OverviewFillInBlankViewLayoutBinding
import com.alchembright.dev.langtrackapp.screen.surveyContainer.fillInTheBlankFragment.FillInWordSentence
import com.alchembright.dev.langtrackapp.screen.surveyContainer.fillInTheBlankFragment.getTextAsList
import com.alchembright.dev.langtrackapp.screen.surveyContainer.fillInTheBlankFragment.underlineSelectedWord

class OverviewFillInBlankView @JvmOverloads constructor(

    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr)  {

    lateinit var binding: OverviewFillInBlankViewLayoutBinding
    var theSentence: FillInWordSentence? = null

    init {
        var inflater = LayoutInflater.from(context)
        binding = OverviewFillInBlankViewLayoutBinding.inflate(inflater, this, true)
    }

    fun setText(question: Question, selectedchoice: String?){
        theSentence = getTextAsList(question.text)

        val tempListWithWords = theSentence!!.listWithWords.toMutableList()
        tempListWithWords[theSentence!!.indexForMissingWord] = selectedchoice ?: ""
        binding.overviewFillBlankTextView.text = underlineSelectedWord(tempListWithWords,
            selectedchoice ?: "")

        //overviewFillBlankTextView.text = question.text
        if (question.fillBlanksChoises != null){
            for (choice in question.fillBlanksChoises!!){
                val textview = TextView(binding.overviewFillBlankChoicesLayout.context)
                textview.text = choice
                if (!selectedchoice.isNullOrBlank() && choice == selectedchoice){
                    textview.setTypeface(null, Typeface.BOLD)
                    textview.textSize = 17F
                    textview.setTextColor(resources.getColor(R.color.lta_blue,null))
                }else{
                    textview.textSize = 15F
                    textview.setTextColor(resources.getColor(R.color.lta_text,null))
                }
                binding.overviewFillBlankChoicesLayout.addView(textview)
            }
        }
    }
}