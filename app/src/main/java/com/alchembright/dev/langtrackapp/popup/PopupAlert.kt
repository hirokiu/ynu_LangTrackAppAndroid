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

class PopupAlert: DialogFragment() {

    lateinit var onAlertPopupReturnListener: OnBoolPopupReturnListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.popup_alert, container)
        val titleView = rootView.findViewById<TextView>(R.id.popupAlertTitleTextView)
        val infoView = rootView.findViewById<TextView>(R.id.popupAlertInfoTextView)
        val okButton = rootView.findViewById<Button>(R.id.popupAlertOkButton)

        popupWidth = arguments?.getInt(WIDTH)
        val title = arguments?.getString(TITLE)
        val infoText = arguments?.getString(TEXT_VIEW_TEXT)
        val placeCenter = arguments?.getBoolean(PLACE_CENTER) ?: false

        okButton.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        titleView.text = title
        infoView.text = infoText

        okButton.setOnClickListener {
            dialog?.dismiss()
            if (::onAlertPopupReturnListener.isInitialized) {
                onAlertPopupReturnListener.popupReturn(false)
            }
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
        this.onAlertPopupReturnListener = listener
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
        val TEXT_VIEW_TEXT = "textViewText"
        val PLACE_CENTER = "placecenter"
        var popupWidth: Int? = 100

        fun show(width: Int,
                 title: String,
                 textViewText: String,
                 placecenter: Boolean,
                 cancelable: Boolean = true): PopupAlert {
            return PopupAlert().apply {
                this.isCancelable = cancelable
                arguments = Bundle().apply {
                    putInt(WIDTH, width)
                    putString(TITLE, title)
                    putString(TEXT_VIEW_TEXT, textViewText)
                    putBoolean(PLACE_CENTER, placecenter)
                }
            }
        }
    }
}
