package com.soywiz.korge.tictactoe

import com.soywiz.korge.Korge
import com.soywiz.korge.animate.AnLibrary
import com.soywiz.korge.animate.AnLibraryPlugin
import com.soywiz.korge.input.mouse
import com.soywiz.korge.plugin.KorgePlugin
import com.soywiz.korge.resources.getPath
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.descendantsWithPropInt
import com.soywiz.korge.view.get
import com.soywiz.korge.view.setText
import com.soywiz.korio.async.Signal
import com.soywiz.korio.async.go
import com.soywiz.korio.async.waitOne
import com.soywiz.korio.error.invalidOp
import com.soywiz.korio.inject.AsyncInjector
import com.soywiz.korio.lang.JvmStatic
import com.soywiz.korma.geom.PointInt

object TicTacToe {
	@JvmStatic
	fun main(args: Array<String>) = Korge(TicTacToeModule, injector = AsyncInjector().generatedInject())
}

object TicTacToeModule : Module() {
	override val mainScene = TicTacToeMainScene::class
	override val title: String = "tic-tac-toe"
	override val icon: String = "icon.png"
	override val plugins: List<KorgePlugin> = super.plugins + listOf(
		AnLibraryPlugin
	)

	suspend override fun init(injector: AsyncInjector) {
		//injector.get<ResourcesRoot>().mapExtensions("swf" to "ani")
		//injector.get<ResourcesRoot>().mapExtensionsJustInJTransc("swf" to "ani")
	}
}

// Controller
class TicTacToeMainScene : Scene() {
	private lateinit var mainLibrary: AnLibrary

	val board = Board(3, 3)
	lateinit var game: Game

	override suspend fun init(injector: AsyncInjector) {
		super.init(injector)

		mainLibrary = injector.getPath(AnLibrary::class, "main.ani")
	}

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
		cancellables += go {
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
		invalidOp("No more movements")
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
