package com.alvdela.smartspend.filters

import android.text.InputFilter
import android.text.Spanned

class DecimalDigitsInputFilter(private val decimalDigits: Int) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val builder = StringBuilder(dest)
        builder.replace(dstart, dend, source?.subSequence(start, end).toString())
        val matcher = Regex("(([1-9][0-9]*)|(0)|([1-9])*)(\\.[0-9]{0,$decimalDigits})?")
        return if (!builder.toString().matches(matcher)) {
            if (source.isNullOrEmpty()) dest?.subSequence(dstart, dend) else ""
        } else null
    }
}
