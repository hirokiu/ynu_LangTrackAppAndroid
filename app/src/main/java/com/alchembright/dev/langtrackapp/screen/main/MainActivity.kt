@file:Suppress("OverrideDeprecatedMigration")

package com.alchembright.dev.langtrackapp.screen.main
/*

Stephan Björck
Humanistlaboratoriet
Lunds Universitet
stephan.bjorck@humlab.lu.se

Viktor Czyżewski
RSE Team
University of York


* tempinloggning

* email: deltagare1a2b3c@humlablu.com
* användarnamn: deltagare1a2b3c
* lösenord: 123456

* email: test1@humlablu.com
* användarnamn: test1
* lösenord: 123456
* */

import com.alchembright.dev.langtrackapp.util.applySystemBarInsets
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
//import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.left_drawer_menu.*
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.data.model.Assignment
import com.alchembright.dev.langtrackapp.data.model.User
import com.alchembright.dev.langtrackapp.databinding.ActivityMainBinding
import com.alchembright.dev.langtrackapp.interfaces.OnBoolPopupReturnListener
import com.alchembright.dev.langtrackapp.interfaces.OnSurveyRowClickedListener
import com.alchembright.dev.langtrackapp.popup.ExpiredSurveyPopup
import com.alchembright.dev.langtrackapp.popup.OneChoicePopup
import com.alchembright.dev.langtrackapp.screen.about.AboutActivity
import com.alchembright.dev.langtrackapp.screen.contact.ContactActivity
import com.alchembright.dev.langtrackapp.screen.instructions.InstructionsActivity
import com.alchembright.dev.langtrackapp.screen.login.LoginActivity
import com.alchembright.dev.langtrackapp.screen.overview.OverviewActivity
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity
import com.alchembright.dev.langtrackapp.util.MyFirebaseInstanceIDService
import com.alchembright.dev.langtrackapp.util.getVersionNumber
import com.alchembright.dev.langtrackapp.util.showApiFailInfo


@Suppress("OverrideDeprecatedMigration")
class MainActivity : AppCompatActivity() {

    private lateinit var mBind : ActivityMainBinding
    private lateinit var viewModel : MainViewModel
    lateinit var mAuth: FirebaseAuth
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: SurveyAdapter
    lateinit var drawerToggle: ActionBarDrawerToggle
    private var inTestMode = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!com.alchembright.dev.langtrackapp.util.ProjectEnvironment.isDev && android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 112)
        }
        mBind = DataBindingUtil.setContentView(this, R.layout.activity_main)
        applySystemBarInsets()
        mBind.projectName.text = com.alchembright.dev.langtrackapp.util.ProjectEnvironment.name
        mBind.projectMessage.setText(listOf(R.string.project_description, R.string.project_message_2).random())
        com.alchembright.dev.langtrackapp.util.ProjectEnvironment.bindSelector(mBind.leftDrawerMenu.projectSelector)

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mBind.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mBind.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
        mBind.lifecycleOwner = this
        mBind.executePendingBindings()
        mAuth = FirebaseAuth.getInstance()

        viewModel = ViewModelProvider(this,
            MainViewModelFactory(this)
        ).get(MainViewModel::class.java)
        mBind.viewModel = viewModel

        // only gets this if app is in foreground...
        val theMessageText = intent.getStringExtra(MyFirebaseInstanceIDService.MESSAGE_TEXT)
        if (!theMessageText.isNullOrBlank()) {
            println("messaging MainActivity onCreate theMessageText: $theMessageText")
        }

        recycler = mBind.surveyRecycler
        linearLayoutManager = LinearLayoutManager(this)
        recycler.layoutManager = linearLayoutManager
        adapter = SurveyAdapter()
        recycler.adapter = adapter

        //swipeRefresh
        mBind.surveyRecyclerRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.lta_blue))
        mBind.surveyRecyclerRefreshLayout.setColorSchemeColors(Color.WHITE)

        mBind.surveyRecyclerRefreshLayout.setOnRefreshListener {
            if (mAuth.currentUser != null){
                viewModel.getAssignments()
            }
            mBind.surveyRecyclerRefreshLayout.isRefreshing = false
        }

        mBind.surveyRecycler.addItemDecoration(MyItemDecorator(4,28))
        adapter.setOnRowClickedListener(object: OnSurveyRowClickedListener {
            override fun rowClicked(item: Assignment) {

                viewModel.setSelectedAssignment(item)
                if (inTestMode) {
                    // in testMode, always show survey
                    SurveyContainerActivity.start(this@MainActivity, item, inTestMode)
                    viewModel.surveyOpened()
                } else {
                    if (item.isActive()) {
                        // show survey - if api is responding
                        viewModel.apiIsAlive { alive, _ ->
                            //if (alive){
                            if (true){
                                SurveyContainerActivity.start(this@MainActivity, item, inTestMode)
                                viewModel.surveyOpened()
                            }else{
                                showApiFailInfo(this@MainActivity)
                            }
                        }

                    } else {
                        if (item.dataset != null) {
                            // show overview
                            OverviewActivity.start(this@MainActivity, item)
                        } else {
                            // show popup
                            showPopupSurveyInfo(item = item)
                        }
                    }
                }
            }

        })

        adapter.setAssignments(viewModel.assignmentList)

        viewModel.assignmentListLiveData.observe(this) {
            adapter.setAssignments(it)
            if (it.isNullOrEmpty()){
                mBind.surveyRecyclerRefreshLayout.visibility = View.GONE
                mBind.mainEmptyListInfoTextView.visibility = View.VISIBLE
            }else{
                mBind.surveyRecyclerRefreshLayout.visibility = View.VISIBLE
                mBind.mainEmptyListInfoTextView.visibility = View.GONE
            }
        }

        setSupportActionBar(mBind.toolbar)
        drawerToggle = ActionBarDrawerToggle(
            this,
            mBind.drawerLayout,
            mBind.toolbar,
            R.string.Open,
            R.string.Close
        )
        supportActionBar?.apply {
            title = getString(R.string.langTrackApp)
            setDisplayHomeAsUpEnabled(true)
        }

        mBind.leftDrawerMenu.menuLogOutTextView.setOnClickListener {
            print("logout");
            showLogOutPopup()
        }

        mBind.leftDrawerMenu.menuInstructionsContactButton.setOnClickListener {
            print("logout");
            InstructionsActivity.start(this)
        }

        mBind.leftDrawerMenu.menuAboutButton.setOnClickListener {
            print("logout");
            AboutActivity.start(this)
        }
        mBind.leftDrawerMenu.menuContactButton.setOnClickListener {
            print("logout");
            ContactActivity.start(this)
            //drawerLayout.closeDrawer(GravityCompat.START)
        }

        mBind.leftDrawerMenu.menuTestSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            print("logout");
            inTestMode = isChecked
        }

        mBind.leftDrawerMenu.menuServerSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            print("logout");
            viewModel.setStagingUrl(isChecked)
            viewModel.postDeviceToken()
            viewModel.clearAssignmentsList()
            viewModel.getAssignments()
        }

    }

    override fun onStart() {
        super.onStart()
        print("start");

        val verNum = getVersionNumber(this)
        // save userId to repository
        if (mAuth.currentUser == null){
            com.alchembright.dev.langtrackapp.util.ProjectEnvironment.openLogin(this)
        } else {
            val user = mAuth.currentUser ?: return
            val dev = com.alchembright.dev.langtrackapp.util.ProjectEnvironment.isDev
            if (dev && viewModel.getCurrentUser().id.isEmpty()) {
                com.alchembright.dev.langtrackapp.util.ProjectEnvironment.openLogin(this)
                return
            }
            val userName = user.email?.substringBefore('@') ?: ""
            if (!dev) viewModel.setCurrentUser(User(userName, userName, user.email ?: ""))
            mBind.leftDrawerMenu.menuUserNameTextView.text = user.displayName ?: userName
            mBind.leftDrawerMenu.menuVersionTextView.text = getString(R.string.version_label, verNum)
            user.getIdToken(false).addOnSuccessListener(this) { result ->
                if (mAuth.currentUser?.uid == user.uid && !result.token.isNullOrBlank()) {
                    viewModel.setIdToken(result.token!!)
                    viewModel.getAssignments()
                    if (!dev) viewModel.postDeviceToken()
                }
            }
            // A build has one isolated project; never offer the legacy staging toggle.
            mBind.leftDrawerMenu.testView.visibility = View.GONE
        }
    }

    private fun setTestModeIfTeam(userName: String){
        /*
        Set testview if user is admin (part of team)
         */
        viewModel.getTeamUserNames { result ->
            mBind.leftDrawerMenu.testView.visibility = if (result.containsKey(userName)) View.VISIBLE else View.GONE
        }
    }

    fun unsubscribeToTopic(){
        val topic = viewModel.getCurrentUser().userName
        if (topic != "") {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        println("unsubscribeToTopic ERROR: ${task.exception?.localizedMessage}")
                    } else {
                        println("unsubscribeToTopic, subscribed to topic: $topic")
                    }
                }
        }
    }

    private fun showLogOutPopup(){
        val alertFm = supportFragmentManager.beginTransaction()
        val width = (mBind.mainLayout.measuredWidth * 0.75).toInt()
        val oneChoicePopup = OneChoicePopup.show(
            width = width,
            title = getString(R.string.log_out),
            infoText = getString(R.string.doYouWantToLogOut, viewModel.getCurrentUser().userName),
            okButtonText = getString(R.string.log_out),
            cancelButtonText = getString(R.string.cancel),
            placecenter = true,
            cancelable = true
        )
        oneChoicePopup.setCompleteListener(object : OnBoolPopupReturnListener {
            override fun popupReturn(value: Boolean) {
                if (value){
                    viewModel.clearAssignmentsList()
                    mAuth.signOut()
                    if (!com.alchembright.dev.langtrackapp.util.ProjectEnvironment.isDev) unsubscribeToTopic()
                    com.alchembright.dev.langtrackapp.util.ProjectEnvironment.openLogin(this@MainActivity)
                }
            }
        })
        oneChoicePopup.show(alertFm, "oneChoicePopup")
    }

    fun showPopupSurveyInfo(item: Assignment){
        val alertFm = supportFragmentManager.beginTransaction()
        val width = (mBind.mainLayout.measuredWidth * 0.85).toInt()

        val alertPopup = ExpiredSurveyPopup.show(
            width = width,
            published = item.publishAt,
            expired = item.expireAt,
            numberOfQuestions = item.survey.questions?.size ?: 0,
            textViewText = item.survey.title,
            placecenter = true
        )
        alertPopup.setCompleteListener(object : OnBoolPopupReturnListener{
            override fun popupReturn(value: Boolean) {
                onBackPressedDispatcher.onBackPressed()
            }
        })
        alertPopup.show(alertFm, "surveyInfoPopup")
    }


    companion object {
        fun start(context: Context){
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}
class MyItemDecorator(private val horizontal: Int, private val vertical: Int): RecyclerView.ItemDecoration(){

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.right = horizontal
        outRect.left = horizontal
        if (parent.getChildLayoutPosition(view) == 0){
            outRect.top = vertical
        }
        outRect.bottom = vertical
    }

}
