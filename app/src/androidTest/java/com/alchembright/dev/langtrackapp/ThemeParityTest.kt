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
            }
            onView(withId(R.id.footerNextButton)).check(matches(isDisplayed()))
        }
    }

    private fun fixture() = Assignment(survey = Survey(name = "fixture", id = "fixture", title = "UI確認", questions = listOf(
        Question(type = "header", index = 0, title = "UI確認", text = "開始"),
        Question(type = "likert", index = 1, text = "5件法", likertMin = "低", likertMax = "高"),
        Question(type = "open", index = 2, text = "自由入力"),
        Question(type = "single", index = 3, text = "単一選択", singleMultipleAnswers = mutableListOf("A", "B")),
        Question(type = "multi", index = 4, text = "複数選択", multipleChoisesAnswers = mutableListOf("A", "B")),
        Question(type = "blanks", index = 5, text = "穴埋め", fillBlanksChoises = mutableListOf("A", "B")),
        Question(type = "duration", index = 6, text = "時間"),
        Question(type = "slider", index = 7, text = "スライダー"),
        Question(type = "footer", index = 8, text = "確認")
    ), answer = null, updatedAt = "", createdAt = ""), updatedAt = "", createdAt = "", userId = "fixture", dataset = null,
        publishAt = "2020-01-01T00:00:00.000Z", expireAt = "2099-01-01T00:00:00.000Z", id = "fixture")
}
