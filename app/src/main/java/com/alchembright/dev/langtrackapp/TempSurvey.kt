package com.alchembright.dev.langtrackapp
/*
import uk.ac.york.langtrackapp.data.model.Question
import uk.ac.york.langtrackapp.data.model.Survey
import uk.ac.york.langtrackapp.screen.survey.SurveyAdapter2
import java.util.*

object TempSurvey {

    fun getTempSurvey(number: String): Survey{
        val tempQuestionList = mutableListOf<Question>()
        val q0 = Question(
            type = SurveyAdapter2.HEADER_VIEW,
            id = "id",
            previous = 0,
            index = 0,
            next = 1,
            title = "THE LANG-TRACK-APP\nHumanistlaboratoriet",
            text = "Hej (användarnamn)\n\nNu är det dags att svara på frågor om din språkinlärning!",
            description = ""
        )
        tempQuestionList.add(q0)
        val q1 = Question(
            type = SurveyAdapter2.LIKERT_SCALES,
            id = "id",
            previous = 0,
            index = 1,
            next = 2,
            title = "LikertScale titel",
            text = "Här skrivs ett påstående som deltagaren graderar hur mycket det stämmer",
            description = "Hur mycket stämmer följande påstående?\n1: stämmer inte alls\n5: stämmer helt"
        )
        tempQuestionList.add(q1)
        val q2 = Question(
            type = SurveyAdapter2.FILL_IN_THE_BLANK,
            id = "id",
            previous = 1,
            index = 2,
            next = 3,
            title = "FillInTheBlank titel",
            text = "Här är texten i FillInTheBlank",
            description = ""
        )
        tempQuestionList.add(q2)
        val q3 = Question(
            type = SurveyAdapter2.MULTIPLE_CHOICE,
            id = "id",
            previous = 2,
            index = 3,
            next = 4,
            title = "MultipleChoise titel",
            text = "Här är texten i MultipleChoise",
            description = ""
        )
        tempQuestionList.add(q3)
        val q4 = Question(
            type = SurveyAdapter2.SINGLE_MULTIPLE_ANSWERS,
            id = "id",
            previous = 3,
            index = 4,
            next = 5,
            title = "SingleMultipleAnswer titel",
            text = "Här är texten i SingleMultipleAnswer",
            description = ""
        )
        tempQuestionList.add(q4)
        val q5 = Question(
            type = SurveyAdapter2.OPEN_ENDED_TEXT_RESPONSES,
            id = "id",
            previous = 4,
            index = 5,
            next = 6,
            title = "OpenEndedTextResponses titel",
            text = "Här är texten i OpenEndedTextResponses",
            description = ""
        )
        tempQuestionList.add(q5)
        val q6 = Question(
            type = SurveyAdapter2.FOOTER_VIEW,
            id = "id",
            previous = 5,
            index = 6,
            next = 0,
            title = "Tack för dina svar.",
            text = "Om du är nöjd med dina svar välj 'Skicka in'\nannars kan du stega bak för att redigera.",
            description = ""
        )
        tempQuestionList.add(q6)

        tempQuestionList.sortBy { it.index }
        val tempSurvey = Survey()
        tempSurvey.questions = tempQuestionList
        tempSurvey.date = Date().time
        tempSurvey.footerText = ""
        tempSurvey.headerText = ""
        tempSurvey.title = "Testsurvey $number"

        return tempSurvey
    }
}*/