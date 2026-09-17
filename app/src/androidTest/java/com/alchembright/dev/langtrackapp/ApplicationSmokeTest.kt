package com.alchembright.dev.langtrackapp

import android.os.Parcel
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.screen.login.LoginActivity
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationSmokeTest {
    @Test fun loginScreenOpensWithoutSubmittingCredentials() {
        ActivityScenario.launch(LoginActivity::class.java).use {
            onView(withId(R.id.logInEmailEditText)).check(matches(isDisplayed()))
            onView(withId(R.id.logInPasswordEditText)).check(matches(isDisplayed()))
            onView(withId(R.id.logInButton)).check(matches(isDisplayed()))
        }
    }

    @Test fun japaneseResourcesAndArgumentOrder() {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = android.content.res.Configuration(context.resources.configuration)
        configuration.setLocale(java.util.Locale.JAPANESE)
        val japanese = context.createConfigurationContext(configuration)
        assertEquals("パスワード", japanese.getString(R.string.password))
        assertEquals("全10件のアンケートのうち3件に回答しました。",
            japanese.getString(R.string.youHaveAnsweredWithFormate, "3", "10"))
        org.junit.Assert.assertTrue(japanese.getString(R.string.menuProjectAbout).contains("KIROKUN アプリ"))
    }

    @Test fun answersSurviveParcelRoundTrip() {
        val original = Answer(type = "multi", index = 4,
            multipleChoiceAnswer = mutableListOf(0, 2), openEndedAnswer = "日本語")
        val parcel = Parcel.obtain()
        try {
            original.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            assertEquals(original, parcelableCreator<Answer>().createFromParcel(parcel))
        } finally {
            parcel.recycle()
        }
    }
}
