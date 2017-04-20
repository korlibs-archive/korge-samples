package com.soywiz.korge.tictactoe

import com.soywiz.korge.animate.play
import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.View
import com.soywiz.korge.view.get
import com.soywiz.korio.async.Signal
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

fun Board.Cell.highlight(highlight: Boolean) {
	view["highlight"].play(if (highlight) "highlight" else "none")
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
