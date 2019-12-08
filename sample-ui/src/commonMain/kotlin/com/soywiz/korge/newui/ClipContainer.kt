package com.soywiz.korge.newui

import com.soywiz.korag.*
import com.soywiz.korge.render.*
import com.soywiz.korge.view.*

inline fun Container.clipContainer(width: Number, height: Number, callback: @ViewsDslMarker ClipContainer.() -> Unit = {}) =
	ClipContainer(width.toDouble(), height.toDouble()).addTo(this).apply(callback)

// @TODO: Make FixedSizecontainer to have a flag called clip with default = false
// @TODO: Make AG.Scissors mutable to avoid generating GC
open class ClipContainer(
	width: Double = 100.0,
	height: Double = 100.0
) : FixedSizeContainer(width, height) {
	override fun renderInternal(ctx: RenderContext) {
		val c2d = ctx.ctx2d
		val bounds = getGlobalBounds()
		c2d.scissor(AG.Scissor(bounds.x.toInt(), bounds.y.toInt(), bounds.width.toInt(), bounds.height.toInt())) {
			super.renderInternal(ctx)
		}
	}
}

