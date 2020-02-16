package com.soywiz.korge.samples.minesweeper

import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.view.*

// Ported from here: https://github.com/soywiz/lunea/tree/master/samples/busca

suspend fun main() = Korge(width = 800, height = 600, virtualWidth = 640, virtualHeight = 480, title = "Minesweeper") {
	views.registerProcessSystem()
	MainProcess(this)
}

class MainProcess(parent: Container) : Process(parent) {
	val lights = arrayListOf<RandomLight>()

	override suspend fun main() {
		val light = readImage("light.png")
		image(readImage("bg.jpg"))
		for (n in 0 until 20) {
			lights += RandomLight(this, light)
		}

		val imageset = readImage("buscaminas.png")
		val imagenes = imageset.split(imageset.height, imageset.height)
		val click = readSound("click.wav")
		val boom = readSound("boom.wav")

		val board = Board(this, imageset, imagenes, click, boom, 22, 15, 40)

		while (true) {
			if (key[Key.ESCAPE]) {
				error("ESC!")
			}
			if (key[Key.UP]) {
				lights += RandomLight(this, light)
			}
			if (key[Key.DOWN]) {
				if (lights.isNotEmpty()) {
					lights.removeAt(lights.size - 1).destroy()
				}
			}
			board.updateTimeText()
			frame()
		}
	}
}

