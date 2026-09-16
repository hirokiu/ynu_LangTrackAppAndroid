package com.alchembright.dev.langtrackapp.popup

/*
* Stephan Björck
* Humanistlaboratoriet
* Lunds Universitet
* stephan.bjorck@humlab.lu.se

* Viktor Czyżewski
* RSE Team
* University of York
* */

import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.interfaces.OnBoolPopupReturnListener


class OneChoicePopup: DialogFragment() {

    lateinit var onBoolPopupReturnListener: OnBoolPopupReturnListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.popup_one_choice, container)
        val titleView = rootView.findViewById<TextView>(R.id.popupOneChoiceTitleTextView)
        val infoView = rootView.findViewById<TextView>(R.id.popupOneChoiceInfoTextView)
        val okButton = rootView.findViewById<Button>(R.id.popupOneChoiceOkButton)
        val cancelButton = rootView.findViewById<Button>(R.id.popupOneChoiceCancelButton)

        popupWidth = arguments?.getInt(WIDTH)
        val title = arguments?.getString(TITLE)
        val infoText = arguments?.getString(INFO_TEXT)
        val okButtonText = arguments?.getString(OK_BUTTON_TEXT)
        val cancelButtonText = arguments?.getString(CANCEL_BUTTON_TEXT)
        val placeCenter = arguments?.getBoolean(PLACE_CENTER) ?: false

        titleView.text = title
        infoView.text = infoText
        okButton.setText(okButtonText)
        cancelButton.setText(cancelButtonText)
        okButton.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        cancelButton.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        okButton.setOnClickListener {
            dialog?.dismiss()
            if (::onBoolPopupReturnListener.isInitialized) {
                onBoolPopupReturnListener.popupReturn(true)
            }
        }
        cancelButton.setOnClickListener {
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
        val TITLE = "title"
        val INFO_TEXT = "infoText"
        val PLACE_CENTER = "placecenter"
        val OK_BUTTON_TEXT = "okButtonText"
        val CANCEL_BUTTON_TEXT = "cancelButtonText"
        var popupWidth: Int? = 100

        fun show(width: Int,
                 title: String,
                 infoText: String,
                 okButtonText: String,
                 cancelButtonText: String,
                 placecenter: Boolean,
                 cancelable: Boolean = true): OneChoicePopup {
            return OneChoicePopup().apply {
                this.isCancelable = cancelable
                arguments = Bundle().apply {
                    putInt(WIDTH, width)
                    putString(TITLE, title)
                    putString(INFO_TEXT, infoText)
                    putBoolean(PLACE_CENTER, placecenter)
                    putString(OK_BUTTON_TEXT, okButtonText)
                    putString(CANCEL_BUTTON_TEXT, cancelButtonText)
                }
            }
        }
    }
}