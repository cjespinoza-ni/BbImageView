/*
 * Copyright (C) 2020 Carlos Joel Espinoza Perez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cjespinozani.widget.extensions.drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.cjespinozani.widget.extensions.bitmap.createScaledCopy

/**
 * Creates a [Bitmap] copy of this Drawable scaled to [maxLength]
 * @param maxLength maximum length to scale the blurred copy
 * @return the scaled bitmap [Bitmap] copy
 */
fun Drawable.createScaledBitmapCopy(
    maxLength: Int=300,
    config: Bitmap.Config? = null
): Bitmap {
    // if this is a [BitmapDrawable] returns a copy of the bitmap instead
    if(this is BitmapDrawable)
        return bitmap.createScaledCopy(maxLength = maxLength)

    // or else proceeds to create a scaled bitmap copy by drawing onto a canvas

    // gets scale factor to match the desired maxLength
    val scaleFactor = this.getScaleFactor(maxLength)

    // gets old bounds values to restore after drawing
    val oldLeft = bounds.left
    val oldTop = bounds.top
    val oldRight = bounds.right
    val oldBottom = bounds.bottom

    val width = (intrinsicWidth * scaleFactor).toInt()
    val height = (intrinsicHeight * scaleFactor).toInt()

    val bitmapCopy = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
    setBounds(0, 0, width, height)
    draw(Canvas(bitmapCopy))

    // restores original bounds
    setBounds(oldLeft, oldTop, oldRight, oldBottom)

    return bitmapCopy
}


/**
 * Helper function to get the scale factor needed to match this Drawable's longest side with the [maxLength] provided
 * @param maxLength desired to match by longest side of this Drawable
 * @return scaleFactor to apply
 */
private fun Drawable.getScaleFactor(maxLength: Int): Double {
    val longestLength = maxOf(intrinsicWidth, intrinsicHeight)
    var scaleFactor:Double = 1.0
    if(maxLength < longestLength){
        scaleFactor = maxLength.toDouble() / longestLength
    }
    return scaleFactor
}