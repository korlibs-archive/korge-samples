package com.soywiz.korge.tictactoe

import com.soywiz.korge.Korge
import com.soywiz.korge.animate.AnLibrary
import com.soywiz.korge.resources.Path
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.descendantsWithPropInt

fun main(args: Array<String>) = Korge(TicTacToeModule)

object TicTacToeModule : Module() {
	override val mainScene: Class<out Scene> = TicTacToeMainScene::class.java
}

// Controller
class TicTacToeMainScene(
	@Path("main.swf") val mainLibrary: AnLibrary
) : Scene() {
	val board = Board(3, 3)

	suspend override fun sceneInit(sceneView: Container) {
		sceneView += mainLibrary.createMainTimeLine()

		for ((rowView, row) in sceneView.descendantsWithPropInt("row")) {
			for ((cellView, cell) in rowView.descendantsWithPropInt("cell")) {
				board.cells[row, cell].init(cellView)
			}
		}

		var turn = true
		for (cell in board.cells) {
			cell.onPress {
				cell.set(if (turn) Chip.CIRCLE else Chip.CROSS)
				turn = !turn
				println("Winner: ${board.winner}")
			}
		}
	}
}