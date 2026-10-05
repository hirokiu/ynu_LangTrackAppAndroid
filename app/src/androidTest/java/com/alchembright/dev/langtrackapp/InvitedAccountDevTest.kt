package com.alchembright.dev.langtrackapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.Observer
import com.alchembright.dev.langtrackapp.data.AccountAuthentication
import com.alchembright.dev.langtrackapp.data.Repository
import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.data.model.Assignment
import com.alchembright.dev.langtrackapp.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Opt-in only: provision a private fixture in a disposable Dev emulator. */
@RunWith(AndroidJUnit4::class)
class InvitedAccountDevTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private fun <T> awaitCallback(start: ((T) -> Unit) -> Unit): T {
        val latch = CountDownLatch(1)
        var result: T? = null
        instrumentation.runOnMainSync { start { value -> result = value; latch.countDown() } }
        assertTrue("Timed out", latch.await(45, TimeUnit.SECONDS))
        @Suppress("UNCHECKED_CAST")
        return result as T
    }
    @Test fun usernameLoginAndAnswerRoundTrip() {
        val context = instrumentation.targetContext
        val file = File(context.filesDir, "kirokun-private-qa.json")
        assumeTrue("Dedicated Dev fixture not installed", file.exists())
        assertEquals("com.alchembright.kirokun.dev", context.packageName)
        assertEquals("kirokun-dev", FirebaseApp.getInstance().options.projectId)
        assertEquals("http://localhost:18082/api/", BuildConfig.API_BASE_URL)
        val fixture = JSONObject(file.readText())
        assertTrue(fixture.getString("username").startsWith("qa_auth_"))
        val auth = FirebaseAuth.getInstance()
        assertTrue("Dedicated emulator required", auth.currentUser == null || auth.currentUser?.uid == fixture.getString("uid"))
        val accounts = AccountAuthentication()
        val repository = Repository(context)
        try {
            assertTrue(awaitCallback<Boolean> { done -> accounts.login(fixture.getString("username"), fixture.getString("password"), done) })
            val identity = awaitCallback<Pair<String?, String?>> { done -> accounts.resolve { id, token -> done(Pair(id, token)) } }
            assertEquals(fixture.getString("userId"), identity.first)
            assertNotNull(identity.second)
            instrumentation.runOnMainSync {
                repository.setCurrentUser(User(id = identity.first!!))
                repository.idToken = identity.second!!
            }
            val id = fixture.getString("androidAssignment")
            fun readAnswer(expected: String? = null): Assignment {
                val latch = CountDownLatch(1)
                var item: Assignment? = null
                val observer = Observer<MutableList<Assignment>> { items ->
                    val found = items?.firstOrNull { it.id == id }
                    if (found != null && (expected == null || found.dataset?.answers?.firstOrNull { it.index == 1 }?.openEndedAnswer == expected)) {
                        item = found; latch.countDown()
                    }
                }
                instrumentation.runOnMainSync {
                    repository.assignmentListLiveData.observeForever(observer)
                    repository.getAssignments()
                }
                try { assertTrue("Assignment/answer not received", latch.await(30, TimeUnit.SECONDS)) }
                finally { instrumentation.runOnMainSync { repository.assignmentListLiveData.removeObserver(observer) } }
                return item!!
            }
            val assignment = readAnswer()
            instrumentation.runOnMainSync { repository.selectedAssignment = assignment }
            val value = "Android 招待回答 " + java.util.UUID.randomUUID()
            assertTrue(awaitCallback<Boolean> { done -> repository.postAnswer(mapOf(1 to Answer(type="open",index=1,openEndedAnswer=value)),done) })
            assertEquals(value, readAnswer(value).dataset?.answers?.first { it.index == 1 }?.openEndedAnswer)
        } finally {
            instrumentation.runOnMainSync { accounts.close(); auth.signOut(); repository.idToken = ""; repository.setCurrentUser(User()) }
        }
    }
}
