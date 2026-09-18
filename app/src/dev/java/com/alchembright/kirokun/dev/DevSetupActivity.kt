package com.alchembright.kirokun.dev

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.alchembright.dev.langtrackapp.BuildConfig
import com.alchembright.dev.langtrackapp.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class DevSetupActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private lateinit var signIn: Button
    private lateinit var signOut: Button
    private lateinit var retry: Button
    private lateinit var spinner: ProgressBar
    private val auth get() = FirebaseAuth.getInstance()
    private val client = OkHttpClient.Builder().followRedirects(false).followSslRedirects(false).callTimeout(15, TimeUnit.SECONDS).build()
    private var generation = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        check(FirebaseApp.getInstance().options.projectId == "kirokun-dev" && packageName == "com.alchembright.kirokun.dev") { "Dev Firebase configuration mismatch" }
        status = TextView(this).apply { text = getString(R.string.dev_login_intro); textSize = 18f }
        signIn = Button(this).apply { setText(R.string.dev_google_sign_in); setOnClickListener { googleSignIn() } }
        signOut = Button(this).apply { setText(R.string.dev_sign_out); setOnClickListener { generation++; auth.signOut(); status.setText(R.string.dev_login_intro); render(false) } }
        retry = Button(this).apply { setText(R.string.dev_retry_connection); setOnClickListener { checkConnection() } }
        spinner = ProgressBar(this)
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(48, 150, 48, 48)
            addView(TextView(this@DevSetupActivity).apply { text = "KIROKUN Dev"; textSize = 28f })
            addView(status); addView(signIn); addView(retry); addView(signOut); addView(spinner)
        })
        render(false)
        if (auth.currentUser != null) checkConnection()
    }
    private fun render(busy: Boolean) {
        spinner.visibility = if (busy) View.VISIBLE else View.GONE
        signIn.isEnabled = !busy; retry.isEnabled = !busy; signOut.isEnabled = !busy
        val signedIn = auth.currentUser != null
        signIn.visibility = if (signedIn) View.GONE else View.VISIBLE
        signOut.visibility = if (signedIn) View.VISIBLE else View.GONE
        retry.visibility = if (signedIn) View.VISIBLE else View.GONE
    }
    private fun googleSignIn() {
        render(true)
        val option = GetSignInWithGoogleOption.Builder(getString(R.string.default_web_client_id)).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        CredentialManager.create(this).getCredentialAsync(this, request, null, ContextCompat.getMainExecutor(this), object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
            override fun onResult(result: GetCredentialResponse) {
                try {
                    val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                    auth.signInWithCredential(GoogleAuthProvider.getCredential(credential.idToken, null)).addOnCompleteListener(this@DevSetupActivity) {
                        if (it.isSuccessful) checkConnection() else failed()
                    }
                } catch (_: Exception) { failed() }
            }
            override fun onError(e: GetCredentialException) { failed() }
        })
    }
    private fun failed() { if (!isDestroyed) { status.setText(R.string.dev_login_failed); render(false) } }
    private fun checkConnection() {
        val user = auth.currentUser ?: return
        val requestGeneration = ++generation
        render(true); status.setText(R.string.dev_connecting)
        user.getIdToken(true).addOnCompleteListener(this) { task ->
            val token = if (task.isSuccessful) task.result?.token else null
            if (token == null) { failed(); return@addOnCompleteListener }
            // Server validates Firebase UID against its allowlist; never derive identity from email.
            val request = Request.Builder().url(BuildConfig.API_BASE_URL + "admin/surveys?page=1&limit=10").header("token", token).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) { showResult(R.string.dev_connection_failed, requestGeneration) }
                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val valid = try { JSONObject(it.body?.string() ?: "").optJSONArray("items") != null } catch (_: Exception) { false }
                        val message = if (it.code == 200 && valid) R.string.dev_connected else if (it.code == 401 || it.code == 403) R.string.dev_not_authorized else R.string.dev_connection_failed
                        showResult(message, requestGeneration)
                    }
                }
            })
        }
    }
    private fun showResult(message: Int, requestGeneration: Int) = runOnUiThread {
        if (!isDestroyed && requestGeneration == generation) { status.setText(message); render(false) }
    }
    override fun onDestroy() { generation++; client.dispatcher.cancelAll(); super.onDestroy() }
}
