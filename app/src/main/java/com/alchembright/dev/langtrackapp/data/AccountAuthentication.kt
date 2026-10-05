package com.alchembright.dev.langtrackapp.data

import android.os.Handler
import android.os.Looper
import com.alchembright.dev.langtrackapp.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Authentication capabilities belong to the selected build's server, never another environment. */
class AccountAuthentication {
    private val auth = FirebaseAuth.getInstance()
    private val main = Handler(Looper.getMainLooper())
    private val client = OkHttpClient.Builder().followRedirects(false).followSslRedirects(false)
        .callTimeout(20, TimeUnit.SECONDS).build()
    private var closed = false
    private fun deliver(block: () -> Unit) { main.post { if (!closed) block() } }
    fun close() { closed = true; client.dispatcher.cancelAll() }

    private fun request(path: String, body: JSONObject? = null, token: String? = null, result: (Int, JSONObject?) -> Unit) {
        val builder = Request.Builder().url(BuildConfig.API_BASE_URL + path)
        if (body != null) builder.post(body.toString().toRequestBody("application/json".toMediaType()))
        if (token != null) builder.header("token", token)
        client.newCall(builder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = deliver { result(0, null) }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val json = try { JSONObject(it.body?.string() ?: "") } catch (_: Exception) { null }
                    val code = it.code
                    deliver { result(code, json) }
                }
            }
        })
    }

    fun login(username: String, password: String, completed: (Boolean) -> Unit) {
        fun legacy() {
            val email = if (username.contains('@')) username else "$username@humlablu.com"
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                deliver { completed(it.isSuccessful) }
            }
        }
        if (BuildConfig.API_BASE_URL.isBlank() || username.contains('@')) { legacy(); return }
        request("auth/options") { status, options ->
            when (AccountLoginPolicy.options(status, options?.opt("usernameLogin") as? Boolean)) {
                AccountLoginPolicy.Route.LEGACY -> legacy()
                AccountLoginPolicy.Route.FAIL -> completed(false)
                AccountLoginPolicy.Route.USERNAME -> request("auth/username-login", JSONObject().put("username", username).put("password", password)) { code, response ->
                    if (AccountLoginPolicy.allowLegacyFallback(code)) legacy()
                    else {
                        val token = response?.opt("customToken") as? String
                        if (code != 200 || token.isNullOrBlank()) completed(false)
                        else auth.signInWithCustomToken(token).addOnCompleteListener { deliver { completed(it.isSuccessful) } }
                    }
                }
            }
        }
    }

    /** No local email-derived identity is accepted by Proto or Dev. */
    fun resolve(completed: (String?, String?) -> Unit) {
        val user = auth.currentUser ?: run { completed(null, null); return }
        user.getIdToken(false).addOnCompleteListener { task ->
            deliver {
                val token = if (task.isSuccessful) task.result?.token else null
                if (token.isNullOrBlank() || auth.currentUser?.uid != user.uid) { completed(null, null); return@deliver }
                if (BuildConfig.API_BASE_URL.isBlank()) {
                    completed(user.email?.substringBefore('@')?.takeIf { it.isNotBlank() }, token)
                } else request("me", token = token) { code, response ->
                    val id = (response?.opt("userId") as? String)?.takeIf { it.isNotBlank() }
                    if (code == 200 && id != null && auth.currentUser?.uid == user.uid) completed(id, token)
                    else completed(null, null)
                }
            }
        }
    }
}
