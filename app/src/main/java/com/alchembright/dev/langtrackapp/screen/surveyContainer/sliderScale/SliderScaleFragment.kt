package com.alchembright.dev.langtrackapp.screen.surveyContainer.sliderScale

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
//import kotlinx.android.synthetic.main.likert_scale_fragment.view.*
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.SliderScaleFragmentBinding
import com.alchembright.dev.langtrackapp.interfaces.OnQuestionInteractionListener
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity

class SliderScaleFragment : Fragment(){


    private var listener: OnQuestionInteractionListener? = null
    lateinit var binding: SliderScaleFragmentBinding
    var theAnswer: Answer? = null
    lateinit var question: Question

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.slider_scale_fragment, container,false)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        val v = binding.root

        initSeekbar()
        binding.sliderScaleNextButton.setOnClickListener {
            theAnswer = null
            listener?.nextQuestion(question)
        }
        binding.sliderScaleBackButton.setOnClickListener {
            theAnswer = null
            listener?.prevoiusQuestion(current = question)
        }
        binding.sliderScaleCheckboxNA.setOnClickListener {
            if (binding.sliderScaleCheckboxNA.isChecked){
                binding.sliderSeekBar.isEnabled = false
                binding.sliderScaleValueTextView.text = ""
                theAnswer?.sliderScaleAnswer = binding.sliderSeekBar.progress
                binding.sliderSeekBar.progress = 0
                listener?.setSliderAnswer(theAnswer?.sliderScaleAnswer ?: 0, true)
            }else{
                if (theAnswer?.sliderScaleAnswer ?: -1 == -1){
                    binding.sliderSeekBar.isEnabled = true
                    binding.sliderScaleValueTextView.text = 50.toString()
                    binding.sliderSeekBar.progress = 50
                    listener?.setSliderAnswer(binding.sliderSeekBar.progress, false)
                }else {
                    val value = theAnswer?.sliderScaleAnswer ?: 50
                    binding.sliderSeekBar.isEnabled = true
                    binding.sliderScaleValueTextView.text = if (value == -1) {
                        ""
                    } else {
                        value.toString()
                    }
                    binding.sliderSeekBar.progress = value
                    listener?.setSliderAnswer(binding.sliderSeekBar.progress, false)
                }
            }
        }
        binding.sliderSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!binding.sliderScaleCheckboxNA.isChecked) {
                    binding.sliderScaleValueTextView.text = progress.toString()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (theAnswer == null){theAnswer = Answer((SurveyContainerActivity.SLIDER_SCALE))}
                listener?.setSliderAnswer(binding.sliderSeekBar.progress, false)
            }
        })
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnQuestionInteractionListener) {
            listener = context
            if (::binding.isInitialized) {
                setQuestion()
            }
        }else {
            throw RuntimeException("$context must implement OnLikertScaleInteraktionListener")
        }
    }

    fun initSeekbar(){
        if (theAnswer == null){
            binding.sliderSeekBar.isEnabled = true
            binding.sliderScaleValueTextView.text = 50.toString()
            binding.sliderSeekBar.progress = 50
            theAnswer = Answer(SurveyContainerActivity.SLIDER_SCALE)
            theAnswer!!.sliderScaleAnswer = 50
            listener?.setSliderAnswer(50, false)
        }else if (theAnswer?.sliderScaleAnswer ?: 0 == -1){
            binding.sliderSeekBar.isEnabled = false
            binding.sliderScaleValueTextView.text = ""
            binding.sliderSeekBar.progress = 0
        }else {
            binding.sliderSeekBar.isEnabled = true
            binding.sliderScaleValueTextView.text = (theAnswer?.sliderScaleAnswer ?: 50).toString()
            binding.sliderSeekBar.progress = theAnswer?.sliderScaleAnswer ?: 50
        }
    }

    fun setQuestion(){
        if (::binding.isInitialized) {
            binding.sliderScaleTextTextView.text = question.text
            binding.sliderScaleMin.text = question.likertMin
            binding.sliderScaleMax.text = question.likertMax
            initSeekbar()
        }
    }

    override fun onResume() {
        super.onResume()
        setQuestion()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SliderScaleFragment().apply {

            }
    }
}