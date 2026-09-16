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
