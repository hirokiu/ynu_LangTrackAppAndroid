package com.alchembright.dev.langtrackapp

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.alchembright.dev.langtrackapp.data.model.*
import com.alchembright.dev.langtrackapp.screen.main.SurveyAdapter
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity
import com.alchembright.dev.langtrackapp.util.AnswerReview
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeParityTest {
    @Test fun answerReviewUsesLabelsWithoutChangingValues() {
        val question = Question(type = "multi", multipleChoisesAnswers = mutableListOf("日本語", "English", "Other"))
        val answer = Answer(type = "multi", multipleChoiceAnswer = mutableListOf(0, 2))
        assertEquals("日本語、Other", AnswerReview.value(question, answer, "missing"))
        assertEquals(listOf(0, 2), answer.multipleChoiceAnswer)
        assertEquals("5", AnswerReview.value(Question(type = "likert"), Answer(likertAnswer = 4), "missing"))
        assertEquals("1:30", AnswerReview.value(Question(type = "duration"), Answer(timeDurationAnswer = 5400), "missing"))
        assertEquals("該当なし", AnswerReview.value(Question(type = "slider"), Answer(sliderScaleAnswer = -1), "missing", "該当なし"))
        assertEquals("missing", AnswerReview.value(Question(type = "single"), Answer(singleMultipleAnswer = 9), "missing"))
    }

    @Test fun achievementRingRemainsWithActiveSurvey() {
        val adapter = SurveyAdapter()
        adapter.setAssignments(listOf(fixture()))
        assertEquals(2, adapter.itemCount)
        assertEquals(1, adapter.getNumberOfUnanswered())
        assertEquals(0, adapter.getNumberOfAnswered())
    }

    @Test fun everyQuestionTypeInflatesWithMaterialTheme() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assignment = fixture()
        ActivityScenario.launch<SurveyContainerActivity>(Intent(context, SurveyContainerActivity::class.java)
            .putExtra("assignment", assignment).putExtra(SurveyContainerActivity.IN_TEST_MODE, true)).use { scenario ->
            scenario.onActivity { activity -> assertNotNull(activity.assignmentDetails()) }
            onView(withId(R.id.surveyToolbar)).check(matches(isDisplayed()))
            assignment.survey.questions!!.dropLast(1).forEach { current ->
                scenario.onActivity { activity -> activity.nextQuestion(current) }
                onView(withId(R.id.surveyHeading)).check(matches(isDisplayed()))
                val next = assignment.survey.questions!!.firstOrNull { it.index == current.index + 1 }
                val field = when (next?.type) {
                    "likert" -> R.id.likertScaleRadioGroup
                    "open" -> R.id.openEditText
                    "single" -> R.id.singleMultipleAnswerContainer
                    "multi" -> R.id.multipleRadioButtonContainer
                    else -> null
                }
                if (field != null) onView(withId(field)).check(matches(isDisplayed()))
                val nextButton = when (next?.type) {
                    "likert" -> R.id.likertScaleNextButton
                    "open" -> R.id.openEndedTextNextButton
                    "single" -> R.id.singleMultipleAnswerNextButton
                    "multi" -> R.id.multipleChoiseFragmentNextButton
                    "blanks" -> R.id.fillInTheBlankNextButton
                    "duration" -> R.id.timeDurationNextButton
                    "slider" -> R.id.sliderScaleNextButton
                    else -> R.id.footerNextButton
                }
                onView(withId(nextButton)).check(matches(isCompletelyDisplayed()))
            }
            onView(withId(R.id.footerNextButton)).check(matches(isDisplayed()))
        }
    }

    @Test fun selectedLikertSurvivesActivityRecreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assignment = fixture()
        ActivityScenario.launch<SurveyContainerActivity>(Intent(context, SurveyContainerActivity::class.java)
            .putExtra(SurveyContainerActivity.ASSIGNMENT, assignment)
            .putExtra(SurveyContainerActivity.IN_TEST_MODE, true)).use { scenario ->
            scenario.onActivity { it.nextQuestion(assignment.survey.questions!![0]) }
            onView(withId(R.id.likertScaleRadioButton5)).perform(androidx.test.espresso.action.ViewActions.click())
            scenario.recreate()
            onView(withId(R.id.likertScaleRadioButton5)).check(matches(isChecked()))
            onView(withId(R.id.likertScaleNextButton)).check(matches(isEnabled()))
        }
    }

    @Test fun blankSelectionKeepsOriginalOptionsAndAnswerLabel() {
        checkBlankSelection(valid = true)
    }

    @Test fun malformedBlankDoesNotCrashOrEnableNext() {
        checkBlankSelection(valid = false)
    }

    private fun checkBlankSelection(valid: Boolean) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assignment = fixture()
        if (!valid) assignment.survey.questions!![5].text = "Missing marker"
        ActivityScenario.launch<SurveyContainerActivity>(Intent(context, SurveyContainerActivity::class.java)
            .putExtra(SurveyContainerActivity.ASSIGNMENT, assignment)
            .putExtra(SurveyContainerActivity.IN_TEST_MODE, true)).use { scenario ->
            assignment.survey.questions!!.take(5).forEach { current ->
                scenario.onActivity { it.nextQuestion(current) }
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            }
            scenario.onActivity { it.findViewById<android.widget.Spinner>(R.id.choice_spinner).setSelection(2) }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                assertEquals(valid, activity.findViewById<android.view.View>(R.id.fillInTheBlankNextButton).isEnabled)
                assertEquals(listOf("A", "B"), activity.assignmentDetails()!!.survey.questions!![5].fillBlanksChoises)
                if (valid) assertTrue(activity.findViewById<android.widget.TextView>(R.id.fillInTheBlankTextView).text.toString().contains("B"))
            }
        }
    }

    @Test fun durationIsCommittedWhenAdvancingWithoutScrollEvents() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assignment = fixture()
        ActivityScenario.launch<SurveyContainerActivity>(Intent(context, SurveyContainerActivity::class.java)
            .putExtra(SurveyContainerActivity.ASSIGNMENT, assignment)
            .putExtra(SurveyContainerActivity.IN_TEST_MODE, true)).use { scenario ->
            assignment.survey.questions!!.take(6).forEach { current ->
                scenario.onActivity { it.nextQuestion(current) }
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            }
            scenario.onActivity { activity ->
                activity.findViewById<android.widget.NumberPicker>(R.id.number_picker_hour).value = 1
                activity.findViewById<android.widget.NumberPicker>(R.id.number_picker_minutes).value = 1
                activity.findViewById<android.view.View>(R.id.timeDurationNextButton).performClick()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { it.nextQuestion(assignment.survey.questions!![7]) }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { assertTrue(it.answerReview().contains("1:05")) }
        }
    }

    private fun fixture() = Assignment(survey = Survey(name = "fixture", id = "fixture", title = "UI確認", questions = listOf(
        Question(type = "header", index = 0, title = "UI確認", text = "開始"),
        Question(type = "likert", index = 1, text = "5件法", likertMin = "低", likertMax = "高"),
        Question(type = "open", index = 2, text = "自由入力"),
        Question(type = "single", index = 3, text = "単一選択", singleMultipleAnswers = mutableListOf("A", "B")),
        Question(type = "multi", index = 4, text = "複数選択", multipleChoisesAnswers = mutableListOf("A", "B")),
        Question(type = "blanks", index = 5, text = "私は _____ を選びました。", fillBlanksChoises = mutableListOf("A", "B")),
        Question(type = "duration", index = 6, text = "時間"),
        Question(type = "slider", index = 7, text = "スライダー"),
        Question(type = "footer", index = 8, text = "確認")
    ), answer = null, updatedAt = "", createdAt = ""), updatedAt = "", createdAt = "", userId = "fixture", dataset = null,
        publishAt = "2020-01-01T00:00:00.000Z", expireAt = "2099-01-01T00:00:00.000Z", id = "fixture")
}
