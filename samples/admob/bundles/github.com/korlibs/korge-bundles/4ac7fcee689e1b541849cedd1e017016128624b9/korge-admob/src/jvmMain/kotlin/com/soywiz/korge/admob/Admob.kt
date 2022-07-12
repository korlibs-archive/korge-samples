package com.soywiz.korge.admob

import com.soywiz.korge.view.Views
import com.soywiz.korim.bitmap.Bitmaps
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korma.geom.Matrix

actual suspend fun AdmobCreate(views: Views, testing: Boolean): Admob = AdmobCreateDefault(views, testing)
