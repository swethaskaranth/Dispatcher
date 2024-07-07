package com.goflash.dispatch.util

import android.content.Context
import android.graphics.PorterDuff
import android.widget.TextView
import android.widget.Toast.*
import androidx.core.content.ContextCompat
import com.goflash.dispatch.R

object Toaster {
    fun show(context: Context, text: String) {
        val toast = makeText(context, text, LENGTH_SHORT)
       /* toast.view.background.setColorFilter(
            ContextCompat.getColor(context, R.color.black_border_color), PorterDuff.Mode.SRC_IN
        )
        val textView = toast.view.findViewById(android.R.id.message) as TextView
        textView.setTextColor(ContextCompat.getColor(context, R.color.white))*/
        toast.show()
    }
}