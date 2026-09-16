package com.alchembright.dev.langtrackapp.screen.contact

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.databinding.ContactActivityBinding
import com.alchembright.dev.langtrackapp.screen.about.TeamViewModel
import com.alchembright.dev.langtrackapp.screen.about.TeamViewModelFactory
import com.alchembright.dev.langtrackapp.util.asUri
import com.alchembright.dev.langtrackapp.util.getLanguageCode
import com.alchembright.dev.langtrackapp.util.openInBrowser

class ContactActivity : AppCompatActivity() {


    private lateinit var mBind :ContactActivityBinding
    private lateinit var viewModel : TeamViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind = DataBindingUtil.setContentView(this, R.layout.contact_activity)
        mBind.lifecycleOwner = this
        mBind.executePendingBindings()

        viewModel = ViewModelProvider(this, TeamViewModelFactory(this))
            .get(TeamViewModel::class.java)

        mBind.contactScroll.setOnScrollChangeListener { _, _, _, _, _ ->
            mBind.contactTopView.isSelected = mBind.contactScroll.canScrollVertically(-1)
        }

        //mBind.researchTextView.text = getString(R.string.reserchText1)

        mBind.contactLtaLinkTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        mBind.contactLtaLinkTextView.setOnClickListener {
            "https://portal.research.lu.se/portal/en/projects/the-langtrackapp-studying-exposure-to-and-use-of-a-new-language-using-smartphone-technology(4e734940-981f-4dd0-841a-eb6ac760af0c).html"
                .asUri().openInBrowser(this)
        }

        mBind.contactHumLinkTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        mBind.contactHumLinkTextView.setOnClickListener {
            "https://www.humlab.lu.se/en/"
                .asUri().openInBrowser(this)
        }

        mBind.contactLuLinkTextView.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        mBind.contactLuLinkTextView.setOnClickListener {
            "https://www.lu.se/"
                .asUri().openInBrowser(this)
        }
        mBind.contactTopCloseButton.setOnClickListener {
            onBackPressed()
        }
        setContactInfo()
    }

    private fun setContactInfo() {

        var tempText = ""
        viewModel.getContactInfo { result ->
            for (i in result.indices) {
                val contact = result[i]
                tempText += "${contact.text?.get(getLanguageCode())}\n${contact.email}"
                if (i != result.count() - 1){
                    tempText += "\n\n"
                }
            }
            val spannableString1 = SpannableString(tempText)

            for (contact in result) {
                val mail = contact.email
                val clickableSpanResearch = object : ClickableSpan() {
                    override fun onClick(textView: View) {
                        val to = mail
                        val subject = getString(R.string.mail_subject)
                        val mailTo = "mailto:" + to +
                                "?&subject=" + Uri.encode(subject)
                        val emailIntent = Intent(Intent.ACTION_VIEW)
                        emailIntent.data = Uri.parse(mailTo)
                        startActivity(emailIntent)
                    }
                }
                val mailIndex = tempText.indexOf(mail)
                val mailEndIndex = mailIndex + mail.count()

                spannableString1.setSpan(clickableSpanResearch,
                    mailIndex,
                    mailEndIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            mBind.contactInfoTextView.text = spannableString1
            mBind.contactInfoTextView.movementMethod = LinkMovementMethod.getInstance()
            mBind.contactInfoTextView.highlightColor = Color.CYAN
        }
    }


    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context, ContactActivity::class.java))
        }
    }
}
