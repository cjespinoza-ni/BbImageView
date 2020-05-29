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

package com.cjespinozani.widget.extensions.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

/**
 * Creates a copy of this Bitmap scaled to [maxLength]
 * @param maxLength maximum length to scale the blurred bitmap copy
 * @return the blurred bitmap copy
 */
fun Bitmap.createScaledCopy(
    maxLength: Int = width
): Bitmap {
    val scaleFactor = getScaleFactorToMatch(maxLength)
    return Bitmap.createScaledBitmap(this, (scaleFactor*this.width).toInt(), (scaleFactor*this.height).toInt(),false)
}

/**
 * Helper function to get the scale factor needed to match this Bitmap's longest side with the [maxLength] provided
 * @param maxLength desired to match by longest side of this Bitmap
 * @return scaleFactor to apply
 */
fun Bitmap.getScaleFactorToMatch(maxLength: Int): Double {
    val longestLength = maxOf(width, height)
    return maxLength.toDouble() / longestLength.toDouble()
}

/**
 * Applies blur effect to this [Bitmap]
 * @param context
 * @param blurRadius radius of the Blur to apply
 * @return this bitmap
 */
fun Bitmap.applyBlur(
    context: Context?,
    blurRadius: Float
): Bitmap {
    val rs = RenderScript.create(context)
    val input = Allocation.createFromBitmap(rs, this)
    val output = Allocation.createTyped(rs, input.type)
    val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    script.setRadius(blurRadius)
    script.setInput(input)
    script.forEach(output)
    output.copyTo(this)
    return this
}