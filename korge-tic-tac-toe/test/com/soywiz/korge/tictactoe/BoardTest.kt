package com.soywiz.korge.tictactoe

import org.junit.Test
import kotlin.test.assertEquals

class BoardTest {
	@Test
	fun name() {
		val board = Board()
		assertEquals(null, board.winner)
		board[0, 0] = Chip.CROSS
		board[1, 0] = Chip.CROSS
		assertEquals(null, board.winner)
		board[2, 0] = Chip.CROSS
		assertEquals(Chip.CROSS, board.winner)
	}
}
