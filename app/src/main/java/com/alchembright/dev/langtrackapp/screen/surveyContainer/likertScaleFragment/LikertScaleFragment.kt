package com.alchembright.dev.langtrackapp.screen.surveyContainer.likertScaleFragment

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
//import kotlinx.android.synthetic.main.likert_scale_fragment.*
//import kotlinx.android.synthetic.main.likert_scale_fragment.view.*
//import kotlinx.android.synthetic.main.likert_scale_fragment.view.likertScaleRadioGroup
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.LikertScaleFragmentBinding
import com.alchembright.dev.langtrackapp.interfaces.OnQuestionInteractionListener

class LikertScaleFragment : Fragment(){

    private var listener: OnQuestionInteractionListener? = null
    lateinit var binding: LikertScaleFragmentBinding
    lateinit var question: Question
    var theAnswer: Answer? = null
    var selectedRadioButton = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.likert_scale_fragment, container,false)
        binding.setLifecycleOwner(this)
        binding.executePendingBindings()
        val v = binding.root
        binding.likertScaleNextButton.setOnClickListener {
            theAnswer = null
            listener?.nextQuestion(question)
        }
        binding.likertScaleBackButton.setOnClickListener {
            theAnswer = null
            listener?.prevoiusQuestion(current = question)
        }
        binding.likertScaleRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                val radio: RadioButton = v.findViewById(checkedId)
                selectedRadioButton = (radio.tag as String).toInt()
                listener?.setLikertAnswer(selectedRadioButton)
                binding.likertScaleNextButton.isEnabled = true
            }
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
            binding.likertScaleRadioGroup.clearCheck()
            binding.likertScaleDescriptionTextView.text = question.description
            binding.likertScaleMin.text = question.likertMin
            binding.likertScaleMax.text = question.likertMax
            binding.likertScaleTextTextView.text = question.text
            binding.likertScaleNextButton.isEnabled = false
            if (theAnswer != null) {
                if (theAnswer!!.likertAnswer != null){
                    if (theAnswer!!.index == question.index){
                        val theRadioButton = when (theAnswer?.likertAnswer){
                            0 -> binding.likertScaleRadioButton1.id
                            1 -> binding.likertScaleRadioButton2.id
                            2 -> binding.likertScaleRadioButton3.id
                            3 -> binding.likertScaleRadioButton4.id
                            4 -> binding.likertScaleRadioButton5.id
                            //5 -> binding.likertScaleRadioButtonNA.id
                            else -> null
                        }
                        if (theRadioButton != null) {
                            binding.likertScaleRadioGroup.check(theRadioButton)
                            binding.likertScaleNextButton.isEnabled = true
                        }
                    }
                }
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
            LikertScaleFragment().apply {

            }
    }
}