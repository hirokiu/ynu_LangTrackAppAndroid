package com.alchembright.dev.langtrackapp.screen.overview

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.data.model.Assignment
import com.alchembright.dev.langtrackapp.data.model.OverviewListItem
import com.alchembright.dev.langtrackapp.databinding.OverviewActivityBinding
import com.alchembright.dev.langtrackapp.screen.overview.overviewQuestionViews.*
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity.Companion.FILL_IN_THE_BLANK
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity.Companion.FOOTER_VIEW
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity.Companion.HEADER_VIEW
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity.Companion.LIKERT_SCALES
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity.Companion.MULTIPLE_CHOICE
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity.Companion.OPEN_ENDED_TEXT_RESPONSES
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity.Companion.SINGLE_MULTIPLE_ANSWERS
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity.Companion.SLIDER_SCALE
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity.Companion.TIME_DURATION
import com.alchembright.dev.langtrackapp.util.formatToReadable
import com.alchembright.dev.langtrackapp.util.toDate
import java.lang.Exception

class OverviewActivity : AppCompatActivity() {

    lateinit var binding: OverviewActivityBinding
    private lateinit var viewModel :OverviewViewModel
    var topViewIsShowing = false
    var theAssignment: Assignment? = null
    var questionsWithAnswers = mutableListOf<OverviewListItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.overview_activity)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        viewModel = ViewModelProviders.of(this,
            OverviewViewModelFactory(this)
        ).get(OverviewViewModel::class.java)
        theAssignment = intent.getParcelableExtra(ASSIGNMENT)

        if (theAssignment != null) {
            filterList()
            presentQuestionsInScrollview()
        }

        binding.overviewTopInfoTextView.text = theAssignment?.survey?.title ?: getString(R.string.noTitle)
        //binding.overviewTopNumberOfQuestionTextView.text = "${theAssignment?.survey?.questions?.size ?: 0} st"
        binding.overviewTopPublishedTextView.text = theAssignment?.publishAt?.toDate()?.formatToReadable(getString(R.string.dateFormat)) ?: getString(R.string.noDate)
        binding.overviewTopExpiredTextView.text = theAssignment?.dataset?.updatedAt?.toDate()?.formatToReadable(getString(R.string.dateFormat)) ?: getString(R.string.noDate)
        binding.overviewTopOkButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun filterList(){
        if (theAssignment != null){
            val header = theAssignment!!.survey.questions?.find { it.type == HEADER_VIEW }
            if (header != null){
                val answer = Answer(type = HEADER_VIEW, index = header.index)
                questionsWithAnswers.add(OverviewListItem(header,answer))
            }
            for (que in theAssignment!!.survey.questions!!){

                val answers = theAssignment!!.dataset?.answers
                if (answers != null) {
                    if (que.type != FOOTER_VIEW) {
                        val answer = answers.find { it.index == que.index }
                        if (answer != null){
                            questionsWithAnswers.add(
                                OverviewListItem(
                                    question = que,
                                    answer = answer
                                )
                            )
                        }
                    }
                }
            }
            //MARK: add footer too?
        }
        questionsWithAnswers.sortedBy { it.question.index }
        binding.overviewTopNumberOfQuestionTextView.text = (questionsWithAnswers.size - 1).toString()//getString(R.string.numberEnding,questionsWithAnswers.size - 1)


    }

    private fun presentQuestionsInScrollview(){
        if (!theAssignment!!.survey.questions.isNullOrEmpty()) {

            for (listItem in questionsWithAnswers) {

                val selectedAnswerIndex = if(!theAssignment!!.dataset?.answers.isNullOrEmpty())
                    theAssignment!!.dataset!!.answers.indexOfFirst { it.index == listItem.question.index } else null
                var selectedAnswer: Answer? = null
                try {
                    selectedAnswer = theAssignment!!.dataset!!.answers[selectedAnswerIndex!!]
                } catch (e: Exception){
                    println("presentQuestionsInScrollview, e: ${e.localizedMessage}")}

                when (listItem.question.type) {
                    HEADER_VIEW -> {
                        val header = OverviewHeaderView(this)
                        header.setText(listItem.question)
                        binding.overviewQuestionContainer.addView(header)
                    }
                    LIKERT_SCALES -> {
                        val likert = OverviewLikertView(this)
                        if (selectedAnswer?.likertAnswer ?: -1 != -1 &&
                                selectedAnswer?.likertAnswer ?: -1 >= 0 &&
                                selectedAnswer?.likertAnswer ?: -1 < 6){
                            likert.setText(listItem.question, selectedAnswer?.likertAnswer!!)
                        }
                        binding.overviewQuestionContainer.addView(likert)
                    }
                    FILL_IN_THE_BLANK -> {
                        val likert = OverviewFillInBlankView(this)
                        val selectedWord =
                            if (selectedAnswer != null){
                                try {
                                    listItem.question.fillBlanksChoises?.get(selectedAnswer.fillBlankAnswer!!)
                                }catch (e: Exception){null}
                            }  else null
                        likert.setText(listItem.question, selectedWord)
                        binding.overviewQuestionContainer.addView(likert)
                    }
                    MULTIPLE_CHOICE -> {
                        val likert = OverviewMultipleChoiceView(this)
                        val templist = mutableListOf<String>()
                        if (selectedAnswer?.multipleChoiceAnswer != null){
                            for (wordIndex in selectedAnswer.multipleChoiceAnswer!!) {
                                try {
                                    templist.add(listItem.question.multipleChoisesAnswers?.get(wordIndex)!!)
                                } catch (e: Exception) {
                                    println("presentQuestionsInScrollview, e: ${e.localizedMessage}")
                                }
                            }
                        }
                        likert.setText(listItem.question, templist)
                        binding.overviewQuestionContainer.addView(likert)
                    }
                    SINGLE_MULTIPLE_ANSWERS -> {
                        val likert = OverviewSingleMultipleView(this)
                        var theChoice: String? = null
                        if (selectedAnswer?.singleMultipleAnswer != null &&
                            listItem.question.singleMultipleAnswers != null) {
                            try {
                                theChoice =
                                    listItem.question.singleMultipleAnswers!!.get(selectedAnswer.singleMultipleAnswer!!)
                            }catch (e: Exception){println("presentQuestionsInScrollview, e: ${e.localizedMessage}")}

                        }
                        likert.setText(listItem.question, theChoice)
                        binding.overviewQuestionContainer.addView(likert)
                    }
                    OPEN_ENDED_TEXT_RESPONSES -> {
                        val likert = OverviewOpenEndedView(this)
                        var theText = ""
                        if (selectedAnswer?.openEndedAnswer != null) {
                            try {
                                theText = selectedAnswer.openEndedAnswer ?: ""
                            }catch (e: Exception){println("presentQuestionsInScrollview, e: ${e.localizedMessage}")}
                        }
                        likert.setText(listItem.question, theText)
                        binding.overviewQuestionContainer.addView(likert)
                    }
                    TIME_DURATION -> {
                        var selectedTime = -99
                        val duration = OverviewTimDurationView(this)
                        if (selectedAnswer?.timeDurationAnswer != null){
                            try {
                                selectedTime = selectedAnswer.timeDurationAnswer ?: -99
                            }catch (e: Exception){println("presentQuestionsInScrollview, e: ${e.localizedMessage}")}
                        }
                        duration.setText(listItem.question, selectedAnswer)
                        binding.overviewQuestionContainer.addView(duration)
                    }
                    SLIDER_SCALE -> {
                        var value = -1
                        val sliderScale = OverviewSliderScaleView(this)
                        if (selectedAnswer?.sliderScaleAnswer != null){
                            try {
                                value = selectedAnswer.sliderScaleAnswer ?: -99
                            }catch (e: Exception){println("presentQuestionsInScrollview, e: ${e.localizedMessage}")}
                        }
                        sliderScale.setText(listItem.question, value)
                        binding.overviewQuestionContainer.addView(sliderScale)
                    }
                }
            }
        }
    }

    companion object {
        const val ASSIGNMENT = "overviewsurvey"

        fun start(context: Context, assignment: Assignment) {
            context.startActivity(Intent(context, OverviewActivity::class.java).apply {
                this.putExtra(ASSIGNMENT, assignment)
            })
        }
    }
}
