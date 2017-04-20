package com.soywiz.korge.tictactoe

import com.soywiz.korio.util.Extra
import com.soywiz.korma.ds.Array2

enum class Chip { EMPTY, CROSS, CIRCLE }

class Board(val width: Int = 3, val height: Int = width) {
	class Cell : Extra by Extra.Mixin() {
		var value = Chip.EMPTY
	}

	val cells = Array2(width, height) { Cell() }

	operator fun get(x: Int, y: Int) = cells[x, y]
	operator fun set(x: Int, y: Int, value: Chip) = run { cells[x, y].value = value }

	val rows = (0 until height).map { row(it) }
	val columns = (0 until width).map { column(it) }

	fun row(row: Int) = (0 until width).map { cells[it, row] }
	fun column(column: Int) = (0 until height).map { cells[column, it] }

	val Iterable<Cell>.chipLine: Chip? get() {
		val expected = this.first().value
		return if (expected == Chip.EMPTY) null else if (this.all { it.value == expected }) expected else null
	}

	val winner: Chip? get() {
		for (row in rows) if (row.chipLine != null) return row.chipLine!!
		for (column in columns) if (column.chipLine != null) return column.chipLine!!
		return null
	}
}