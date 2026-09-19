package com.alchembright.dev.langtrackapp.screen.surveyContainer.timeDuration

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.TimeDurationFragmentBinding
import com.alchembright.dev.langtrackapp.interfaces.OnQuestionInteractionListener

class TimeDurationFragment : Fragment(){

    private var listener: OnQuestionInteractionListener? = null
    lateinit var binding: TimeDurationFragmentBinding
    lateinit var theQuestion: Question
    var theAnswer: Answer? = null
    private val listOfHours = arrayOf("0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24")
    private val listOfMinutes = arrayOf("00","05","10","15","20","25","30","35","40","45","50","55")
    var selectedHours = 0
    var selectedMinutes = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.time_duration_fragment, container,false)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        val v = binding.root
        binding.numberPickerHour.minValue = 0
        binding.numberPickerHour.maxValue = listOfHours.size - 1
        binding.numberPickerHour.displayedValues = listOfHours
        binding.numberPickerHour.setOnValueChangedListener { _, _, _ ->
            listener?.setTimeDurationAnswer(getSelectedDurationInSeconds())
        }
        binding.numberPickerMinutes.minValue = 0
        binding.numberPickerMinutes.maxValue = listOfMinutes.size - 1
        binding.numberPickerMinutes.displayedValues = listOfMinutes
        binding.numberPickerMinutes.setOnValueChangedListener { _, _, _ ->
            listener?.setTimeDurationAnswer(getSelectedDurationInSeconds())
        }

        binding.timeDurationBackButton.setOnClickListener {
            binding.numberPickerHour.clearFocus()
            binding.numberPickerMinutes.clearFocus()
            listener?.setTimeDurationAnswer(getSelectedDurationInSeconds())
            theAnswer = null
            listener?.prevoiusQuestion(theQuestion)
        }
        binding.timeDurationNextButton.setOnClickListener {
            binding.numberPickerHour.clearFocus()
            binding.numberPickerMinutes.clearFocus()
            listener?.setTimeDurationAnswer(getSelectedDurationInSeconds())
            theAnswer = null
            listener?.nextQuestion(theQuestion)
        }
        return v
    }

    private fun getSelectedDurationInSeconds(): Int =
        binding.numberPickerHour.value * 3600 + listOfMinutes[binding.numberPickerMinutes.value].toInt() * 60

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnQuestionInteractionListener) {
            listener = context
            if (::binding.isInitialized) {
                setQuestion()
            }
        }else {
            throw RuntimeException(context.toString() + " must implement OnLikertScaleInteraktionListener")
        }
    }

    fun setQuestion(){
        if (::binding.isInitialized) {
            binding.timeDurationTextTextView.text = theQuestion.text
            if (theAnswer != null){
                if (theAnswer!!.timeDurationAnswer != null){
                    val hours = theAnswer!!.timeDurationAnswer!! / 60 / 60
                    val minutes = (theAnswer!!.timeDurationAnswer!! - (hours * 60 * 60)) / 60
                    binding.numberPickerHour.value = hours
                    var minString = minutes.toString()
                    if (minString == "0"){
                        minString = "00"
                    }else if (minString == "5"){
                        minString = "05"
                    }
                    val minIndex: Int = listOfMinutes.indexOf(minString)
                    binding.numberPickerMinutes.value = minIndex
                }
            }else{
                binding.numberPickerHour.value = 0
                binding.numberPickerMinutes.value = 0
            }
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
            TimeDurationFragment().apply {

            }
    }
}