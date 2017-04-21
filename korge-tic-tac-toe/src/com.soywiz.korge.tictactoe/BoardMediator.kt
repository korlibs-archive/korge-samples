package com.soywiz.korge.tictactoe

import com.soywiz.korge.animate.play
import com.soywiz.korge.input.onClick
import com.soywiz.korge.tween.Easings
import com.soywiz.korge.tween.rangeTo
import com.soywiz.korge.tween.tween
import com.soywiz.korge.tween.withEasing
import com.soywiz.korge.view.View
import com.soywiz.korge.view.get
import com.soywiz.korio.async.Signal
import com.soywiz.korio.async.async
import com.soywiz.korio.util.Extra

var Board.Cell.view by Extra.Property<View?> { null }
val Board.Cell.onPress by Extra.Property { Signal<Unit>() }

fun Board.Cell.set(type: Chip) {
	this.value = type
	view.play(when (type) {
		Chip.EMPTY -> "empty"
		Chip.CIRCLE -> "circle"
		Chip.CROSS -> "cross"
	})
}

fun Board.Cell.setAnimate(type: Chip) {
	set(type)
	async {
		view?.tween(
			(View::alpha..0.7..1.0).withEasing(Easings.LINEAR),
			(View::scale..0.8..1.0).withEasing(Easings.EASE_OUT_ELASTIC),
			time = 300
		)
	}
}

fun Board.Cell.highlight(highlight: Boolean) {
	view["highlight"].play(if (highlight) "highlight" else "none")
	async {
		view?.tween(
			View::scale..0.1..1.2,
			View::rotationDegrees..360.0,
			time = 600, easing = Easings.EASE_OUT_ELASTIC
		)
	}
}

fun Board.Cell.lowlight(lowlight: Boolean) {
	async {
		view?.tween(
			View::scale..0.8,
			View::alpha..0.5,
			time = 300, easing = Easings.EASE_OUT_QUAD
		)
	}
}

fun Board.reset() {
	for (cell in cells) {
		cell.set(Chip.EMPTY)
		cell.highlight(false)
	}
}

fun Board.Cell.init(view: View) {
	this.view = view
	set(this.value)
	view["hit"].onClick {
		onPress(Unit)
	}
}
