package com.alchembright.dev.langtrackapp.util

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.alchembright.dev.langtrackapp.R
import java.text.SimpleDateFormat
import java.util.*

fun AppCompatActivity.loadFragment(fragment: Fragment){
    supportFragmentManager
        .beginTransaction()
        .replace(
            R.id.surveyContainer,
            fragment
        )
        .commit()
}

fun getLanguageCode() : String{
    val langStr = Locale.getDefault().language
    var languageCode = "eng"
    when(langStr){
        "ar" -> languageCode =  "ar"
        "tr" -> languageCode =  "tu"
        "fa" -> languageCode =  "fa"
        "fa-AF" -> languageCode =  "da"
        "sv" -> languageCode =  "swe"
    }
    return languageCode
    /*switch langStr{
        case "ar": languageCode = "ar" //arabic
        case "tr": languageCode = "tu" //turkish
        case "fa": languageCode = "fa" //farsi (persiska, iran)
        case "fa-AF": languageCode = "da" //dari (afganistan)
        case "sv": languageCode = "swe"
        default: languageCode = "eng"
    }*/
    return languageCode
}

fun getDate(milli: Long): String{
    val formatter = SimpleDateFormat("dd MMMM yyyy    HH:mm",
        Locale("sv", "SE")
    )
    val calendar = Calendar.getInstance();
    calendar.timeInMillis = milli * 1000//TODO: temp, should be in milli
    return formatter.format(calendar.time)
}

fun pxToDp(px: Int, context: Context): Int{
    val density = context.resources.displayMetrics.density
    return Math.round(px.toFloat() / density)
}

fun dpToPx(dp: Int, context: Context): Int{
    val density = context.resources.displayMetrics.density
    return Math.round(dp.toFloat() * density)
}

//inString: 2020-03-16T22:15:25.820Z
fun String.toDate(dateFormat: String = "yyyy-MM-dd'T'HH:mm:ss", timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date? {
    val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
    parser.timeZone = timeZone
    return parser.parse(this.substringBefore('.'))
}

fun Resources.formatStringWithArabicNumbers(resId: Int, vararg args: Any?)
        = String.format(Locale.US, this.getString(resId), *args)

fun Resources.formatArabicNumbersToString(resId: Int)
        = String.format(Locale.US, resId.toString())

fun Date.formatToReadable(dateFormat: String, timeZone: TimeZone = TimeZone.getDefault()): String {
    val formatter = SimpleDateFormat(dateFormat, Locale.ENGLISH)
    formatter.timeZone = timeZone
    return formatter.format(this)
}
fun String?.asUri(): Uri? {
    try {
        return Uri.parse(this)
    } catch (e: Exception) {}
    return null
}
fun Uri?.openInBrowser(context: Context) {
    this ?: return // Do nothing if uri is null

    val browserIntent = Intent(Intent.ACTION_VIEW, this)
    ContextCompat.startActivity(context, browserIntent, null)
}

fun View.setMarginTop(topMargin: Int) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(params.leftMargin, topMargin, params.rightMargin, params.bottomMargin)
    layoutParams = params
}