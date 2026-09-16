package com.alchembright.dev.langtrackapp.screen.surveyContainer.openEndedTextResponsesFragment

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se

* Viktor Czyżewski
* RSE Team
* University of York
* */

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
//import kotlinx.android.synthetic.main.open_ended_text_responses_fragment.view.*
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.OpenEndedTextResponsesFragmentBinding
import com.alchembright.dev.langtrackapp.interfaces.OnQuestionInteractionListener

class OpenEndedTextResponsesFragment : Fragment(){

    private var listener: OnQuestionInteractionListener? = null
    lateinit var binding: OpenEndedTextResponsesFragmentBinding
    lateinit var question: Question
    var theAnswer: Answer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.open_ended_text_responses_fragment, container,false)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        val v = binding.root
        binding.openEndedTextNextButton.setOnClickListener {
            saveAnswer()
            listener?.nextQuestion(current = question)
        }
        binding.openEndedTextBackButton.setOnClickListener {
            saveAnswer()
            listener?.prevoiusQuestion(current = question)
        }
        binding.openLayout.setOnClickListener {
            hideKeyboard()
        }
        binding.openEditText.setOnKeyListener { _, _, event ->
            if((event.action == KeyEvent.ACTION_DOWN)
                && (event.keyCode == KeyEvent.KEYCODE_ENTER)){
                hideKeyboard()
                return@setOnKeyListener true
            }
            false
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

    private fun saveAnswer(){
        if (binding.openEditText.text.isNullOrBlank()){
            listener?.setOpenEndedAnswer(null)
        }else {
            listener?.setOpenEndedAnswer(binding.openEditText.text.toString())
        }
    }

    fun setQuestion(){
        if (::binding.isInitialized) {
            binding.openTextTextView.text = question.text
            binding.openEditText.setText(theAnswer?.openEndedAnswer ?: "")
        }
    }

    override fun onResume() {
        super.onResume()
        setQuestion()
    }

    private fun hideKeyboard() {
        if (activity != null) {
            val imm =
                activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = binding.openEditText
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            OpenEndedTextResponsesFragment().apply {

            }
    }
}