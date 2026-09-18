package com.alchembright.dev.langtrackapp.data

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se

* Viktor Czyżewski
* RSE Team
* University of York
* */

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.github.kittinunf.fuel.Fuel
import com.google.firebase.database.*
import com.google.gson.Gson
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.alchembright.dev.langtrackapp.data.model.*
import com.alchembright.dev.langtrackapp.screen.surveyContainer.SurveyContainerActivity
import com.alchembright.dev.langtrackapp.util.*
import java.util.*
import kotlin.collections.ArrayList

class Repository(val context: Context) {



    private var currentUser = User()
    var currentUserLiveData = MutableLiveData<User>()
    var selectedAssignment: Assignment? = null
    var idToken = ""
    private var assignmentList = mutableListOf<Assignment>()
    var assignmentListLiveData = MutableLiveData<MutableList<Assignment>>()
    //private val ltaUrl = "http://ht-lang-track.ht.lu.se/api/"
    //private val ltaUrl = "http://ht-lang-track.ht.lu.se:443/"
    var client = OkHttpClient()
    private var useStagingServer = false
    val database = FirebaseDatabase.getInstance()
    val dbRef = database.reference


    fun setCurrentUser(user: User){
        currentUser = user
        currentUserLiveData.postValue(currentUser)
    }
    fun getCurrentUser(): User{
        return currentUser
    }

    fun setStagingUrl(useStaging: Boolean){
        useStagingServer = useStaging
    }

    fun isInStaging() :Boolean{
        return useStagingServer
    }

    private fun getUrl(listener: (result: String?) -> Unit) {
        // A proto build never switches the shared Firebase URL used by existing apps.
        val configured = com.alchembright.dev.langtrackapp.BuildConfig.API_BASE_URL
        if (configured.isNotEmpty()) {
            listener(configured)
            return
        }
        /*
         Getting the correct url frlom firebase realtime - prodUrl or stagingUrl
         */

        var myRef :DatabaseReference?
        if (useStagingServer){
            myRef = database.getReference("stagingUrl")
        }else{
            myRef = database.getReference("url")
        }
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //println("getUrl snapshot: ${snapshot.value}")
                try {
                    val theFetchedUrl = snapshot.value as? String
                    listener(theFetchedUrl)
                } catch (e: Exception) {
                    println("getUrl Exception: $e")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("getUrl ERROR: ${error.message}")
            }

        })
    }

    fun putDeviceToken(){

        apiIsAlive { alive, theUrl ->
            //if (alive) {
            if (true) {

                if (currentUser.id != "") {
                    val verNr = getVersionNumber(context)
                    println("putDeviceToken version number: $verNr")

                    val localTimeZoneIdentifier = TimeZone.getDefault().id
                    println("putDeviceToken localTimeZoneIdentifier: $localTimeZoneIdentifier")
                    val deviceTokenUrl = "${theUrl}users/${currentUser.id}"

                    if (localTimeZoneIdentifier != "") {
                        MyFirebaseInstanceIDService.getDeviceTokengetDeviceToken(object :
                                (String?) -> Unit {
                            override fun invoke(deviceToken: String?) {
                                val vNumber = Build.VERSION.RELEASE ?: ""
                                if (deviceToken != null) {//"": "Android"
                                    val jsonAnswer =
                                        "{\"timezone\":\"${localTimeZoneIdentifier}\", \"deviceToken\":\"${deviceToken}\", \"versionNumber\":\"${verNr}\", \"os\":\"Android ${vNumber}\"}"

                                    IO.execute {
                                        val httpUrl = deviceTokenUrl.toHttpUrl()
                                        val httpUrlBuilder = httpUrl.newBuilder()
                                        val requestBuilder =
                                            Request.Builder().url(httpUrlBuilder.build()).header(
                                                "Authorization",
                                                "token " + idToken
                                            )
                                        requestBuilder.addHeader("token", idToken)
                                        requestBuilder.addHeader("bearer", idToken)
                                        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

                                        requestBuilder.put(
                                            jsonAnswer.toRequestBody(mediaTypeJson)
                                        )
                                        val call = client.newCall(requestBuilder.build())
                                        call.execute().use {
                                            if (it.isSuccessful) {
                                                println("putDeviceToken SUCCESS: ${it.body} url: ${theUrl}")
                                            } else {
                                                println("putDeviceToken ERROR: ${it.body} url: ${theUrl}, ${it.message}")
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }else{
                println("api is dead")
            }
        }
    }

    //MARK: -   Team and dynamic texts



    fun getContactInfo(callback: (result: List<ContactInfo>) -> Unit) {

        var test = mutableListOf<ContactInfo>()

        dbRef.child("contactInfo").keepSynced(true)
        dbRef.child("contactInfo").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //println("getUrl snapshot: ${snapshot.value}")
                try {
                    val theContactInfoArrayList = snapshot.value as? ArrayList<*>
                    if (theContactInfoArrayList != null) {
                        for (contact in theContactInfoArrayList){
                            if (contact != null) {
                                val theContact = contact as? Map<String, Any>
                                if (theContact != null) {
                                    val tempContact = ContactInfo(
                                        email = theContact["email"] as? String ?: "",
                                        text = theContact["text"] as? Map<String, String>
                                    )
                                    test.add(tempContact)
                                }
                            }
                        }
                        callback(test)
                    }
                } catch (e: Exception) {
                    println("getContactInfo Exception: ${e.localizedMessage}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("getContactInfo ERROR: ${error.message}")
            }
        })
    }

    fun getTeamUsernames(callback: (result: Map<String,String>) -> Unit){

        var tempMap = mutableMapOf<String,String>()

        dbRef.child("admins").keepSynced(true)
        dbRef.child("admins").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //println("getUrl snapshot: ${snapshot.value}")
                try {
                    val teamArrayList = snapshot.value as? Map<*, *>
                    if (teamArrayList != null) {
                        for (member in teamArrayList){
                            tempMap[member.key as String] = member.value as String
                        }
                        callback(tempMap)
                    }
                } catch (e: Exception) {
                    println("getTeamUsernames Exception: ${e.localizedMessage}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("getTeamUsernames ERROR: ${error.message}")
            }
        })
    }

    fun getTeamsText(callback: (result: List<TeamMember>) -> Unit){
        var tempList = mutableListOf<TeamMember>()
        dbRef.child("team").keepSynced(true)
        dbRef.child("team").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val teamArrayList = snapshot.value as? List<*>
                    if (teamArrayList != null) {
                        for (person in teamArrayList){
                            var listWithNames = mutableMapOf<String, String>()
                            var listWithDescr = mutableMapOf<String, String>()
                            val personDict = person as? Map<*, *>
                            if (personDict != null) {
                                for (language in personDict) {
                                    val languageKey = language.key as? String
                                    if (languageKey != null){
                                        val thelanguage = language.value as? Map<*, *>
                                        println("thelanguage: $thelanguage")
                                        if (thelanguage != null) {
                                            var name = ""
                                            var description = ""
                                            for (item in thelanguage) {
                                                if (item.key == "name") {
                                                    name = item.value as String
                                                }
                                                if (item.key == "description") {
                                                    description = item.value as String
                                                }
                                            }
                                            if (name.isNotBlank()) {
                                                listWithNames[languageKey] = name
                                                listWithDescr[languageKey] = description
                                            }
                                        }
                                    }
                                }
                                if (listWithNames.isNotEmpty()){
                                    tempList.add(TeamMember(
                                        name = listWithNames,
                                        description = listWithDescr
                                    ))
                                }
                            }
                        }
                        callback(tempList)
                    }
                } catch (e: Exception) {
                    println("getTeamUsernames Exception: ${e.localizedMessage}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("getTeamsText ERROR: ${error.message}")
            }
        })
    }

    fun postAnswer(answerDict: Map<Int, Answer>){
        if (currentUser.id.isNotEmpty()){
            getUrl { theUrl ->
                val answers = mutableListOf<AnswerBody>()
                for (answer in answerDict.values) {
                    var stringValue: String? = null
                    var intValue: Int? = null
                    var multiValue: MutableList<Int>? = null
                    when (answer.type) {
                        SurveyContainerActivity.LIKERT_SCALES ->
                            intValue = answer.likertAnswer
                        SurveyContainerActivity.SINGLE_MULTIPLE_ANSWERS ->
                            intValue = answer.singleMultipleAnswer
                        SurveyContainerActivity.FILL_IN_THE_BLANK ->
                            intValue = answer.fillBlankAnswer
                        SurveyContainerActivity.MULTIPLE_CHOICE ->
                            multiValue = answer.multipleChoiceAnswer
                        SurveyContainerActivity.OPEN_ENDED_TEXT_RESPONSES ->
                            stringValue = answer.openEndedAnswer
                        SurveyContainerActivity.TIME_DURATION ->
                            intValue = answer.timeDurationAnswer
                        SurveyContainerActivity.SLIDER_SCALE ->
                            intValue = answer.sliderScaleAnswer
                    }
                    val body = AnswerBody(
                        index = answer.index,
                        type = answer.type,
                        intValue = intValue,
                        multiValue = multiValue,
                        stringValue = stringValue
                    )
                    if (body.index != -99) {
                        answers.add(body)
                    }

                }
                val answerUrl =
                    "${theUrl}users/${currentUser.id}/assignments/${selectedAssignment!!.id}/datasets"
                val gson = Gson()
                val jsonAnswer: String = gson.toJson(answers)
                val jsonAnswer2 = "{\"answers\":${jsonAnswer}}"

                IO.execute {
                    val httpUrl = answerUrl.toHttpUrl()
                    val httpUrlBuilder = httpUrl.newBuilder()
                    val requestBuilder = Request.Builder().url(httpUrlBuilder.build()).header(
                        "Authorization",
                        "token " + idToken
                    )
                    requestBuilder.addHeader("token", idToken)
                    requestBuilder.addHeader("bearer", idToken)
                    val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
                    requestBuilder.post(
                        jsonAnswer2.toRequestBody(mediaTypeJson)
                    )
                    val call = client.newCall(requestBuilder.build())
                    call.execute().use {
                        if (it.isSuccessful) {
                            println("postAnswer SUCCESS: ${it.body} url: ${theUrl}")
                            //reload to get answer to list
                            getAssignments()
                        } else {
                            println("postAnswer ERROR: ${it.body} url: ${theUrl}")
                        }
                    }
                }
            }
        }
    }

    fun surveyOpened(){
        //test2()
        println("token: $idToken")
        getUrl { theUrl ->
            if (theUrl != null && selectedAssignment != null) {
                val openedUrl = "${theUrl}assignments/${selectedAssignment!!.id}/open"
                println("token: $idToken, openedUrl: $openedUrl")

                IO.execute {
                    val (ignoredRequest, ignoredResponse, result) =
                        Fuel.post(openedUrl)
                            .header("Content-Type", "text/html")
                            .header("token" to idToken)
                            .header("bearer" to idToken)
                            .responseString()

                    result.fold(
                        { print("surveyOpened success: $result") },
                        { print("surveyOpened failure: $result") })
                }
            }

        }
    }

    fun apiIsAlive(listener: (result: Boolean, theUrl: String?) -> Unit) {
        getUrl { theUrl ->
            if (theUrl != null) {
                val apiPingUrl = "${theUrl}ping"
                Fuel.get(apiPingUrl).response { _, response, _ ->
                    listener(response.statusCode == 200, theUrl)
                }
            }else{
                listener(false, null)
            }
        }
    }

    fun emptyAssignmentsList(){
        assignmentList.clear()
        assignmentListLiveData.value = assignmentList
    }

    fun getAssignments(){
        getUrl { theUrl ->
            val assigmnentUrl = "${theUrl}users/${currentUser.userName}/assignments"

            val request = Request.Builder()
                .url(assigmnentUrl)
                .addHeader("token", idToken)
                .build()

            client.newCall(request).execute().use {
                if (it.isSuccessful) {
                    val body = it.body!!.string()
                    val templist = sortList(getAssignmentsFromJson(body)).toMutableList()
                    if (!templist.isNullOrEmpty()) {
                        assignmentList = templist
                        assignmentListLiveData.value = assignmentList
                    }
                } else {
                    println("Repository getAssignmens ERROR: ${it.message}")
                    showApiFailInfo(context)
                }
            }
        }
    }

    private fun sortList(theListWithSurveys: List<Assignment>): List<Assignment>{

        //if no dataset and not expired
        val activeList = theListWithSurveys.filter {
            it.isActive()
        }.sortedByDescending { it.publishAt }

        //if dataset or expired
        val inactiveList = theListWithSurveys.filter {
            !it.isActive()
        }.sortedByDescending { it.publishAt }

        val returnlist = activeList.toMutableList()
        returnlist.addAll(inactiveList)
        return returnlist
    }


    private fun getAssignmentsFromJson(json: String): List<Assignment>{
        val theListWithSurveys = mutableListOf<Assignment>()
        if (json.contains('[') && json.contains(']')) {
            val jsonObj = JSONArray(json.substring(json.indexOf("["), json.lastIndexOf("]") + 1))
            for (i in 0 until jsonObj.length()) {
                var newsurvey: Survey? = null
                var newupdatedAt = ""
                var newcreatedAt = ""
                var newuserId = ""
                var newdataset: Dataset? = null
                var newpublishAt = ""
                var newexpireAt = ""
                var newid = ""
                val assignment = jsonObj.getJSONObject(i)
                try {
                    val tempSurveyObj = assignment.get("survey") as? JSONObject
                    if (tempSurveyObj != null) {
                        newsurvey = getSurveyFromJsonObj(tempSurveyObj)
                    }
                } catch (e: Exception) {
                }
                try {
                    newupdatedAt = assignment.get("updatedAt") as? String ?: ""
                } catch (e: Exception) {
                }
                try {
                    newcreatedAt = assignment.get("createdAt") as? String ?: ""
                } catch (e: Exception) {
                }
                try {
                    newuserId = assignment.get("userId") as? String ?: ""
                } catch (e: Exception) {
                }
                try {
                    val datasetObj = assignment.get("dataset") as? JSONObject
                    if (datasetObj != null) {
                        newdataset = getDatasetFromJsonObj(datasetObj)
                    }
                } catch (e: Exception) {
                }
                try {
                    newpublishAt = assignment.get("publishAt") as? String ?: ""
                } catch (e: Exception) {
                }
                try {
                    newexpireAt = assignment.get("expireAt") as? String ?: ""
                } catch (e: Exception) {
                }
                try {
                    newid = assignment.get("_id") as? String ?: ""
                } catch (e: Exception) {
                }

                if (newsurvey != null) {

                    //if expireAt is empty -> set expireAt to one hour from publishAt
                    if (newexpireAt.isEmpty()){
                        val publishDate = newpublishAt.toDate()
                        if (publishDate != null){
                            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                            formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
                            newexpireAt = formatter.format(java.util.Date(publishDate.time + 60 * 60 * 1000L))
                        }
                    }
                    theListWithSurveys.add(
                        Assignment(
                            survey = newsurvey,
                            updatedAt = newupdatedAt,
                            createdAt = newcreatedAt,
                            userId = newuserId,
                            dataset = newdataset,
                            publishAt = newpublishAt,
                            expireAt = newexpireAt,
                            id = newid
                        )
                    )
                }
            }
        }
        return theListWithSurveys
    }



    private fun getDatasetFromJsonObj(jsonObj: JSONObject): Dataset?{
        var id = ""
        var createdAt = ""
        var updatedAt = ""
        val answers = mutableListOf<Answer>()

        try {
            id = jsonObj.get("_id") as? String ?: ""
        }catch (e: Exception){ }
        try {
            createdAt = jsonObj.get("createdAt") as? String ?: ""
        }catch (e: Exception){ }
        try {
            updatedAt = jsonObj.get("updatedAt") as? String ?: ""
        }catch (e: Exception){ }
        try {
            val answersArray = jsonObj.get("answers") as? JSONArray
            if (answersArray != null){

                for (a in 0 until answersArray.length()){
                    try{
                        val answerObj = answersArray.getJSONObject(a)
                        var index = -99
                        var type = ""
                        var intValue: Int? = -99
                        var stringValue: String? = ""
                        val multiValue = mutableListOf<Int>()
                        try {
                            index = answerObj.get("index") as? Int ?: -99
                        }catch (e: Exception){ }
                        try {
                            type = answerObj.get("type") as? String ?: ""
                        }catch (e: Exception){ }
                        try {
                            intValue = answerObj.get("intValue") as? Int
                        }catch (e: Exception){ }
                        try {
                            stringValue = answerObj.get("stringValue") as? String
                        }catch (e: Exception){ }
                        try {
                            val multiArray = answerObj.get("multiValue") as? JSONArray
                            if (multiArray != null){
                                for (m in 0 until multiArray.length()){
                                    multiValue.add(multiArray.getInt(m))
                                }
                            }
                        }catch (e: Exception){ }
                        when(type){
                            SurveyContainerActivity.SINGLE_MULTIPLE_ANSWERS -> {
                                answers.add(
                                    Answer(
                                        type = type,
                                        index = index,
                                        likertAnswer = null,
                                        fillBlankAnswer = null,
                                        multipleChoiceAnswer = null,
                                        singleMultipleAnswer = intValue,
                                        openEndedAnswer = null,
                                        sliderScaleAnswer = null
                                    )
                                )
                            }
                            SurveyContainerActivity.MULTIPLE_CHOICE -> {
                                answers.add(
                                    Answer(
                                        type = type,
                                        index = index,
                                        likertAnswer = null,
                                        fillBlankAnswer = null,
                                        multipleChoiceAnswer = multiValue,
                                        singleMultipleAnswer = null,
                                        openEndedAnswer = null,
                                        sliderScaleAnswer = null
                                    )
                                )
                            }
                            SurveyContainerActivity.LIKERT_SCALES -> {
                                answers.add(
                                    Answer(
                                        type = type,
                                        index = index,
                                        likertAnswer = intValue,
                                        fillBlankAnswer = null,
                                        multipleChoiceAnswer = null,
                                        singleMultipleAnswer = null,
                                        openEndedAnswer = null,
                                        sliderScaleAnswer = null
                                    )
                                )
                            }
                            SurveyContainerActivity.FILL_IN_THE_BLANK -> {
                                answers.add(
                                    Answer(
                                        type = type,
                                        index = index,
                                        likertAnswer = null,
                                        fillBlankAnswer = intValue,
                                        multipleChoiceAnswer = null,
                                        singleMultipleAnswer = null,
                                        openEndedAnswer = null,
                                        sliderScaleAnswer = null
                                    )
                                )
                            }
                            SurveyContainerActivity.OPEN_ENDED_TEXT_RESPONSES -> {
                                answers.add(
                                    Answer(
                                        type = type,
                                        index = index,
                                        likertAnswer = null,
                                        fillBlankAnswer = null,
                                        multipleChoiceAnswer = null,
                                        singleMultipleAnswer = null,
                                        openEndedAnswer = stringValue,
                                        sliderScaleAnswer = null
                                    )
                                )
                            }
                            SurveyContainerActivity.TIME_DURATION -> {
                                answers.add(
                                    Answer(
                                        type = type,
                                        index = index,
                                        likertAnswer = null,
                                        fillBlankAnswer = null,
                                        multipleChoiceAnswer = null,
                                        singleMultipleAnswer = null,
                                        openEndedAnswer = null,
                                        timeDurationAnswer = intValue,
                                        sliderScaleAnswer = null
                                    )
                                )
                            }
                            SurveyContainerActivity.SLIDER_SCALE -> {
                                answers.add(
                                    Answer(
                                        type = type,
                                        index = index,
                                        likertAnswer = null,
                                        fillBlankAnswer = null,
                                        multipleChoiceAnswer = null,
                                        singleMultipleAnswer = null,
                                        openEndedAnswer = null,
                                        timeDurationAnswer = null,
                                        sliderScaleAnswer = intValue
                                    )
                                )
                            }
                        }
                    }catch (e: Exception){ }
                }
            }
        }catch (e: Exception){ }
        return if (id != "" && answers.isNotEmpty()) {
            Dataset(
                id = id,
                createdAt = createdAt,
                updatedAt = updatedAt,
                answers = answers
            )
        }else{
            null
        }
    }
    private fun getSurveyFromJsonObj(jsonObj: JSONObject): Survey{
        val tempSurvey = Survey()
        try {
            tempSurvey.id = jsonObj.get("id") as? String ?: ""
        }catch (e: Exception){ }
        try {
            tempSurvey.name = jsonObj.get("name") as? String ?: ""
        }catch (e: Exception){ }
        try {
            tempSurvey.title = jsonObj.get("title") as? String ?: ""
        }catch (e: Exception){ }
        try {
            val tempquestions = jsonObj.get("questions") as? JSONArray
            if (tempquestions != null) {
                tempSurvey.questions = getQuestionsFromJsonArray(tempquestions)
            }
        }catch (e: Exception){ }
        try {
            tempSurvey.createdAt = jsonObj.get("createdAt") as? String ?: ""
        }catch (e: Exception){ }
        try {
            tempSurvey.updatedAt = jsonObj.get("updatedAt") as? String ?: ""
        }catch (e: Exception){ }
        if (tempSurvey.questions != null) {
            for (question in tempSurvey.questions!!) {
                when {
                    question.index == 0 -> {
                        question.previous = 0
                        question.next = question.index + 1
                    }
                    question.index < tempSurvey.questions!!.size - 1 -> {
                        question.next = question.index + 1
                        question.previous = question.index - 1
                    }
                    else -> {
                        question.next = 0
                        question.previous = question.index - 1
                    }
                }
            }
        }
        return tempSurvey
    }

    private fun getQuestionsFromJsonArray(questionsObj: JSONArray): List<Question>{
        val thequestions = mutableListOf<Question>()
        for (q in 0 until questionsObj.length()){
            val values = mutableListOf<String>()
            val tempQuestion = Question()
            val question = questionsObj.getJSONObject(q)

            try {
                tempQuestion.type = question.get("type") as? String ?: ""
            }catch (e: Exception){ }

            try {
                tempQuestion.index = question.get("index") as? Int ?: 0
            }catch (e: Exception){ }

            try {
                tempQuestion.text = question.get("text") as? String ?: ""
            }catch (e: Exception){ }

            try {
                tempQuestion.title = question.get("title") as? String ?: ""
            }catch (e: Exception){ }

            try {
                tempQuestion.description = question.get("description") as? String ?: ""
            }catch (e: Exception){ }

            try {
                tempQuestion.likertMax = question.get("maxAnnotation") as? String ?: ""
            }catch (e: Exception){ }

            try {
                tempQuestion.likertMin = question.get("minAnnotation") as? String ?: ""
            }catch (e: Exception){ }

            try {
                tempQuestion.id = question.get("id") as? String ?: ""
            }catch (e: Exception){ }

            try {
                val tempskip = question.getJSONObject("skip")
                val theSkip = SkipLogic()
                for (s in tempskip.keys()){
                    theSkip.ifChosen = tempskip.getInt("ifChosen")
                    theSkip.goto = tempskip.getInt("goto")
                }
                tempQuestion.skip = theSkip
            }catch (e: Exception){ }
            try {
                val includeIfObject = question.getJSONObject("includeIf")
                val tempIncludeIf = IncludeIf()
                for (s in includeIfObject.keys()){
                    tempIncludeIf.ifIndex = includeIfObject.getInt("ifIndex")
                    tempIncludeIf.ifValue = includeIfObject.getInt("ifValue")
                }
                tempQuestion.includeIf = tempIncludeIf
            }catch (e: Exception){ }

            try {
                val valuesArray = question.get("values") as? JSONArray
                if (valuesArray != null){
                    for (v in 0 until valuesArray.length()){
                        try{
                            values.add(valuesArray.getString(v))
                        }catch (e: Exception){ }
                    }
                }
            }catch (e: Exception){ }

            when (tempQuestion.type){
                SurveyContainerActivity.SINGLE_MULTIPLE_ANSWERS -> {
                    tempQuestion.singleMultipleAnswers = values
                }
                SurveyContainerActivity.MULTIPLE_CHOICE -> {
                    tempQuestion.multipleChoisesAnswers = values
                }
                SurveyContainerActivity.FILL_IN_THE_BLANK -> {
                    tempQuestion.fillBlanksChoises = values
                }
            }
            thequestions.add(tempQuestion)
        }
        return thequestions
    }
}