package com.ruhutasbih.app.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object WidgetBitmapUtil {
    fun loadDownsampledBitmap(context: Context, resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeResource(context.resources, resId, options)
            if (options.outWidth <= 0 || options.outHeight <= 0) return null

            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
                val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
                inSampleSize = if (heightRatio > widthRatio) heightRatio else widthRatio
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize.coerceAtLeast(1)
                this.inPreferredConfig = Bitmap.Config.RGB_565
            }
            val decoded = BitmapFactory.decodeResource(context.resources, resId, decodeOptions) ?: return null
            Bitmap.createScaledBitmap(decoded, reqWidth, reqHeight, true)
        } catch (_: Throwable) {
            null
        }
    }
}