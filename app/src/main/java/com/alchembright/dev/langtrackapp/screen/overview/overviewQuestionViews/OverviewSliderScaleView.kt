package com.alchembright.dev.langtrackapp.screen.overview.overviewQuestionViews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
//import kotlinx.android.synthetic.main.overview_slider_scale_view_layout.view.*
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.OverviewSliderScaleViewLayoutBinding

class OverviewSliderScaleView@JvmOverloads constructor(

    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr)  {

    lateinit var binding: OverviewSliderScaleViewLayoutBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = OverviewSliderScaleViewLayoutBinding.inflate(inflater, this, true)

        //disable checkbox
        binding.overviewSliderScaleCheckboxNA.isActivated = false

        //disable seekbar touch
        binding.overviewSliderSeekBar.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                return true
            }
        })
    }

    fun setText(question: Question, answer: Int){
        println("answer: $answer")
        binding.overviewSliderScaleViewTextTextView.text = question.text
        binding.overviewSliderScaleMax.text = question.likertMax
        binding.overviewSliderScaleMin.text = question.likertMin
        binding.overviewSliderScaleValueTextView.text = ""
        if (answer == -1){
            binding.overviewSliderScaleCheckboxNA.isChecked = true
            binding.overviewSliderSeekBar.isEnabled = false
        }else if ((0..100).contains(answer)){
            binding.overviewSliderScaleValueTextView.text = answer.toString()
            binding.overviewSliderScaleCheckboxNA.isChecked = false
            binding.overviewSliderSeekBar.isEnabled = true
            binding.overviewSliderSeekBar.progress = answer
        }else{
            binding.overviewSliderScaleCheckboxNA.isChecked = false
            binding.overviewSliderSeekBar.isEnabled = false
        }
    }

}