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

package com.cjespinozani.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import com.cjespinozani.widget.extensions.bitmap.applyBlur
import com.cjespinozani.widget.extensions.bitmap.createScaledCopy
import com.cjespinozani.widget.extensions.drawable.createScaledBitmapCopy

/**
 * BbImageView extends [android.widget.ImageView]' functionality
 * by adding a blur background when configured
 *
 * @attr ref [R.styleable.BbImageView_show_blurred_background]
 * @attr ref [R.styleable.BbImageView_blur_radius]
 */
class BbImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    private var mShowBlurredBackground: Boolean
    private var mBlurRadius: Float

    /**
     * blurredDrawable representation of this view's drawable
     */
    private var blurredBitmap: Bitmap? = null

    var showBlurredBackground: Boolean
        /**
         * Return whether this view shows a blurred background.
         * @return whether this view shows a blurred background.
         */
        get() = mShowBlurredBackground
        /**
         * Sets whether this view will show a blurred background.
         * @param value whether this view will show a blurred background.
         */
        set(value) {
            if(value != mShowBlurredBackground) {
                mShowBlurredBackground = value
                createBlurredBackground(drawable)
                invalidate()
            }
        }

    var blurRadius: Float
        /**
         * Gets the radius to use when blurring this view's background drawable.
         * @return the radius used to blur this view's background drawable.
         */
        get() = mBlurRadius
        /**
         * Sets the radius of the blur to apply to this view's background drawable.
         * Supported range 0 < radius <= 25
         * @param value The radius of the blur
         */
        set(value) {
            if(value <= 0 || value > 25) return
            if(value != mBlurRadius) {
                mBlurRadius = value
                createBlurredBackground(drawable)
                invalidate()
            }
        }

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.BbImageView, defStyleAttr, 0
        )

        mShowBlurredBackground = a.getBoolean(R.styleable.BbImageView_show_blurred_background, DEFAULT_SHOW_BLURRED_BACKGROUND)
        mBlurRadius = a.getFloat(R.styleable.BbImageView_blur_radius, DEFAULT_BLUR_RADIUS)

        a.recycle()
    }

    /**
     * overrides [ImageView.setImageDrawable] to create the blurred drawable representation if configured
     */
    override fun setImageDrawable(drawable: Drawable?) {
        if(this.drawable != drawable)
            createBlurredBackground(drawable)
        super.setImageDrawable(drawable)
    }


    /**
     * overrides [ImageView.setImageResource] to create the blurred drawable representation if configured
     */
    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        createBlurredBackground(drawable)
    }


    /**
     * overrides [ImageView.setImageURI] to create the blurred drawable representation if configured
     */
    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        createBlurredBackground(drawable)
    }

    /**
     * overrides [ImageView.onDraw] to draw a blurred background if configured
     */
    override fun onDraw(canvas: Canvas?) {
        if(mShowBlurredBackground && drawable != null && blurredBitmap == null)
            createBlurredBackground(drawable)
        if (mShowBlurredBackground && canvas != null && blurredBitmap != null) {
            drawBlurredBackground(canvas, blurredBitmap!!)
        }
        super.onDraw(canvas)
    }

    /**
     * Draws blurred background representation of this view's drawable
     */
    private fun drawBlurredBackground(canvas: Canvas, blurredBitmap: Bitmap) {
        val widthScaleFactor = this.width.toDouble() / blurredBitmap.width.toDouble()
        val heightScaleFactor = this.height.toDouble() / blurredBitmap.height.toDouble()

        val scaleFactor = maxOf(widthScaleFactor, heightScaleFactor)

        val width = (scaleFactor * blurredBitmap.width).toInt()
        val height = (scaleFactor * blurredBitmap.height).toInt()

        val horizontalOffset = (this.width - width) / 2
        val verticalOffset = (this.height - height) / 2

        val rect = Rect(horizontalOffset, verticalOffset, width + horizontalOffset, height + verticalOffset)
        canvas.drawBitmap(blurredBitmap, null, rect, null)
    }

    /**
     * Creates an blurred BitmapDrawable representation of this view's drawable
     */
    private fun createBlurredBackground(drawable: Drawable?) {
        if(drawable is BitmapDrawable) {
            createBlurredBackground(drawable.bitmap)
            return
        }

        if (!shouldCreateBlurredBitmap() || drawable == null) {
            blurredBitmap = null
            return
        }

        //RenderScript blur effect will fail if it's in edit mode
        if (!isInEditMode)
            blurredBitmap = drawable.createScaledBitmapCopy(maxLength = 300).applyBlur(context, mBlurRadius)
    }

    /**
     * Creates an blurred BitmapDrawable representation of this view's drawable
     */
    private fun createBlurredBackground(bitmap: Bitmap?) {
        if (!shouldCreateBlurredBitmap() || bitmap == null) {
            blurredBitmap = null
            return
        }

        //RenderScript blur effect will fail if it's in edit mode
        if (!isInEditMode)
            blurredBitmap = bitmap.createScaledCopy(maxLength = 300).applyBlur(context, mBlurRadius)
    }

    /**
     * Whether to create a blurred background by verifying if there will be space to render it
     * @return whether to create the blurred background or not
     */
    private fun shouldCreateBlurredBitmap(): Boolean {
        if(!mShowBlurredBackground){
            return false
        }
        when(scaleType) {
            // if ScaleType is CENTER_CROP or FIT_XY
            // the image will fill the entire view
            // so there's no need to create the blurred background
            ScaleType.CENTER_CROP,
            ScaleType.FIT_XY -> {
                return false
            }
            // if scaleType is FIT_CENTER, FIT_END or FIT_START
            // the image will fill the entire view area if it's aspect ratio is the same as the view's aspect ratio
            // so there's no need to create the blurred background
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.FIT_START -> {
                val viewRatio = width.toDouble() / height.toDouble()
                val drawableRatio = drawable.intrinsicWidth.toDouble() / drawable.intrinsicHeight.toDouble()
                return Math.abs(viewRatio - drawableRatio) > 0.001
            }
            else -> {
                // if the image is smaller than the view
                // the image will fill the entire view area only if it's aspect ratio is the same as the view's aspect ratio
                if(drawable.intrinsicWidth > width || drawable.intrinsicHeight > height){
                    val viewRatio = width.toDouble() / height.toDouble()
                    val drawableRatio = drawable.intrinsicWidth.toDouble() / drawable.intrinsicHeight.toDouble()
                    return Math.abs(viewRatio - drawableRatio) > 0.001
                }
                return true
            }
        }
    }
}