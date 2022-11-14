package co.temy.securitysample.extentions

import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat

fun Resources.getDrawableCompat(id: Int, theme: Resources.Theme? = null) = ResourcesCompat.getDrawable(this, id, theme)
fun Resources.getColorCompat(id: Int, theme: Resources.Theme? = null) = ResourcesCompat.getColor(this, id, theme)