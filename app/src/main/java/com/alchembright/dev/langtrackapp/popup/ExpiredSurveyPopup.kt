package com.alchembright.dev.langtrackapp.popup

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
//import kotlinx.android.synthetic.main.popup_expired_survey.view.*
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.interfaces.OnBoolPopupReturnListener
import com.alchembright.dev.langtrackapp.util.formatToReadable
import com.alchembright.dev.langtrackapp.util.toDate

class ExpiredSurveyPopup: DialogFragment() {

    lateinit var onBoolPopupReturnListener: OnBoolPopupReturnListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.popup_expired_survey, container)
        val titleView = rootView.findViewById<TextView>(R.id.popupExpiredTitleTextView)
        val infoView = rootView.findViewById<TextView>(R.id.popupExpiredInfoTextView)
        val numberTextView = rootView.findViewById<TextView>(R.id.popupExpiredNumberOfQuestionTextView)
        val publishedTextView = rootView.findViewById<TextView>(R.id.popupExpiredPublishedTextView)
        val expiredTextView = rootView.findViewById<TextView>(R.id.popupExpiredExpiredTextView)
        val okButton = rootView.findViewById<ImageButton>(R.id.popupExpiredOkButton)

        popupWidth = arguments?.getInt(WIDTH)
        val infoText = arguments?.getString(TEXT_VIEW_TEXT)
        val expiredText = arguments?.getString(EXPIRED) ?: ""
        val publishedText = arguments?.getString(PUBLISHED) ?: ""
        val questionNumber = arguments?.getInt(NUMBER_OF_QUESTIONS, 0)
        val placeCenter = arguments?.getBoolean(PLACE_CENTER) ?: false

        //okButton.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        infoView.text = infoText
        numberTextView.text = questionNumber.toString()//getString(R.string.numberEnding,questionNumber)
        publishedTextView.text = publishedText.toDate()?.formatToReadable(publishedTextView.context.getString(R.string.dateFormat)) ?: publishedTextView.context.getString(R.string.noDate)
        expiredTextView.text = expiredText.toDate()?.formatToReadable(publishedTextView.context.getString(R.string.dateFormat)) ?: publishedTextView.context.getString(R.string.noDate)

        okButton.setOnClickListener {
            dialog?.dismiss()
        }

        if (dialog != null) {

            if (!placeCenter) {
                //place popup 100 px from top
                val window: Window? = dialog!!.window
                if (window != null) {
                    window.setGravity(Gravity.TOP or Gravity.CENTER)

                    val params: WindowManager.LayoutParams = window.attributes
                    params.y = 100
                    window.attributes = params
                }
            }
        }
        return rootView
    }

    fun setCompleteListener(listener: OnBoolPopupReturnListener){
        this.onBoolPopupReturnListener = listener
    }

    override fun onStart()
    {
        super.onStart()
        val theDialog = dialog
        if (theDialog != null) {
            val width = ViewGroup.LayoutParams.WRAP_CONTENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            theDialog.window?.setLayout(popupWidth ?: width, height)
            theDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
    companion object {
        val WIDTH = "width"
        val PUBLISHED = "published"
        val EXPIRED = "expired"
        val NUMBER_OF_QUESTIONS = "numberOfQuestions"
        val TEXT_VIEW_TEXT = "textViewText"
        val PLACE_CENTER = "placecenter"
        var popupWidth: Int? = 100

        fun show(width: Int,
                 published: String,
                 expired: String,
                 numberOfQuestions: Int,
                 textViewText: String,
                 placecenter: Boolean,
                 cancelable: Boolean = true): ExpiredSurveyPopup {
            return ExpiredSurveyPopup().apply {
                this.isCancelable = cancelable
                arguments = Bundle().apply {
                    putInt(WIDTH, width)
                    putString(PUBLISHED, published)
                    putString(EXPIRED, expired)
                    putInt(NUMBER_OF_QUESTIONS, numberOfQuestions)
                    putString(TEXT_VIEW_TEXT, textViewText)
                    putBoolean(PLACE_CENTER, placecenter)
                }
            }
        }
    }
}