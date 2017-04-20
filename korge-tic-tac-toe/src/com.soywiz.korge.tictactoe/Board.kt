package com.soywiz.korge.tictactoe

import com.soywiz.korio.util.Extra
import com.soywiz.korma.ds.Array2
import com.soywiz.korma.geom.IPoint

enum class Chip { EMPTY, CROSS, CIRCLE }

class Board(val width: Int = 3, val height: Int = width, val lineSize: Int = width) {
	class Cell(val x: Int, val y: Int) : Extra by Extra.Mixin() {
		val pos = IPoint(x, y)
		var value = Chip.EMPTY
	}

	val cells = Array2(width, height) { Cell(it % width, it / width) }

	fun inside(x: Int, y: Int) = cells.inside(x, y)

	fun select(x: Int, y: Int, dx: Int, dy: Int, size: Int): List<Cell>? {
		if (!inside(x, y)) return null
		if (!inside(x + dx * (size - 1), y + dy * (size - 1))) return null
		return (0 until size).map { cells[x + dx * it, y + dy * it] }
	}

	val lines = arrayListOf<List<Cell>>()

	init {
		fun addLine(line: List<Cell>?) {
			if (line != null) lines += line
		}
		for (y in 0 .. height) {
			for (x in 0 .. width) {
				addLine(select(x, y, 1, 0, lineSize))
				addLine(select(x, y, 0, 1, lineSize))
				addLine(select(x, y, 1, 1, lineSize))
				addLine(select(width - x - 1, y, -1, 1, lineSize))
			}
		}
	}

	operator fun get(x: Int, y: Int) = cells[x, y]
	operator fun set(x: Int, y: Int, value: Chip) = run { cells[x, y].value = value }

	val Iterable<Cell>.chipLine: Chip? get() {
		val expected = this.first().value
		return if (expected == Chip.EMPTY) null else if (this.all { it.value == expected }) expected else null
	}

	val moreMovements: Boolean get() = cells.any { it.value == Chip.EMPTY }

	val winnerLine: List<Cell>? get() {
		for (line in lines) if (line.chipLine != null) return line
		return null
	}

	val winner: Chip? get() {
		return winnerLine?.firstOrNull()?.value
	}
}