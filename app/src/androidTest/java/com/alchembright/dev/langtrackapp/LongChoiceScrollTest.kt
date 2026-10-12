package com.alchembright.dev.langtrackapp

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.alchembright.dev.langtrackapp.data.model.*
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LongChoiceScrollTest {
    @Test fun actualSurveyLongChoicesReachLastOption() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val json = try { JSONObject(instrumentation.context.assets.open("scroll-survey.json").bufferedReader().use { it.readText() }) }
        catch (_: java.io.FileNotFoundException) {
            JSONObject().put("questions", org.json.JSONArray().apply {
                repeat(6) { index -> put(JSONObject().put("type", if(index == 5) "multi" else "single")
                    .put("text", "長い選択肢を最後までスクロールし、選んで次へ進めるかを確認します。")
                    .put("values", org.json.JSONArray((1..30).map { "Choice $it / 長い選択肢の折り返しとスクロールを確認するためのテスト用の文章です。" }))) }
            })
        }
        for (index in listOf(1, 5)) {
            val source = json.getJSONArray("questions").getJSONObject(index)
            val values = source.getJSONArray("values").let { a -> (0 until a.length()).map { a.getString(it) }.toMutableList() }
            val type = source.getString("type")
            val header = Question(type="header", index=0, text="Local test")
            val q = Question(type=type, index=1, text=source.getString("text"), singleMultipleAnswers=values, multipleChoisesAnswers=values)
            val footer = Question(type="footer", index=2, text="Complete")
            val assignment = Assignment(survey=Survey(name="local-scroll", id="fixture", title="Scroll test", questions=listOf(header,q,footer), answer=null, updatedAt="", createdAt=""), updatedAt="", createdAt="", userId="fixture", dataset=null, publishAt="2020-01-01T00:00:00.000Z", expireAt="2099-01-01T00:00:00.000Z", id="fixture")
            ActivityScenario.launch<SurveyContainerActivity>(Intent(instrumentation.targetContext, SurveyContainerActivity::class.java).putExtra(SurveyContainerActivity.ASSIGNMENT, assignment).putExtra(SurveyContainerActivity.IN_TEST_MODE,true)).use { scenario ->
                scenario.onActivity { it.nextQuestion(header) }
                onView(withText(values.last())).perform(scrollTo()).check(matches(isCompletelyDisplayed())).perform(click())
                val next = if (type=="single") R.id.singleMultipleAnswerNextButton else R.id.multipleChoiseFragmentNextButton
                onView(withId(next)).check(matches(isCompletelyDisplayed())).check(matches(isEnabled())).perform(click())
                onView(withId(R.id.footerNextButton)).check(matches(isDisplayed()))
            }
        }
    }
}
