package com.soywiz.korge.admob

import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*

suspend fun AdmobCreateDefault(views: Views, testing: Boolean): Admob = object : Admob(views) {
	override suspend fun available(): Boolean = false
	override suspend fun bannerShow() {
		views.onAfterRender {
			it.batch.drawQuad(
					it.getTex(Bitmaps.white),
					x = 0f,
					y = 0f,
					width = it.ag.mainRenderBuffer.width.toFloat(),
					height = 86f,
					colorMul = Colors["#f0f0f0"],
					m = Matrix()
			)
		}
	}
}
