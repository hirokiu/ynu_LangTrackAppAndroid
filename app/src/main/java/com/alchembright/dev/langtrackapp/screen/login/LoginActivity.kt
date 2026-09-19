package com.alchembright.dev.langtrackapp.screen.login

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se

* Viktor Czyżewski
* RSE Team
* University of York
* */

import com.alchembright.dev.langtrackapp.util.applySystemBarInsets
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
//import kotlinx.android.synthetic.main.login_activity.*
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.User
import com.alchembright.dev.langtrackapp.databinding.LoginActivityBinding
import com.alchembright.dev.langtrackapp.popup.PopupAlert
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    lateinit var mBind: LoginActivityBinding
    private lateinit var viewModel : LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBind = DataBindingUtil.setContentView(this,R.layout.login_activity)
        applySystemBarInsets()
        com.alchembright.dev.langtrackapp.util.ProjectEnvironment.bindSelector(mBind.projectSelector)

        viewModel = ViewModelProvider(this,
            LoginViewModelFactory(this)
        ).get(LoginViewModel::class.java)

        mBind.logInButton.setOnClickListener {
            checkTextAndLogIn()
        }
        mBind.loginHelpButton.setOnClickListener {
            val alertFm = supportFragmentManager.beginTransaction()
            val width = (mBind.loginLayout.measuredWidth * 0.75).toInt()
            val alertPopup = PopupAlert.show(
                width = width,
                title = getString(R.string.info),
                textViewText = getString(R.string.langTrackAppInfo1),
                placecenter = true
            )
            alertPopup.show(alertFm, "alertPopup")
        }
        mBind.loginProgressbar.visibility = View.GONE
        mBind.logInEmailEditText.requestFocus()
    }

    private fun checkTextAndLogIn(){
        val username = mBind.logInEmailEditText.text.toString().trim()
        val password = mBind.logInPasswordEditText.text.toString().trim()

        if (username.isEmpty()){
            mBind.logInEmailEditText.error = getString(R.string.enterUserName)
            mBind.logInEmailEditText.requestFocus()
            return
        }

        if (password.isEmpty()){
            mBind.logInPasswordEditText.error = getString(R.string.enterPassword)
            mBind.logInPasswordEditText.requestFocus()
            return
        }
        mBind.loginProgressbar.visibility = View.VISIBLE

        val userEmail = "${username}@humlablu.com"
        if (!isValidEmail(userEmail)){
            return
        }
        logIn(userEmail,password)
    }

    private fun isValidEmail(email: String): Boolean{
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    fun subscribeToTopic(topic:String){
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    println("subscribeToTopic ERROR: ${task.exception?.localizedMessage}")
                }else{
                    println("subscribeToTopic, subscribed to topic: $topic")
                }
            }
    }


    fun logIn(email: String, password: String){
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                mBind.loginProgressbar.visibility = View.GONE
                if (it.isSuccessful){
                    val userEmail = mAuth.currentUser!!.email
                    val userName = userEmail?.substringBefore('@')
                    if (!userName.isNullOrEmpty()) {
                        viewModel.setCurrentUser(
                            User(
                                id = userName,
                                name = userName,
                                mail = userEmail
                            )
                        )
                        viewModel.putDeviceToken()
                        subscribeToTopic(userName)
                    }
                    onBackPressedDispatcher.onBackPressed()
                }else{
                    Toast.makeText(this@LoginActivity,
                        getString(R.string.authenticationFailed),
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,LoginActivity::class.java).apply {

            })
        }
    }
}
