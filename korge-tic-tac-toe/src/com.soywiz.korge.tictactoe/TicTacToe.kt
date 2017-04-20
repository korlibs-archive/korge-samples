package com.soywiz.korge.tictactoe

import com.soywiz.korge.Korge
import com.soywiz.korge.animate.AnLibrary
import com.soywiz.korge.animate.play
import com.soywiz.korge.input.onClick
import com.soywiz.korge.resources.Path
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.View
import com.soywiz.korge.view.descendantsWithPropInt
import com.soywiz.korge.view.get
import com.soywiz.korio.async.Signal
import com.soywiz.korio.util.Extra
import com.soywiz.korma.ds.Array2

fun main(args: Array<String>) = Korge(TicTacToeModule)

object TicTacToeModule : Module() {
	override val mainScene: Class<out Scene> = TicTacToeMainScene::class.java
}

var Board.Cell.view by Extra.Property<View?> { null }
val Board.Cell.onPress by Extra.Property { Signal<Unit>() }

fun Board.Cell.set(type: Board.Cell.Type) {
	this.value = type
	view.play(when (type) {
		Board.Cell.Type.EMPTY -> "empty"
		Board.Cell.Type.CIRCLE -> "circle"
		Board.Cell.Type.CROSS -> "cross"
	})
}

fun Board.Cell.init(view: View) {
	this.view = view
	set(this.value)
	view["hit"].onClick {
		onPress(Unit)
	}
}

class TicTacToeMainScene(
	@Path("main.swf") val mainLibrary: AnLibrary
) : Scene() {
	val board = Board()

	suspend override fun sceneInit(sceneView: Container) {
		sceneView += mainLibrary.createMainTimeLine()

		for ((rowView, row) in sceneView.descendantsWithPropInt("row")) {
			for ((cellView, cell) in rowView.descendantsWithPropInt("cell")) {
				board.cells[row, cell].init(cellView)
				//println("$rowId, $cellId")
			}
		}

		var turn = true
		for (cell in board.cells) {
			cell.onPress {
				if (turn) {
					cell.set(Board.Cell.Type.CIRCLE)
				} else {
					cell.set(Board.Cell.Type.CROSS)
				}
				turn = !turn

			}
		}
	}
}