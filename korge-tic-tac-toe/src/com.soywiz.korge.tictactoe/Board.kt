package com.soywiz.korge.tictactoe

import com.soywiz.korio.util.Extra
import com.soywiz.korma.ds.Array2

class Board {
	class Cell : Extra by Extra.Mixin() {
		enum class Type { EMPTY, CROSS, CIRCLE }
		var value = Type.EMPTY
	}

	val cells = Array2(3, 3) { Cell() }
}