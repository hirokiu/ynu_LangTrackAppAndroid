package com.alchembright.dev.langtrackapp.screen.surveyContainer.fillInTheBlankFragment

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
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
//import kotlinx.android.synthetic.main.fill_in_the_blanks_fragment.view.*
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.FillInTheBlanksFragmentBinding
import com.alchembright.dev.langtrackapp.interfaces.OnQuestionInteractionListener
import com.alchembright.dev.langtrackapp.screen.surveyContainer.fillInTheBlankFragment.FillInTheBlankFragment.Companion.FIVE_UNDERSCORES


class FillInTheBlankFragment : Fragment(){

    private var listener: OnQuestionInteractionListener? = null
    lateinit var binding: FillInTheBlanksFragmentBinding
    lateinit var spinner: Spinner
    lateinit var theQuestion: Question
    var theSentence: FillInWordSentence? = null
    var theChosenWordIndex : Int? = null
    var theAnswer: Answer? = null
    var check = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fill_in_the_blanks_fragment, container,false)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        val v = binding.root
        spinner = binding.choiceSpinner
        if (theQuestion.fillBlanksChoises != null && ::binding.isInitialized) {
            setAdapter()
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    theChosenWordIndex = position
                    setSentence(position)
                    if (theSentence?.indexForMissingWord in (theSentence?.listWithWords?.indices ?: IntRange.EMPTY)) {
                        listener?.setFillBlankAnswer(if (position > 0) position - 1 else null)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }
        }
        binding.fillInTheBlankNextButton.setOnClickListener {
            theAnswer = null
            theChosenWordIndex = null
            theSentence = null
            listener?.nextQuestion(theQuestion)
        }
        binding.fillInTheBlankBackButton.setOnClickListener {
            theAnswer = null
            theChosenWordIndex = null
            theSentence = null
            listener?.prevoiusQuestion(current = theQuestion)
        }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setQuestion()
    }

    fun setAdapter(){

        val adapter =
            ArrayAdapter(
                spinner.context,
                R.layout.choice_spinner_item,
                listOf(FIVE_UNDERSCORES) + theQuestion.fillBlanksChoises.orEmpty()
            )
        adapter.setDropDownViewResource(R.layout.choice_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setInitAnswer() {
        val answerIndex = theAnswer?.fillBlankAnswer
        val position = if (answerIndex != null && answerIndex in theQuestion.fillBlanksChoises.orEmpty().indices) answerIndex + 1 else 0
        theChosenWordIndex = position
        spinner.setSelection(position, false)
        setSentence(position)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnQuestionInteractionListener) {
            listener = context
        }else {
            throw RuntimeException(context.toString() + " must implement OnFillInBlankInteractionListener")
        }
    }

    fun setQuestion(){
        if (::binding.isInitialized) {
            check = 0
            theSentence = null
            theSentence = getTextAsList(theQuestion.text)
            setAdapter()
            if (theSentence != null){
                if (theChosenWordIndex != null){
                    setSentence(theChosenWordIndex)
                }else {
                    setSentence(null)
                }
            }
            setInitAnswer()
        }
    }



    fun setSentence(indexOfWord: Int?) {
        val sentence = theSentence ?: return
        if (sentence.indexForMissingWord !in sentence.listWithWords.indices) {
            binding.fillInTheBlankTextView.text = theQuestion.text + "\n" + getString(R.string.invalid_blank_question)
            binding.fillInTheBlankNextButton.isEnabled = false
            return
        }
        val selectedWord = indexOfWord?.takeIf { it > 0 }?.let { theQuestion.fillBlanksChoises?.getOrNull(it - 1) }
        val words = sentence.listWithWords.toMutableList()
        words[sentence.indexForMissingWord] = selectedWord ?: FIVE_UNDERSCORES
        binding.fillInTheBlankTextView.text = if (selectedWord == null) words.joinToString(" ") else underlineSelectedWord(words, selectedWord)
        binding.fillInTheBlankNextButton.isEnabled = selectedWord != null
    }

    override fun onDetach() {
        println("FillInTheBlankFragment onDetach")
        super.onDetach()
        listener = null
    }

    companion object {
        val FIVE_UNDERSCORES = "_____"
        @JvmStatic
        fun newInstance() =
            FillInTheBlankFragment().apply {

            }
    }
}

fun underlineSelectedWord(list: List<String>, selectedWord: String): SpannableString{
    val theOrgSentence = list.joinToString(separator = " ")
    val start = theOrgSentence.indexOf(selectedWord)
    val end = start + selectedWord.length
    val returnString = SpannableString(theOrgSentence)
    if (start >= 0) returnString.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return returnString
}

fun getTextAsList(theText: String): FillInWordSentence{
    val listWithWords = theText.split(" ")

    var ind = -99
    for ((i,word) in listWithWords.withIndex()){
        if (word == FIVE_UNDERSCORES){
            ind = i
        }

    }
    val theSentence = FillInWordSentence(
        listWithWords = listWithWords,
        indexForMissingWord = ind
    )
    return theSentence
}

data class FillInWordSentence (
    var listWithWords: List<String>,
    var indexForMissingWord: Int
)