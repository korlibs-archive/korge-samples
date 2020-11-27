package org.korge.sample.kuyo

import com.soywiz.kds.*
import com.soywiz.korim.bitmap.*

class KuyoTiles(val tex: BitmapSlice<Bitmap>) {
	val tiles = Array2(16, 16) { tex }

	//val tile = tex.sliceSize(0, 0, 32, 32)
	//val tile = arrayListOf<SceneTexture>()
	init {
		for (y in 0 until 16) {
			for (x in 0 until 16) {
				tiles[x, y] = tex.sliceWithSize(x * 32, y * 32, 32, 32)
			}
		}
	}

	fun getTex(
		color: Int,
		up: Boolean = false,
		left: Boolean = false,
		right: Boolean = false,
		down: Boolean = false
	): BmpSlice {
		if (color <= 0) return tiles[5, 15]
		val combinable = true
		//val combinable = false
		var offset = 0
		if (combinable) {
			offset += if (down) 1 else 0
			offset += if (up) 2 else 0
			offset += if (right) 4 else 0
			offset += if (left) 8 else 0
		}
		return tiles[offset, color - 1]
	}

	fun getDestroyTex(color: Int): BmpSlice {
		return when (color) {
			1 -> tiles[0, 12]
			2 -> tiles[0, 13]
			3 -> tiles[2, 12]
			4 -> tiles[2, 13]
			5 -> tiles[4, 12]
			else -> tiles[5, 15]
		}
	}

	fun getHintTex(color: Int): BmpSlice {
		return tiles[5 + color - 1, 11]
	}

	val default = getTex(1)
	val colors = listOf(tiles[0, 0]) + (0 until 5).map { getTex(it + 1) }
	val empty = getTex(0)
}
