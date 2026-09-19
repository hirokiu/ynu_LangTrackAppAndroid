package com.alchembright.dev.langtrackapp.util

import com.alchembright.dev.langtrackapp.data.model.Answer
import com.alchembright.dev.langtrackapp.data.model.Question

/** Render labels while leaving serialized answer values untouched. */
object AnswerReview {
    fun value(question: Question, answer: Answer?, missing: String): String {
        if (answer == null) return missing
        return when (question.type) {
            "open" -> answer.openEndedAnswer?.takeIf { it.isNotBlank() } ?: missing
            "likert" -> answer.likertAnswer?.let { if (it in 0..4) (it + 1).toString() else missing } ?: missing
            "single" -> answer.singleMultipleAnswer?.let { question.singleMultipleAnswers?.getOrNull(it) } ?: missing
            "blanks" -> answer.fillBlankAnswer?.let { question.fillBlanksChoises?.getOrNull(it) } ?: missing
            "multi" -> answer.multipleChoiceAnswer?.mapNotNull { question.multipleChoisesAnswers?.getOrNull(it) }?.joinToString("、")?.takeIf { it.isNotEmpty() } ?: missing
            "duration" -> answer.timeDurationAnswer?.let { "%d:%02d".format(it / 3600, (it % 3600) / 60) } ?: missing
            "slider" -> answer.sliderScaleAnswer?.toString() ?: missing
            else -> missing
        }
    }
}
