@file:Suppress("OverrideDeprecatedMigration")

package com.alchembright.dev.langtrackapp.screen.surveyContainer

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se

* Viktor Czyżewski
* RSE Team
* University of York
* */

import com.alchembright.dev.langtrackapp.util.applySystemBarInsets
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
//import kotlinx.android.synthetic.main.login_activity.*
//import kotlinx.android.synthetic.main.survey_container_activity.*
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.data.model.Assignment
import com.alchembright.dev.langtrackapp.data.model.Question
import com.alchembright.dev.langtrackapp.databinding.SurveyContainerActivityBinding
import com.alchembright.dev.langtrackapp.interfaces.*
import com.alchembright.dev.langtrackapp.popup.OneChoicePopup
import com.alchembright.dev.langtrackapp.popup.PopupAlert
import com.alchembright.dev.langtrackapp.screen.surveyContainer.fillInTheBlankFragment.FillInTheBlankFragment
import com.alchembright.dev.langtrackapp.screen.surveyContainer.footerFragment.FooterFragment
import com.alchembright.dev.langtrackapp.screen.surveyContainer.header.HeaderFragment
import com.alchembright.dev.langtrackapp.screen.surveyContainer.likertScaleFragment.LikertScaleFragment
import com.alchembright.dev.langtrackapp.screen.surveyContainer.multipleChoiceFragment.MultipleChoiceFragment
import com.alchembright.dev.langtrackapp.screen.surveyContainer.openEndedTextResponsesFragment.OpenEndedTextResponsesFragment
import com.alchembright.dev.langtrackapp.screen.surveyContainer.singleMultipleAnswersFragment.SingleMultipleAnswersFragment
import com.alchembright.dev.langtrackapp.screen.surveyContainer.sliderScale.SliderScaleFragment
import com.alchembright.dev.langtrackapp.screen.surveyContainer.timeDuration.TimeDurationFragment
import com.alchembright.dev.langtrackapp.util.formatToReadable
import com.alchembright.dev.langtrackapp.util.loadFragment
import com.alchembright.dev.langtrackapp.util.toDate
import java.util.*

class SurveyContainerActivity : AppCompatActivity(),
    OnQuestionInteractionListener{


    private lateinit var mBind : SurveyContainerActivityBinding
    private lateinit var viewModel : SurveyContainerViewModel
    private var questionList = mutableListOf<Question>()
    private lateinit var headerFragment: HeaderFragment
    private lateinit var likertScaleFragment: LikertScaleFragment
    private lateinit var fillInTheBlankFragment: FillInTheBlankFragment
    private lateinit var multipleChoiceFragment: MultipleChoiceFragment
    private lateinit var singleMultipleAnswersFragment: SingleMultipleAnswersFragment
    private lateinit var openEndedTextResponsesFragment: OpenEndedTextResponsesFragment
    private lateinit var timeDurationFragment: TimeDurationFragment
    private lateinit var sliderScaleFragment: SliderScaleFragment
    private lateinit var footerFragment: FooterFragment
    private var currentPage = Question()
    private var answer = mutableMapOf<Int,Answer>()
    private var theAssignment: Assignment? = null
    private var inTestMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        theAssignment = intent.getParcelableExtra<Assignment>(ASSIGNMENT)
        inTestMode = intent.getBooleanExtra(IN_TEST_MODE, false)

        mBind = DataBindingUtil.setContentView(this, R.layout.survey_container_activity)
        applySystemBarInsets()
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = confirmCloseSurvey()
        })
        mBind.lifecycleOwner = this
        mBind.executePendingBindings()

        viewModel = ViewModelProvider(this,
            SurveyContainerViewModelFactory(this)
        ).get(SurveyContainerViewModel::class.java)
        mBind.viewModel = viewModel

        //create fragments
        headerFragment = HeaderFragment.newInstance()
        likertScaleFragment = LikertScaleFragment.newInstance()
        fillInTheBlankFragment = FillInTheBlankFragment.newInstance()
        multipleChoiceFragment = MultipleChoiceFragment.newInstance()
        singleMultipleAnswersFragment = SingleMultipleAnswersFragment.newInstance()
        openEndedTextResponsesFragment = OpenEndedTextResponsesFragment.newInstance()
        timeDurationFragment = TimeDurationFragment.newInstance()
        sliderScaleFragment = SliderScaleFragment.newInstance()
        footerFragment = FooterFragment.newInstance()

        if (theAssignment != null){
            setSurvey(theAssignment!!)
        }else{
            showErrorPopup()
        }
    }

    private fun confirmCloseSurvey() {
        if (currentPage.type == HEADER_VIEW){
            closeTheSurvey()
        }else {
            val alertFm = supportFragmentManager.beginTransaction()
            val width = (mBind.surveyContainerLayout.measuredWidth * 0.75).toInt()
            val oneChoicePopup = OneChoicePopup.show(
                width = width,
                title = getString(R.string.closeTheSurvey),
                infoText = getString(R.string.doYouWantToCloseTheSurvey),
                okButtonText = getString(R.string.closeTheSurvey),
                cancelButtonText = getString(R.string.cancel),
                placecenter = true,
                cancelable = true
            )
            oneChoicePopup.setCompleteListener(object : OnBoolPopupReturnListener {
                override fun popupReturn(value: Boolean) {
                    if (value) {
                        closeTheSurvey()
                    }
                }
            })
            oneChoicePopup.show(alertFm, "oneChoicePopup")
        }
    }

    private fun closeTheSurvey(){
        finish()
    }

    private fun setSurvey(assignment: Assignment){
        val temp = assignment.survey.questions?.toMutableList()
        if (!temp.isNullOrEmpty()) {
            questionList = temp
            showQuestion(questionList.first().index)
        }else{
            //no questions - close Survey
            showErrorPopup()
        }
    }

    private fun showErrorPopup(){
        val alertFm = supportFragmentManager.beginTransaction()
        val width = (mBind.surveyContainerLayout.measuredWidth * 0.75).toInt()
        val alertPopup = PopupAlert.show(
            width = width,
            title = getString(R.string.error_title),
            textViewText = getString(R.string.survey_display_error),
            placecenter = true
        )
        alertPopup.setCompleteListener(object : OnBoolPopupReturnListener{
            override fun popupReturn(value: Boolean) {
                confirmCloseSurvey()
                //TODO: send info to backend
            }
        })
        alertPopup.show(alertFm, "alertPopup")
    }

    private fun showQuestion(index: Int){
        println("showQuestion: $index")
        if (questionList.size > index) {

            for (question in questionList) {
                if (question.index == index) {
                    currentPage = question
                    val existingAnswer = answer[question.index]
                    when (question.type) {

                        HEADER_VIEW -> {
                            headerFragment.question = question
                            loadFragment(headerFragment)
                            headerFragment.setText()
                        }
                        LIKERT_SCALES -> {
                            likertScaleFragment.question = question
                            loadFragment(likertScaleFragment)
                            likertScaleFragment.theAnswer = existingAnswer
                            likertScaleFragment.setQuestion()
                        }
                        FILL_IN_THE_BLANK -> {
                            fillInTheBlankFragment.theQuestion = question
                            loadFragment(fillInTheBlankFragment)
                            fillInTheBlankFragment.theAnswer = existingAnswer
                            fillInTheBlankFragment.setQuestion()
                        }
                        MULTIPLE_CHOICE -> {
                            multipleChoiceFragment.theQuestion = question
                            loadFragment(multipleChoiceFragment)
                            multipleChoiceFragment.theAnswer = existingAnswer
                            multipleChoiceFragment.setQuestion()
                        }
                        SINGLE_MULTIPLE_ANSWERS -> {
                            singleMultipleAnswersFragment.theQuestion = question
                            loadFragment(singleMultipleAnswersFragment)
                            singleMultipleAnswersFragment.theAnswer = existingAnswer
                            singleMultipleAnswersFragment.setQuestion()
                        }
                        OPEN_ENDED_TEXT_RESPONSES -> {
                            openEndedTextResponsesFragment.question = question
                            loadFragment(openEndedTextResponsesFragment)
                            openEndedTextResponsesFragment.theAnswer = existingAnswer
                            openEndedTextResponsesFragment.setQuestion()
                        }
                        TIME_DURATION -> {
                            timeDurationFragment.theQuestion = question
                            loadFragment(timeDurationFragment)
                            timeDurationFragment.theAnswer = existingAnswer
                            timeDurationFragment.setQuestion()
                        }
                        SLIDER_SCALE -> {
                            sliderScaleFragment.question = question
                            loadFragment(sliderScaleFragment)
                            sliderScaleFragment.theAnswer = existingAnswer
                            sliderScaleFragment.setQuestion()
                        }
                        FOOTER_VIEW -> {
                            footerFragment.question = question
                            loadFragment(footerFragment)
                            footerFragment.setText()
                        }
                    }
                    return
                }
            }
        }
    }

    private fun resetPreviousOfQuestions(){
        for ((index, question) in questionList.withIndex()){
            if (index == 0){
                question.previous = index
            }else question.previous = index - 1
        }
    }

    fun skipIsExecuted(current: Question) : Question?{
        current.skip?.also { skip ->
            // skip if ifChosen is -1 regardless of answer...
            if (skip.ifChosen == -1){
                return theAssignment?.survey?.questions?.first { it.index == skip.goto }
            }
            val answerObj = answer[current.index]
            if (answerObj != null){
                when (answerObj.type){
                    LIKERT_SCALES -> {
                        return if (skip.ifChosen == answerObj.likertAnswer){
                            theAssignment?.survey?.questions?.first { it.index == skip.goto }
                        }else null
                    }
                    SINGLE_MULTIPLE_ANSWERS -> {
                        return if (skip.ifChosen == answerObj.singleMultipleAnswer){
                            theAssignment?.survey?.questions?.first { it.index == skip.goto }
                        }else null
                    }
                    FILL_IN_THE_BLANK -> {
                        return if (skip.ifChosen == answerObj.fillBlankAnswer){
                            theAssignment?.survey?.questions?.first { it.index == skip.goto }
                        }else null
                    }
                    MULTIPLE_CHOICE -> {
                        return if (answerObj.multipleChoiceAnswer?.contains(skip.ifChosen) == true){
                            theAssignment?.survey?.questions?.first { it.index == skip.goto }
                        }else null
                    }
                    TIME_DURATION -> {
                        return if (skip.ifChosen == answerObj.timeDurationAnswer){
                            theAssignment?.survey?.questions?.first { it.index == skip.goto }
                        }else null
                    }
                    else -> return null
                }
            }else return null
        } ?: run {
            return null
        }
        return null
    }

    private fun checkNext(current: Question){

        if (current.index + 1 < theAssignment!!.survey.questions?.size ?: 0){
            val next = theAssignment!!.survey.questions!![current.index + 1]
            if (next.includeIf != null){
                val includeIfIndexQuestion = theAssignment!!.survey.questions!![next.includeIf!!.ifIndex]
                if (next.includeIf!!.ifIndex == includeIfIndexQuestion.index){
                    val answer = answer[includeIfIndexQuestion.index] ?: Answer(type = includeIfIndexQuestion.type,index = includeIfIndexQuestion.index)
                    when (includeIfIndexQuestion.type) {
                        LIKERT_SCALES -> {
                            if (next.includeIf?.ifValue ?: -99 == answer.likertAnswer) {
                                next.previous = currentPage.index
                                showQuestion(next.index)
                            }else {
                                // dont show next - check following question
                                this.answer.remove(next.index)
                                checkNext(next)
                            }
                        }
                        SINGLE_MULTIPLE_ANSWERS -> {
                            if (next.includeIf?.ifValue ?: -99 == answer.singleMultipleAnswer){
                                next.previous = currentPage.index
                                showQuestion(next.index)
                            }else{
                                // dont show next - check following question
                                this.answer.remove(next.index)
                                checkNext(next)
                            }

                        }
                        FILL_IN_THE_BLANK -> {
                            if (next.includeIf?.ifValue ?: -99 == answer.fillBlankAnswer){
                                next.previous = currentPage.index
                                showQuestion(next.index)
                            }else{
                                // dont show next - check following question
                                this.answer.remove(next.index)
                                checkNext(next)
                            }
                        }

                        MULTIPLE_CHOICE -> {
                            if (answer.multipleChoiceAnswer?.contains(next.includeIf?.ifValue ?: -99) == true){
                                next.previous = currentPage.index
                                showQuestion(next.index)
                            }else{
                                // dont show next - check following question
                                this.answer.remove(next.index)
                                checkNext(next)
                            }
                        }
                        else -> {
                            next.previous = currentPage.index
                            showQuestion(next.index)
                        }
                    }
                }else{
                    //next includeIf:ifIndex is not current index - show next
                    next.previous = currentPage.index
                    showQuestion(next.index)
                }
            }else{
                //next does not hanve includeIf - show next
                next.previous = currentPage.index
                showQuestion(next.index)
            }
        }else if (current.index + 1 == theAssignment!!.survey.questions?.size){
            //next is last (footer) - dont check, show direct
            if (theAssignment!!.survey.questions?.size ?: 0 > current.index + 1){
                theAssignment!!.survey.questions!![current.index + 1].previous = currentPage.index
                showQuestion(theAssignment!!.survey.questions!![current.index + 1].index)
            }
        }
    }
    // OnQuestionInteractionListener

    override fun nextQuestion(current: Question) {
        if(theAssignment != null){
            theAssignment!!.survey.questions?.sortedBy { it.index }
            skipIsExecuted(current)?.also { skipToQuestion ->
                skipToQuestion.previous = currentPage.index
                showQuestion(skipToQuestion.index)
            } ?: run {
                checkNext(current = current)
            }
        }
    }

    override fun prevoiusQuestion(current: Question) {
        if (theAssignment != null){
            for (q in theAssignment!!.survey.questions!!) {
            if (q.index == current.previous){
                showQuestion(q.index)
            }
        }
        }
    }

    override fun closeSurvey() {
        closeTheSurvey()
    }

    override fun sendInSurvey() {
        if (answer.isNotEmpty()){
            if (theAssignment?.expireAt?.toDate()?.after(Date()) == false && !inTestMode){

                // expired - show popup
                val alertFm = supportFragmentManager.beginTransaction()
                val width = (mBind.surveyContainerLayout.measuredWidth * 0.75).toInt()
                val alertPopup = PopupAlert.show(
                    width = width,
                    title = getString(R.string.survey_expired),
                    textViewText = theAssignment?.expireAt?.toDate()?.formatToReadable(getString(R.string.dateFormat)) ?: getString(R.string.noDate),
                    placecenter = true,
                    cancelable = false
                )
                alertPopup.setCompleteListener(object : OnBoolPopupReturnListener{
                    override fun popupReturn(value: Boolean) {
                        finish()
                    }
                })
                alertPopup.show(alertFm, "alertPopup")
            }else{

                //not expired (or in test mode) - send in answers
                val tempList = theAssignment?.survey?.questions?.sortedBy {it.index }
                val answersToInclude = mutableListOf<Int>()

                var counter = tempList?.last()?.index ?: -99
                if (counter != -99){
                    while (counter > 0) {
                        val currentQuestion = tempList?.first { it.index == counter }
                        if (currentQuestion?.type ?: "header" != "header" &&
                            currentQuestion?.type ?: "footer" != "footer"){
                            answersToInclude.add(counter)
                        }
                        counter = currentQuestion?.previous ?: 0
                    }
                }
                val tempAnswers = mutableMapOf<Int,Answer>()
                for (a in answersToInclude){
                    val theAnswer = answer.filter { it.value.index == a }
                    if (theAnswer.size == 1){
                        theAnswer.forEach {
                            tempAnswers[it.value.index] = it.value
                        }
                    }
                }
                println("tempAnswers: $tempAnswers")
                viewModel.postAnswer(tempAnswers)
                finish()
            }
        }else {
            finish()
        }
    }

    override fun setSingleMultipleAnswer(selected: Int) {
        answer[currentPage.index] = Answer(
            type = SINGLE_MULTIPLE_ANSWERS,
            index = currentPage.index,
            likertAnswer = null,
            fillBlankAnswer = null,
            multipleChoiceAnswer = null,
            singleMultipleAnswer = selected,
            openEndedAnswer = null,
            timeDurationAnswer = null,
            sliderScaleAnswer = null
        )
    }

    override fun setMultipleAnswersAnswer(selected: List<Int>?) {
        answer[currentPage.index] = Answer(
            type = MULTIPLE_CHOICE,
            index = currentPage.index,
            likertAnswer = null,
            fillBlankAnswer = null,
            multipleChoiceAnswer = selected?.toMutableList(),
            singleMultipleAnswer = null,
            openEndedAnswer = null,
            timeDurationAnswer = null,
            sliderScaleAnswer = null
        )
    }

    override fun setLikertAnswer(selected: Int) {
        answer[currentPage.index] = Answer(
            type = LIKERT_SCALES,
            index = currentPage.index,
            likertAnswer = selected,
            fillBlankAnswer = null,
            multipleChoiceAnswer = null,
            singleMultipleAnswer = null,
            openEndedAnswer = null,
            timeDurationAnswer = null,
            sliderScaleAnswer = null
        )
    }

    override fun setOpenEndedAnswer(text: String?) {
        answer[currentPage.index] = Answer(
            type = OPEN_ENDED_TEXT_RESPONSES,
            index = currentPage.index,
            likertAnswer = null,
            fillBlankAnswer = null,
            multipleChoiceAnswer = null,
            singleMultipleAnswer = null,
            openEndedAnswer = text,
            timeDurationAnswer = null,
            sliderScaleAnswer = null
        )
    }

    override fun setFillBlankAnswer(selected: Int?) {
        answer[currentPage.index] = Answer(
            type = FILL_IN_THE_BLANK,
            index = currentPage.index,
            likertAnswer = null,
            fillBlankAnswer = selected,
            multipleChoiceAnswer = null,
            singleMultipleAnswer = null,
            openEndedAnswer = null,
            timeDurationAnswer = null,
            sliderScaleAnswer = null
        )
    }

    override fun setTimeDurationAnswer(selected: Int) {
        answer[currentPage.index] = Answer(
            type = TIME_DURATION,
            index = currentPage.index,
            likertAnswer = null,
            fillBlankAnswer = null,
            multipleChoiceAnswer = null,
            singleMultipleAnswer = null,
            openEndedAnswer = null,
            timeDurationAnswer = selected,
            sliderScaleAnswer = null
        )
    }

    override fun setSliderAnswer(selected: Int, naButton: Boolean) {
        var theValue = selected
        if (naButton) {
            theValue = -1
        }
        answer[currentPage.index] = Answer(
            type = SLIDER_SCALE,
            index = currentPage.index,
            likertAnswer = null,
            fillBlankAnswer = null,
            multipleChoiceAnswer = null,
            singleMultipleAnswer = null,
            openEndedAnswer = null,
            timeDurationAnswer = null,
            sliderScaleAnswer = theValue
        )
    }

    companion object {
        const val IN_TEST_MODE = "InTestMode"
        const val ASSIGNMENT = "assignment"
        const val HEADER_VIEW = "header"
        const val LIKERT_SCALES = "likert"
        const val FILL_IN_THE_BLANK = "blanks"
        const val MULTIPLE_CHOICE = "multi"
        const val SINGLE_MULTIPLE_ANSWERS = "single"
        const val OPEN_ENDED_TEXT_RESPONSES = "open"
        const val TIME_DURATION = "duration"
        const val SLIDER_SCALE = "slider"
        const val FOOTER_VIEW = "footer"

        fun start(context: Context, assignment: Assignment, isInTestMode: Boolean){
            context.startActivity(Intent(context, SurveyContainerActivity::class.java).apply {
                this.putExtra(ASSIGNMENT,assignment)
                this.putExtra(IN_TEST_MODE, isInTestMode)
            })
        }
    }
}
