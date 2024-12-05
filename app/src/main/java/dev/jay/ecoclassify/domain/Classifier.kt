package dev.jay.ecoclassify.domain

import android.graphics.Bitmap

interface Classifier {
    fun classify(bitmap: Bitmap, rotation: Int): List<Classification>
}