package com.alchembright.dev.langtrackapp.screen.surveyContainer.singleMultipleAnswersFragment

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se

* Viktor Czyżewski
* RSE Team
* University of York
* */

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
//import kotlinx.android.synthetic.main.single_multiple_answer_item.view.singleMultipleAnswerNextButton
//import kotlinx.android.synthetic.main.single_multiple_answers_fragment.view.*
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.SingleMultipleAnswersFragmentBinding
import com.alchembright.dev.langtrackapp.interfaces.OnQuestionInteractionListener
import com.alchembright.dev.langtrackapp.util.setMarginTop


class SingleMultipleAnswersFragment : Fragment(){

    private var listener: OnQuestionInteractionListener? = null
    lateinit var binding: SingleMultipleAnswersFragmentBinding
    lateinit var theQuestion: Question
    var theAnswer: Answer? = null
    var selectedRadioButton = -99

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.single_multiple_answers_fragment, container,false)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        val v = binding.root
        binding.singleMultipleAnswerNextButton.setOnClickListener {
            theAnswer = null
            listener?.nextQuestion(theQuestion)
        }
        binding.singleMultipleAnswerBackButton.setOnClickListener {
            theAnswer = null
            listener?.prevoiusQuestion(current = theQuestion)
        }
        binding.singleMultipleAnswerContainer.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1 ) {
                val radio: RadioButton? = group.findViewById(group.checkedRadioButtonId)
                if (radio != null) {
                    selectedRadioButton = radio.tag as Int
                    listener?.setSingleMultipleAnswer(selectedRadioButton)
                    println("selectedRadioButton: $selectedRadioButton")
                    setNextButton()
                }
            }
        }
        //set shadow on scroll
        binding.singleMultipleAnswerScroll.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.singleIndicatorTop.isSelected = binding.singleMultipleAnswerScroll.canScrollVertically(-1)
            binding.singleIndicatorBottom.isSelected = binding.singleMultipleAnswerScroll.canScrollVertically(1)
        }

        //set shadow when start
        binding.singleMultipleAnswerScroll.viewTreeObserver.addOnGlobalLayoutListener {
            binding.singleIndicatorTop.isSelected = binding.singleMultipleAnswerScroll.canScrollVertically(-1)
            binding.singleIndicatorBottom.isSelected = binding.singleMultipleAnswerScroll.canScrollVertically(1)
        }
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
            throw RuntimeException(context.toString() + " must implement OnLikertScaleInteraktionListener")
        }
    }

    fun setQuestion(){
        if (::binding.isInitialized) {
            binding.singleMultipleAnswerTextTextView.text = theQuestion.text
            presentChoices()
            //setWidthOfShadows()
        }
    }

    fun setWidthOfShadows(){
        val radioButtonContainerWidth = binding.singleMultipleAnswerContainer.measuredWidth
        val paramTop = binding.singleIndicatorTop.layoutParams
        paramTop.width = radioButtonContainerWidth
        binding.singleIndicatorTop.layoutParams = paramTop
    }

    fun presentChoices(){
        selectedRadioButton = -99
        if (theQuestion.singleMultipleAnswers != null) {
            binding.singleMultipleAnswerContainer.removeAllViews()
            for ((index, choice) in theQuestion.singleMultipleAnswers!!.withIndex()) {
                val radioButton = RadioButton(binding.singleMultipleAnswerContainer.context)
                radioButton.text = choice
                radioButton.tag = index
                radioButton.textSize = 17f
                binding.singleMultipleAnswerContainer.addView(radioButton)
                radioButton.setMarginTop(10)
                if (theAnswer?.singleMultipleAnswer ?: -99 == index) {
                    radioButton.isChecked = true
                    selectedRadioButton = index
                }else{
                    radioButton.isChecked = false
                }
            }
        }
        setNextButton()
    }

    private fun setNextButton(){
        binding.singleMultipleAnswerNextButton.isEnabled =
            selectedRadioButton != -99

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
            SingleMultipleAnswersFragment().apply {

            }
    }
}