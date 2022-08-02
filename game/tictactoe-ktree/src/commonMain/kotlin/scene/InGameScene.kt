package scene

import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.ktree.*
import com.soywiz.korio.file.std.*
import model.*

class InGameScene(

) : Scene() {
	suspend override fun SContainer.sceneInit() {
		addChild(resourcesVfs["board.ktree"].readKTree(views))
	}

	suspend override fun SContainer.sceneMain() {
		var model = TicTacToeModel()

		setResultText("")

		var turn = CellKind.CROSS

		// Register the input events
		setModel(model)
		for (y in 0 until 3) {
			for (x in 0 until 3) {
				getCell(x, y).first.mouse {
					onOver { if (model.checkResult() is GameResult.InProgress) cellSetHighlight(x, y, true) }
					onOut { if (model.checkResult() is GameResult.InProgress) cellSetHighlight(x, y, false) }
					onUp {
						val result = model.checkResult()

						if (result is GameResult.InProgress) {
							if (model.checkValidCell(x, y)) {
								val transition = model.place(x, y, turn)
								model = transition.newModel
								executeTransition(transition)
								turn = if (turn == CellKind.CROSS) CellKind.CIRCLE else CellKind.CROSS
								//cellSetKind(x, y, model.CellKind.CIRCLE)

								val result = model.checkResult()
								when (result) {
									is GameResult.Winner -> {
										setResultText("${result.player} wins")
										for (cell in result.winnerCells) {
											cellSetHighlight(cell.x, cell.y, true)
										}
									}
									GameResult.Tie -> {
										setResultText("TIE!")
									}
									GameResult.InProgress -> Unit
								}
							}
						} else {
							sceneContainer.changeTo<InGameScene>()
							//model = model.TicTacToeModel()
							//setModel(model)
							//setResultText("")
						}
					}
				}
			}
		}
	}


	fun Container.setResultText(text: String) {
		val gameResultView = this["gameresult"]
		(gameResultView.firstOrNull as? Text?)?.text = text
	}

	fun Container.executeTransition(transition: TicTacToeTransition) {
		for (command in transition.commands) {
			when (command) {
				is Command.PlaceChip -> {
					cellSetKind(command.x, command.y, command.kind)
				}
			}
		}
	}

	fun Container.setModel(model: TicTacToeModel) {
		for (y in 0 until 3) {
			for (x in 0 until 3) {
				cellSetKind(x, y, model.board[x, y])
				cellSetHighlight(x, y, false)
			}
		}
	}

	fun Container.getCell(row: Int, column: Int): QView = this["row$row"]["cell$column"]

	fun Container.cellSetKind(row: Int, column: Int, kind: CellKind) {
		val cell = getCell(row, column)
		cell["cross"].alpha(if (kind == CellKind.CROSS) 1.0 else 0.0)
		cell["circle"].alpha(if (kind == CellKind.CIRCLE) 1.0 else 0.0)
	}

	fun Container.cellSetHighlight(row: Int, column: Int, highlight: Boolean) {
		val cell = getCell(row, column)
		cell["highlight"].alpha(if (highlight) 0.2 else 0.0)
	}
}
