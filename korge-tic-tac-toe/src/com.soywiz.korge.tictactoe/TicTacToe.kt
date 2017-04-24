package com.soywiz.korge.tictactoe

import com.soywiz.korge.Korge
import com.soywiz.korge.animate.AnLibrary
import com.soywiz.korge.bitmapfont.BitmapFont
import com.soywiz.korge.input.mouse
import com.soywiz.korge.resources.Mipmaps
import com.soywiz.korge.resources.Path
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.descendantsWithPropInt
import com.soywiz.korge.view.get
import com.soywiz.korge.view.setText
import com.soywiz.korio.async.Signal
import com.soywiz.korio.async.go
import com.soywiz.korio.async.waitOne
import com.soywiz.korma.geom.PointInt

fun main(args: Array<String>) = Korge(TicTacToeModule)

object TicTacToeModule : Module() {
	override val mainScene: Class<out Scene> = TicTacToeMainScene::class.java
	override val title: String = "tic-tac-toe"
	override val icon: String = "icon.png"
}

// Controller
class TicTacToeMainScene(
	@Mipmaps @Path("main.swf") val mainLibrary: AnLibrary,
	@Path("font/font.fnt") val font: BitmapFont
) : Scene() {
	val board = Board(3, 3)
	lateinit var game: Game

	suspend override fun sceneInit(sceneView: Container) {
		sceneView += mainLibrary.createMainTimeLine()

		for ((rowView, row) in sceneView.descendantsWithPropInt("row")) {
			for ((cellView, cell) in rowView.descendantsWithPropInt("cell")) {
				board.cells[row, cell].init(cellView)
			}
		}

		val p1 = InteractivePlayer(board, Chip.CROSS)
		val p2 = BotPlayer(board, Chip.CIRCLE)
		//val p2 = InteractivePlayer(board, Chip.CIRCLE)

		game = Game(board, listOf(p1, p2))
		destroyCancellables += go {
			while (true) {
				game.board.reset()
				val result = game.game()

				println(result)

				val results = mainLibrary.createMovieClip("Results")
				//(results["result"] as AnTextField).format?.face = Html.FontFace.Bitmap(font)
				when (result) {
					is Game.Result.DRAW -> results["result"].setText("DRAW")
					is Game.Result.WIN -> {
						results["result"].setText("WIN")
						for (cell in result.cells) cell.highlight(true)
						for (cell in game.board.cells.toList() - result.cells) cell.lowlight(true)
					}
				}
				sceneView += results
				results["hit"]?.mouse?.onClick?.waitOne()
				//sceneView -= results
				results.removeFromParent()

			}
		}
	}
}

interface Player {
	val chip: Chip
	suspend fun move(): PointInt
}

class Game(val board: Board, val players: List<Player>) {
	interface Result {
		object DRAW : Result
		class WIN(val player: Player?, val cells: List<Board.Cell>) : Result
	}

	suspend fun game(): Result {
		var turn = 0
		while (board.moreMovements) {
			val currentPlayer = players[turn % players.size]
			while (true) {
				val pos = currentPlayer.move()
				println(pos)
				if (board.cells[pos].value == Chip.EMPTY) {
					board.cells[pos].setAnimate(currentPlayer.chip)
					break
				}
			}
			if (board.winner != null) return Result.WIN(currentPlayer, board.winnerLine ?: listOf())
			turn++
		}
		return Result.DRAW
	}
}

class BotPlayer(val board: Board, override val chip: Chip) : Player {
	suspend override fun move(): PointInt {
		for (cell in board.cells) {
			if (cell.value == Chip.EMPTY) {
				return cell.pos
			}
		}
		throw IllegalStateException("No more movements")
	}
}

class InteractivePlayer(val board: Board, override val chip: Chip) : Player {
	val clicked = Signal<PointInt>()

	init {
		for (cell in board.cells) {
			cell.onPress {
				clicked(cell.pos)
			}
		}
	}

	suspend override fun move(): PointInt {
		return clicked.waitOne()
	}

}