package com.soywiz.korge.samples.minesweeper

import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.service.process.*
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.*

// Ported from here: https://github.com/soywiz/lunea/tree/master/samples/busca

suspend fun main() = Korge(
	width = 800, height = 600,
	virtualWidth = 640, virtualHeight = 480,
	title = "Minesweeper",
	scaleMode = ScaleMode.SHOW_ALL,
	clipBorders = false
) {
	views.registerProcessSystem()
	MainProcess(this)
}


class MainProcess(parent: Container) : Process(parent) {
	val lights = arrayListOf<RandomLight>()

	override suspend fun main() {
		image(readImage("bg.jpg")).dockedTo2(Anchor.TOP_LEFT, ScaleMode.EXACT)
		val light = readImage("light.png")
		val imageset = readImage("buscaminas.png")
		val imagenes = imageset.split(imageset.height, imageset.height)
		val click = readSound("click.wav")
		val boom = readSound("boom.wav")

		for (n in 0 until 20) {
			lights += RandomLight(this, light)
		}

		val board = Board(this, imageset, imagenes, click, boom, 22, 15, 40)
		val nativeProcess = NativeProcess(views)

		while (true) {
			if (key[Key.ESCAPE]) {
				nativeProcess.close()

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

