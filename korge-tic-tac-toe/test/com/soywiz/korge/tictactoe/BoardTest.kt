package com.soywiz.korge.tictactoe

import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class BoardTest {
	@Test
	fun name() {
		val board = Board()
		Assert.assertEquals(null, board.winner)
		board[0, 0] = Chip.CROSS
		board[1, 0] = Chip.CROSS
		Assert.assertEquals(null, board.winner)
		board[2, 0] = Chip.CROSS
		Assert.assertEquals(Chip.CROSS, board.winner)
	}
}