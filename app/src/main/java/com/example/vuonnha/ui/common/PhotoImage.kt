package com.example.vuonnha.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageCache {
    private val cache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 8).toInt()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    suspend fun load(path: String): Bitmap? {
        cache.get(path)?.let { return it }
        return withContext(Dispatchers.IO) {
            val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, o)
            var s = 1
            while (o.outWidth / s > 1024) s *= 2
            BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = s })?.also { cache.put(path, it) }
        }
    }
}

@Composable fun PhotoImage(path: String, modifier: Modifier = Modifier) {
    val bmp by produceState<Bitmap?>(null, path) { value = ImageCache.load(path) }
    bmp?.let { Image(it.asImageBitmap(), null, modifier, contentScale = ContentScale.Crop) }
}