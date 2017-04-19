package com.soywiz.korge.tictactoe

import com.soywiz.korge.Korge
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container

fun main(args: Array<String>) = Korge(TicTacToeModule)

object TicTacToeModule : Module() {
	override val mainScene: Class<out Scene> = TicTacToeMainScene::class.java
}

class TicTacToeMainScene : Scene() {
	suspend override fun sceneInit(sceneView: Container) {
	}
}