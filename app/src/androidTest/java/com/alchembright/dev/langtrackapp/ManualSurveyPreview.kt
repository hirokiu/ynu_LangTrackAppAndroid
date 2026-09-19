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
class ManualSurveyPreview {
    /** Explicitly opt in for manual CUA verification. No network writes in test mode. */
    @Test fun manualSurveyPreview() {
        org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("manualPreview") == "true")
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags("ja"))
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<SurveyContainerActivity>(Intent(context, SurveyContainerActivity::class.java)
            .putExtra(SurveyContainerActivity.ASSIGNMENT, fixture())
            .putExtra(SurveyContainerActivity.IN_TEST_MODE, true)).use { scenario ->
            val previewIndex = InstrumentationRegistry.getArguments().getString("previewIndex")?.toIntOrNull()?.coerceIn(0, 8) ?: 0
            val questions = fixture().survey.questions!!
            for (question in questions.take(previewIndex)) {
                scenario.onActivity { activity ->
                    when (question.type) {
                        "likert" -> activity.setLikertAnswer(4)
                        "open" -> activity.setOpenEndedAnswer("Local UI test")
                        "single" -> activity.setSingleMultipleAnswer(1)
                        "multi" -> activity.setMultipleAnswersAnswer(listOf(0, 1))
                        "blanks" -> activity.setFillBlankAnswer(1)
                        "duration" -> activity.setTimeDurationAnswer(5400)
                        "slider" -> activity.setSliderAnswer(75, false)
                    }
                    activity.nextQuestion(question)
                }
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            }
            val deadline = System.currentTimeMillis() + 900_000
            while (System.currentTimeMillis() < deadline && scenario.state != androidx.lifecycle.Lifecycle.State.DESTROYED) {
                Thread.sleep(500)
            }
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
