package com.alchembright.dev.langtrackapp.screen.surveyContainer.footerFragment

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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
//import kotlinx.android.synthetic.main.footer_fragment.view.*
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.FooterFragmentBinding
import com.alchembright.dev.langtrackapp.interfaces.OnQuestionInteractionListener

class FooterFragment : Fragment(){

    private var listener: OnQuestionInteractionListener? = null
    lateinit var binding: FooterFragmentBinding
    lateinit var question: Question

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.footer_fragment, container,false)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        val v = binding.root
        binding.footerNextButton.setOnClickListener {
            listener?.sendInSurvey()
        }
        binding.footerBackButton.setOnClickListener {
            listener?.prevoiusQuestion(current = question)
        }
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnQuestionInteractionListener) {
            listener = context
            if (::binding.isInitialized) {
                //load survey
                setText()
            }
        }else {
            throw RuntimeException(context.toString() + " must implement OnLikertScaleInteraktionListener")
        }
    }

    fun setText(){
        if (::binding.isInitialized) {
            binding.footerTextView.text = (activity as? com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity)?.answerReview() ?: question.text
        }
    }

    override fun onResume() {
        super.onResume()
        //update question
        setText()
    }

    fun setSending(sending: Boolean) {
        if (!::binding.isInitialized) return
        binding.footerNextButton.isEnabled = !sending
        binding.footerBackButton.isEnabled = !sending
        binding.footerNextButton.setText(if (sending) R.string.sending_answers else R.string.submit_answers)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            FooterFragment().apply {

            }
    }
}