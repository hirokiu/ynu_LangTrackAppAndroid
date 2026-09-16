package com.alchembright.dev.langtrackapp.screen.instructions

import com.alchembright.dev.langtrackapp.util.applySystemBarInsets
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.databinding.DataBindingUtil
import com.alchembright.dev.langtrackapp.R
import com.alchembright.dev.langtrackapp.databinding.InstructionsActivityBinding

class InstructionsActivity : AppCompatActivity() {


    private lateinit var mBind :InstructionsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBind = DataBindingUtil.setContentView(this, R.layout.instructions_activity)
        applySystemBarInsets()
        mBind.lifecycleOwner = this
        mBind.executePendingBindings()

        mBind.informationScroll.setOnScrollChangeListener { _, _, _, _, _ ->
            mBind.informationTopView.isSelected = mBind.informationScroll.canScrollVertically(-1)
        }
        mBind.instructionsTopCloseButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        mBind.informationInfoTextView.text = getString(R.string.instructionsText)
        setDataProcessingText()
    }

    fun setDataProcessingText(){

        val builder = SpannableStringBuilder()

        val spannableHeader = SpannableString(getString(R.string.dataProcessingHeader))
        val boldSpan = StyleSpan(Typeface.BOLD)
        val sizeSpan = RelativeSizeSpan(1.1f)
        val colourSpan = ForegroundColorSpan(getColor(R.color.lta_blue))
        val length = getString(R.string.dataProcessingHeader).count()
        spannableHeader.setSpan(boldSpan,0,length , Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableHeader.setSpan(sizeSpan,0,length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        spannableHeader.setSpan(colourSpan,0,length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        builder.append(spannableHeader)
        builder.append("\n\n")

        val spannableStringInfo = SpannableString(getString(R.string.dataProcessingInfo))
        builder.append(spannableStringInfo)

        mBind.informationDataTextView.text = builder
    }

    companion object{
        fun start(context: Context){
            context.startActivity(Intent(context, InstructionsActivity::class.java))
        }
    }
}
