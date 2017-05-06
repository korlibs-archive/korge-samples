package com.soywiz.korge.games.coffee

import com.soywiz.korge.Korge
import com.soywiz.korge.animate.AnLibrary
import com.soywiz.korge.component.docking.jekllyButton
import com.soywiz.korge.resources.Path
import com.soywiz.korge.scene.*
import com.soywiz.korge.service.Browser
import com.soywiz.korge.time.seconds
import com.soywiz.korge.time.sleep
import com.soywiz.korge.tween.Easing
import com.soywiz.korge.tween.get
import com.soywiz.korge.tween.tween
import com.soywiz.korge.view.Camera
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.camera
import com.soywiz.korge.view.get
import com.soywiz.korim.color.ColorTransform
import com.soywiz.korio.async.AsyncSignal
import com.soywiz.korio.async.go
import com.soywiz.korio.inject.Singleton
import java.net.URL

object KorgeCoffeeModule : Module() {
	@JvmStatic fun main(args: Array<String>) = Korge(this@KorgeCoffeeModule)

	override val title: String = "KorGE Coffee"
	override val mainScene: Class<out Scene> = MainScene::class.java
	override val width: Int = (720 * 0.75).toInt()
	override val height: Int = (1280 * 0.75).toInt()
	override val virtualHeight: Int = 1280
	override val virtualWidth: Int = 720
	override val icon: String = "icon.png"

	object StartMessage
	//object CloseCredits

	@Singleton
	class LibraryContainer(
		@Path("main.ani") val library: AnLibrary
	)

	class CreditsScene(
		val lib: LibraryContainer,
		val browser: Browser
	) : Scene() {
		suspend override fun sceneInit(sceneView: Container) {
			sceneView += lib.library.createMovieClip("Credits")
			sceneView["korge"].jekllyButton(1.2).onClick { browser.browse(URL("http://korge.soywiz.com/")) }
			sceneView["kotlin"].jekllyButton(1.2).onClick { browser.browse(URL("https://kotlinlang.org/")) }
			sceneView["animate"].jekllyButton(1.2).onClick { browser.browse(URL("http://www.adobe.com/products/animate.html")) }
			sceneView["github"].jekllyButton(1.2).onClick { browser.browse(URL("https://github.com/soywiz/korge-samples/tree/master/korge-coffee")) }
			sceneView["soywiz"].jekllyButton(1.2).onClick { browser.browse(URL("http://soywiz.com/")) }
			sceneView["tamy"].jekllyButton(1.2).onClick { browser.browse(URL("http://comic.tamy.es/")) }
			sceneView["close"].jekllyButton(1.2).onClick { this.sceneContainer.back(time = 0.3.seconds) }
		}

		suspend override fun sceneBeforeLeaving() {
			super.sceneBeforeLeaving()
			sceneView.mouseEnabled = false
		}
	}

	class MainMenuScene(
		val lib: LibraryContainer
	) : Scene() {
		lateinit var creditsSC: SceneContainer
		val onStart = AsyncSignal<Unit>()

		suspend override fun sceneInit(sceneView: Container) {
			sceneView += lib.library.createMovieClip("MainMenu")
			creditsSC = views.sceneContainer()
			sceneView += creditsSC
			sceneView["playButton"].jekllyButton(1.2).onClick {
				//bus.send(StartMessage)
				onStart(Unit)
			}
			sceneView["creditsButton"].jekllyButton(1.2).onClick {
				creditsSC.pushTo<CreditsScene>(time = 0.2.seconds)
			}
			//o {
			//	sleep(1.seconds)
			//	bus.send(StartMessage)
			//
		}

		suspend override fun sceneBeforeLeaving() {
			super.sceneBeforeLeaving()
			sceneView.mouseEnabled = false
		}
	}

	class MainScene(
		val lib: LibraryContainer
	) : Scene() {
		lateinit var camera: Camera
		lateinit var mainMenuSC: SceneContainer

		//@BusHandler suspend fun handle(s: StartMessage) {
		//	//views.clearEachFrame = false
		//	go {
		//		mainMenuSC.changeTo<EmptyScene>(time = 1.seconds, transition = AlphaTransition.withEasing(Easing.EASE_OUT_QUAD))
		//	}
		//	startGame()
		//	//views.clearEachFrame = true
		//}

		suspend override fun sceneInit(sceneView: Container) {
			camera = views.camera()

			sceneView += lib.library.createMovieClip("Cameras").apply { visible = false }
			sceneView += camera.apply {
				this += lib.library.createMovieClip("Ingame")
			}
			mainMenuSC = views.sceneContainer()
			sceneView += mainMenuSC
			val mainMenu = mainMenuSC.pushTo<MainMenuScene>()
			mainMenu.onStart {
				go {
					mainMenuSC.back(time = 1.seconds, transition = AlphaTransition.withEasing(Easing.EASE_OUT_QUAD))
				}
				startGame()
			}
			//println(sceneView["action"])
			//println(sceneView["menuCamera"]?.getGlobalBounds())
			//println(sceneView["ingameCamera"]?.getGlobalBounds())

			//println(camera.getLocalMatrixFittingView(sceneView["ingameCamera"]!!))
			camera.setTo(sceneView["menuCamera"]!!)
			sceneView["action"]?.colorTransform = ColorTransform.Add(-255, -255, -255, 0)
		}

		suspend fun startGame() {
			go {
				val action = sceneView["action"]
				action?.tween(action::colorTransform[ColorTransform.Add(0, 0, 0, 0)], time = 5.seconds, easing = Easing.LINEAR)
			}
			camera.tweenTo(sceneView["showCamera"], time = 2.seconds, easing = Easing.EASE_IN_OUT_QUAD)
			camera.sleep(0.5.seconds)
			go {
				val background = sceneView["background"]
				background?.tween(background::alpha[1.0], time = 1.seconds, easing = Easing.EASE_OUT_ELASTIC)
			}
			camera.tweenTo(sceneView["ingameCamera"], time = 1.seconds, easing = Easing.EASE_OUT_ELASTIC)
		}
	}
}
