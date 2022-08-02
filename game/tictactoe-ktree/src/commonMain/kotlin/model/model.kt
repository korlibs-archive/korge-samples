package model

import com.soywiz.kds.*
import com.soywiz.korma.geom.*

data class TicTacToeModel(
	val board: Array2<CellKind> = Array2(3, 3) { CellKind.EMPTY }
) {
	companion object {
		operator fun invoke(str: String) = TicTacToeModel(Array2(str) { char, x, y ->
			when (char) {
				'X' -> CellKind.CROSS
				'O' -> CellKind.CIRCLE
				else -> CellKind.EMPTY
			}
		})
	}

	override fun toString(): String = board.toStringList({ when (it) {
        CellKind.EMPTY -> '.'
        CellKind.CROSS -> 'X'
        CellKind.CIRCLE -> 'O'
    }}).joinToString("\n")
}

enum class CellKind { CIRCLE, CROSS, EMPTY }

sealed class GameResult {
	data class Winner(val player: CellKind, val winnerCells: List<PointInt>) : GameResult()
	object Tie : GameResult()
	object InProgress : GameResult()
}

sealed class Command {
	data class PlaceChip(val x: Int, val y: Int, val kind: CellKind) : Command()
}

data class TicTacToeTransition(
		val oldModel: TicTacToeModel,
		val newModel: TicTacToeModel,
		val commands: List<Command>
)

fun TicTacToeModel.checkValidCell(x: Int, y: Int): Boolean = board[x, y] == CellKind.EMPTY

fun TicTacToeModel.place(x: Int, y: Int, kind: CellKind): TicTacToeTransition {
	return TicTacToeTransition(
			oldModel = this,
			newModel = TicTacToeModel(this.board.map2 { cx, cy, cv -> if (cx == x && cy == y) kind else cv }),
			commands = listOf(
				Command.PlaceChip(x, y, kind)
			)
	)
}

fun TicTacToeModel.checkLine(x: Int, y: Int, dx: Int, dy: Int): GameResult.Winner? {
	val startCellValue = board[x, y]
	val points = arrayListOf<PointInt>()
	val lineWithAllSameChipsNonEmpty = (0 until 3).all { n ->
		val px = x + (dx * n)
		val py = y + (dy * n)
		val value = board[px, py]
		points.add(PointInt(px, py))
		value == startCellValue && value != CellKind.EMPTY
	}
	return if (lineWithAllSameChipsNonEmpty) GameResult.Winner(startCellValue, points) else null
}

fun TicTacToeModel.checkResult(): GameResult {
	// . | . | .
	// ---------
	// . | . | .
	// ---------
	// . | . | .

	for (n in 0 until 3) {
		checkLine(n, 0, 0, +1)?.let { return it } // Verticals
		checkLine(0, n, +1, 0)?.let { return it } // Horizontals
	}
	checkLine(0, 0, +1, +1)?.let { return it } // Diagonal1
	checkLine(2, 0, -1, +1)?.let { return it } // Diagonal2

	if (board.all { it != CellKind.EMPTY }) {
		return GameResult.Tie
	}

	return GameResult.InProgress
}
