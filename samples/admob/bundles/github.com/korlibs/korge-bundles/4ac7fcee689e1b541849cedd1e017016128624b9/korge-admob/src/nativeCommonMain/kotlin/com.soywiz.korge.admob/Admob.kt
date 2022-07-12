package com.soywiz.korge.admob

import com.soywiz.korge.view.*

actual suspend fun AdmobCreate(views: Views, testing: Boolean): Admob = AdmobCreateDefault(views, testing)
