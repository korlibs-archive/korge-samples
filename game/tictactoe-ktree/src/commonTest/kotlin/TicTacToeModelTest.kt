import com.soywiz.korma.geom.*
import model.*
import kotlin.test.*

class TicTacToeModelTest {
	@Test
	fun test() {
		assertEquals(
				"""
					...
					.X.
					...
				""".trimIndent(),
				TicTacToeModel().place(1, 1, CellKind.CROSS).newModel.toString()
		)
	}

	@Test
	fun testResult() {
		assertEquals(GameResult.InProgress, TicTacToeModel().checkResult())
		assertEquals(GameResult.InProgress, TicTacToeModel().place(1, 1, CellKind.CROSS).newModel.checkResult())

		assertEquals(GameResult.Winner(CellKind.CROSS, listOf(PointInt(1, 0), PointInt(1, 1), PointInt(1, 2))), TicTacToeModel("""
			.X.
			.X.
			.X.
		""".trimIndent()).checkResult())

		assertEquals(GameResult.Tie, TicTacToeModel("""
			OXX
			XOO
			OXX
		""".trimIndent()).checkResult())

		assertEquals(GameResult.InProgress, TicTacToeModel("""
			OXX
			XO.
			OXX
		""".trimIndent()).checkResult())
	}
}
