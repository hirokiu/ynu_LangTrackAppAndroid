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

class LoginActivity : AppCompatActivity() {

    private val accountAuth = com.alchembright.dev.langtrackapp.data.AccountAuthentication()
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
        val password = mBind.logInPasswordEditText.text.toString()

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

        mBind.logInButton.isEnabled = false
        accountAuth.login(username, password) { successful ->
            if (!successful) loginFailed()
            else accountAuth.resolve { id, token ->
                if (id == null || token == null) {
                    FirebaseAuth.getInstance().signOut()
                    viewModel.mRepository.setCurrentUser(User())
                    viewModel.mRepository.idToken = ""
                    viewModel.mRepository.emptyAssignmentsList()
                    loginFailed()
                }
                else {
                    val user = FirebaseAuth.getInstance().currentUser
                    viewModel.setCurrentUser(User(id, username, user?.email ?: ""))
                    viewModel.mRepository.idToken = token
                    if (com.alchembright.dev.langtrackapp.util.ProjectEnvironment.isDev) {
                        startActivity(Intent(this, com.alchembright.dev.langtrackapp.screen.main.MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
                    } else {
                        viewModel.putDeviceToken()
                        subscribeToTopic(id)
                    }
                    finish()
                }
            }
        }
    }

    private fun loginFailed() {
        mBind.loginProgressbar.visibility = View.GONE
        mBind.logInButton.isEnabled = true
        Toast.makeText(this, getString(R.string.authenticationFailed), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() { accountAuth.close(); super.onDestroy() }

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


    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context,LoginActivity::class.java).apply {

            })
        }
    }
}
