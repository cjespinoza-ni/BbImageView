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
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.ViewSwitcher

/**
 * Displays images using [BbImageView]
 * extends [ViewSwitcher] to apply transition effects when the image changes
 *
 * @attr ref [R.styleable.BbImageViewSwitcher_src]
 * @attr ref [R.styleable.BbImageViewSwitcher_show_blurred_background]
 * @attr ref [R.styleable.BbImageViewSwitcher_blur_radius]
 */
class BbImageViewSwitcher(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : ViewSwitcher(context, attrs) {
    constructor(context: Context, attrs: AttributeSet?) : this(context,attrs, 0)
    constructor(context: Context) : this(context,null, 0)

    private var mShowBlurredBackground: Boolean
    private var mBlurRadius: Float

    var showBlurredBackground: Boolean
        /**
         * Return whether to show a blurred background.
         * @return whether to show a blurred background.
         */
        get() = mShowBlurredBackground
        /**
         * Sets whether to show a blurred background.
         * @param value whether to show a blurred background.
         */
        set(value) {
            if(value != mShowBlurredBackground) {
                mShowBlurredBackground = value
                handleShowBlurRadiusChanged()
            }
        }

    var blurRadius:Float
        /**
         * Gets the radius to use when blurring the background.
         * @return the radius used to blur the background drawable.
         */
        get() = mBlurRadius
        /**
         * Sets the radius of the blur to apply to the background.
         * Supported range 0 < radius <= 25
         * @param value The radius of the blur
         */
        set(value) {
            if(value <= 0 || value > 25) return
            if(value != mBlurRadius) {
                mBlurRadius=value
                handleBlurRadiusChanged()
            }
        }

    init {
        BbImageView(context).also {
            this.addView(it)
        }
        BbImageView(context).also {
            this.addView(it)
        }

        val a = context.obtainStyledAttributes(
            attrs, R.styleable.BbImageViewSwitcher, defStyleAttr, 0
        )

        mShowBlurredBackground = a.getBoolean(R.styleable.BbImageViewSwitcher_show_blurred_background, DEFAULT_SHOW_BLURRED_BACKGROUND)
        mBlurRadius = a.getFloat(R.styleable.BbImageViewSwitcher_blur_radius, DEFAULT_BLUR_RADIUS)

        if(a.hasValue(R.styleable.BbImageViewSwitcher_src)){
            a.getDrawable(R.styleable.BbImageViewSwitcher_src)?.also {
                setImageDrawable(it)
            }
        }

        a.recycle()

        //update children values
        handleShowBlurRadiusChanged()
        handleBlurRadiusChanged()
    }

    /**
     * Sets a drawable as the content of this view.
     * @param drawable the Drawable to set, or {@code null} to clear the content
     */
    fun setImageDrawable(drawable: Drawable?) {
        nextView.also {
            if(it is BbImageView)
                it.setImageDrawable(drawable)
            showNext()
        }
    }

    /**
     * Sets a bitmap as the content of this view.
     * @param bitmap the Bitmap to set, or {@code null} to clear the content
     */
    fun setImageBitmap(bitmap: Bitmap?) {
        nextView.also {
            if(it is BbImageView)
                it.setImageBitmap(bitmap)
            showNext()
        }
    }

    /**
     * Sets a drawable as the content of this view.
     * @param resId the resource identifier of the drawable
     */
    fun setImageSrc(resId: Int) {
        nextView.also {
            if(it is BbImageView)
                it.setImageResource(resId)
            showNext()
        }
    }

    /**
     * Sets the content of this ImageView to the specified Uri.
     * Note that you use this method to load images from a local Uri only.
     * @param uri the Uri of an image, or {@code null} to clear the content
     */
    fun setImageURI(uri: Uri?) {
        nextView.also {
            if(it is BbImageView)
                it.setImageURI(uri)
            showNext()
        }
    }

    /**
     * Updates children's [BbImageView.showBlurredBackground]
     */
    private fun handleShowBlurRadiusChanged() {
        for(i in 0 until childCount) {
            getChildAt(i)?.also {
                if(it is BbImageView)
                    it.showBlurredBackground = mShowBlurredBackground
            }
        }
    }

    /**
     * Updates children's [BbImageView.blurRadius]
     */
    private fun handleBlurRadiusChanged() {
        for(i in 0 until childCount) {
            getChildAt(i)?.also {
                if(it is BbImageView)
                    it.blurRadius = mBlurRadius
            }
        }
    }
}