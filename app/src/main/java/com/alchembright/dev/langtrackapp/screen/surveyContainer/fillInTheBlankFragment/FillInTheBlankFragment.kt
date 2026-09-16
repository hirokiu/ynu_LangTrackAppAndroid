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
                    if(++check > 1) {
                        theChosenWordIndex = position
                        listener?.setFillBlankAnswer(if(theChosenWordIndex != null) (theChosenWordIndex!! - 1) else theChosenWordIndex ) // -1 for blank first place
                        if (theSentence != null) {
                            setSentence(theChosenWordIndex)
                        }
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
        addEmptyWordToTopOfList()
        val adapter =
            ArrayAdapter(
                spinner.context,
                R.layout.choice_spinner_item,
                theQuestion.fillBlanksChoises!!
            )
        adapter.setDropDownViewResource(R.layout.choice_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setInitAnswer(){
        val answerIndex = theAnswer?.fillBlankAnswer ?: -99
        theChosenWordIndex = null
        if (answerIndex >= 0){
            val answerWord = theQuestion.fillBlanksChoises?.get(answerIndex)
            if (answerWord != null){
                if (theSentence != null){
                    theChosenWordIndex = answerIndex
                    check++
                    spinner.setSelection(answerIndex + 1,false)//must add 1 because _____ at first place
                }
            }
        }
    }


    private fun addEmptyWordToTopOfList(){
        if (theQuestion.fillBlanksChoises?.first() != FIVE_UNDERSCORES) {
            theQuestion.fillBlanksChoises!!.add(0, FIVE_UNDERSCORES)
        }
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



    fun setSentence(indexOfWord: Int?){
        if (indexOfWord == null){
            binding.fillInTheBlankTextView.text = theSentence!!.listWithWords.joinToString(separator = " ")
            binding.fillInTheBlankSpinnerTitle
            binding.fillInTheBlankNextButton.isEnabled = false
        }else if (theChosenWordIndex != null){
            val tempListWithWords = theSentence!!.listWithWords.toMutableList()
            tempListWithWords[theSentence!!.indexForMissingWord] = theQuestion.fillBlanksChoises?.get(theChosenWordIndex!!) ?: ""
            binding.fillInTheBlankTextView.text = underlineSelectedWord(tempListWithWords,
                theQuestion.fillBlanksChoises?.get(theChosenWordIndex!!) ?: "")
            binding.fillInTheBlankNextButton.isEnabled = theChosenWordIndex != 0
        }else{
            val tempListWithWords = theSentence!!.listWithWords.toMutableList()
            tempListWithWords[theSentence!!.indexForMissingWord] = theQuestion.fillBlanksChoises?.get(indexOfWord) ?: ""
            binding.fillInTheBlankTextView.text = underlineSelectedWord(tempListWithWords,
                theQuestion.fillBlanksChoises?.get(theChosenWordIndex!!) ?: "")
            binding.fillInTheBlankNextButton.isEnabled = theChosenWordIndex != 0
        }
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
    returnString.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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