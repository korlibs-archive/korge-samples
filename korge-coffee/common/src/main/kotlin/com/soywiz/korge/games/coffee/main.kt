package com.soywiz.korge.games.coffee

import com.soywiz.korge.Korge
import com.soywiz.korge.animate.*
import com.soywiz.korge.bitmapfont.BitmapFont
import com.soywiz.korge.component.docking.jellyButton
import com.soywiz.korge.event.addEventListener
import com.soywiz.korge.input.mouse
import com.soywiz.korge.resources.Path
import com.soywiz.korge.resources.getPath
import com.soywiz.korge.scene.*
import com.soywiz.korge.service.Browser
import com.soywiz.korge.service.storage.Storage
import com.soywiz.korge.service.storage.item
import com.soywiz.korge.time.TimeSpan
import com.soywiz.korge.time.seconds
import com.soywiz.korge.time.sleep
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.ColorTransform
import com.soywiz.korio.async.AsyncSignal
import com.soywiz.korio.async.Promise
import com.soywiz.korio.async.go
import com.soywiz.korio.inject.AsyncInjector
import com.soywiz.korio.inject.Optional
import com.soywiz.korio.inject.Singleton
import com.soywiz.korio.lang.JvmStatic
import com.soywiz.korio.util.closeable
import com.soywiz.korma.geom.Anchor
import com.soywiz.korma.geom.Point2d
import com.soywiz.korma.geom.SizeInt
import com.soywiz.korma.random.MtRand
import com.soywiz.korma.random.get
import kotlin.math.max

@Suppress("unused")
object KorgeCoffeeModule : Module() {
	@JvmStatic
	fun main(args: Array<String>) = Korge(KorgeCoffeeModule, injector = AsyncInjector().prepareKoffee())

	fun AsyncInjector.prepareKoffee() = this.apply {
		mapSingleton { Storage() }
		mapSingleton { com.soywiz.korge.service.Browser() }
		mapSingleton { GameStorage(get()) }
		mapSingleton { LibraryContainer(getPath("font.fnt"), getPath("main.ani")) }
		mapPrototype { CreditsScene(get(), get(), get()) }
		mapPrototype { MainMenuScene(get(), get()) }
		mapPrototype { MainScene(get(), getOrNull(), get()) }
	}

	object MainDebug {
		@JvmStatic
		fun main(args: Array<String>) = Korge(KorgeCoffeeModule, sceneClass = MainScene::class, injector = AsyncInjector().prepareKoffee(), sceneInjects = listOf(MainScene.State.MAIN_MENU), debug = true)
	}

	// Go directly to ingame to avoid testing main menu
	object IngameDebug {
		//@JvmStatic fun main(args: Array<String>) = Korge.invoke(KorgeCoffeeModule, sceneClass = MainScene::class.java, sceneInjects = listOf(MainScene.State.INGAME), debug = true)
		@JvmStatic
		fun main(args: Array<String>) = Korge.invoke(KorgeCoffeeModule, sceneClass = MainScene::class, injector = AsyncInjector().prepareKoffee(), sceneInjects = listOf(MainScene.State.INGAME), debug = false)
	}

	override val title: String = "KorGE Coffee"
	override val mainScene = MainScene::class
	override val size: SizeInt = SizeInt(720, 1280)
	override val windowSize: SizeInt = size * 0.75
	override val icon: String = "icon.png"
	override val plugins = super.plugins + listOf(
		AnLibraryPlugin
	)

	@Singleton
	class GameStorage(
		val storage: Storage
	) {
		var HiScore by storage.item("HiScore") { 0 }
	}

	suspend override fun init(injector: AsyncInjector) {
		injector.get<Views>().registerPropertyTriggerSuspend("disabled") { view, key, value ->
			view.mouseEnabled = false
		}
	}

	@Singleton
	class LibraryContainer(
		@Path("font.fnt") val font: BitmapFont,
		@Path("main.ani") val library: AnLibrary
	)

	class CreditsScene(
		val libraryContainer: LibraryContainer,
		val browser: Browser,
		val gameStorage: GameStorage
	) : Scene() {
		val lib = libraryContainer.library
		suspend override fun sceneInit(sceneView: Container) {
			sceneView += lib.createMovieClip("Credits")
			sceneView["korge"].jellyButton(1.2).onClick { browser.browse("http://korge.soywiz.com/") }
			sceneView["kotlin"].jellyButton(1.2).onClick { browser.browse("https://kotlinlang.org/") }
			sceneView["animate"].jellyButton(1.2).onClick { browser.browse("http://www.adobe.com/products/animate.html") }
			sceneView["github"].jellyButton(1.2).onClick { browser.browse("https://github.com/soywiz/korge-samples/tree/master/korge-coffee") }
			sceneView["soywiz"].jellyButton(1.2).onClick { browser.browse("http://soywiz.com/") }
			sceneView["tamy"].jellyButton(1.2).onClick { browser.browse("http://comic.tamy.es/") }
			sceneView["close"].jellyButton(1.2).onClick { this.sceneContainer.back(time = 0.3.seconds) }
		}

		suspend override fun sceneBeforeLeaving() {
			super.sceneBeforeLeaving()
			sceneView.mouseEnabled = false
		}
	}

	class MainMenuScene(
		val libraryContainer: LibraryContainer,
		val gameStorage: GameStorage
	) : Scene() {
		val lib = libraryContainer.library
		lateinit var creditsSC: SceneContainer
		val onStart = AsyncSignal<Unit>()

		suspend override fun sceneInit(sceneView: Container) {
			sceneView += lib.createMovieClip("MainMenu")
			creditsSC = views.sceneContainer()
			sceneView += creditsSC
			sceneView["playButton"].jellyButton(1.2).onClick {
				onStart(Unit)
			}
			sceneView["creditsButton"].jellyButton(1.2).onClick {
				creditsSC.pushTo<CreditsScene>(time = 0.2.seconds)
			}
			sceneView["hiscore"].setText("${gameStorage.HiScore}")
		}

		suspend override fun sceneBeforeLeaving() {
			super.sceneBeforeLeaving()
			sceneView.mouseEnabled = false
		}
	}

	class MainScene(
		val lib: LibraryContainer,
		@Optional val initialState: State?,
		val gameStorage: GameStorage
	) : Scene() {
		enum class State { MAIN_MENU, INGAME }

		lateinit var camera: Camera
		lateinit var hud: View
		lateinit var ingame: View
		lateinit var mainMenuSC: SceneContainer

		private suspend fun openMainMenu(transition: Boolean, callback: suspend () -> Unit) {
			hud.alpha = 0.0
			val mainMenu = mainMenuSC.pushTo<MainMenuScene>()
			mainMenu.onStart {
				callback()
			}
			if (transition) {
				camera.tweenTo(
					sceneView["menuCamera"]!!,
					sceneView["action"]!!::colorTransform[ColorTransform.Add(-255, -255, -255, 0)],
					time = 0.5.seconds, easing = Easing.EASE_OUT_QUAD
				)
			} else {
				camera.setTo(sceneView["menuCamera"]!!)
				sceneView["action"]?.colorTransform = ColorTransform.Add(-255, -255, -255, 0)
			}
		}

		private suspend fun closeMainMenu() {
			go {
				mainMenuSC.back(time = 1.seconds, transition = AlphaTransition.withEasing(Easing.EASE_OUT_QUAD))
			}
		}

		suspend override fun sceneInit(sceneView: Container) {
			camera = views.camera()

			sceneView += lib.library.createMovieClip("Cameras").apply { visible = false }
			sceneView += camera.apply {
				ingame = lib.library.createMovieClip("Ingame")
				this += ingame
			}
			hud = lib.library.createMovieClip("Hud")
			//(hud["scoreLabel"] as? AnTextField?)?.format = (hud["scoreLabel"] as? AnTextField?)?.format?.copy(face = Html.FontFace.Bitmap(lib.font))!!
			//(hud["scoreText"] as? AnTextField?)?.format = (hud["scoreText"] as? AnTextField?)?.format?.copy(face = Html.FontFace.Bitmap(lib.font))!!
			sceneView += hud
			mainMenuSC = views.sceneContainer()
			sceneView += mainMenuSC

			when (initialState) {
				null, State.MAIN_MENU -> {
					hud.alpha = 0.0
					openMainMenu(transition = false) {
						closeMainMenu()
						startGame()
						ingame()
					}
				}
				State.INGAME -> {
					camera.setTo(sceneView["ingameCamera"]!!)
					sceneView["action"]?.colorTransform = ColorTransform.Add(0, 0, 0, 0)
					sceneView["background"]?.alpha = 1.0
					ingame()
				}
			}

			sceneView["pauseButton"]?.mouseEnabled = false
			sceneView["pauseButton"].jellyButton(1.125).onClick {
				//sceneView["ingame"]?.speed = 0.0
				updateHiScore()
				ingame.speed = 0.0

				val oldCamera = camera.localMatrix.copy()

				openMainMenu(transition = true) {
					closeMainMenu()
					camera.tween(
						camera::localMatrix[oldCamera],
						sceneView["action"]!!::colorTransform[ColorTransform.Add(0, 0, 0, 0)],
						hud::alpha[1.0],
						time = 0.5.seconds, easing = Easing.EASE_OUT_QUAD
					)
					ingame.speed = 1.0
				}

				//mainMenuSC.pushTo<MainMenuScene>()
			}

		}

		suspend fun startGame() {
			allowSpeedUp {
				go {
					val action = sceneView["action"]
					action?.tween(action::colorTransform[ColorTransform.Add(0, 0, 0, 0)], time = 5.seconds, easing = Easing.LINEAR)
				}
				go {
					hud.tween(hud::alpha[1.0], time = 2.seconds, easing = Easing.LINEAR)
				}
				camera.tweenTo(sceneView["showCamera"], time = 2.seconds, easing = Easing.EASE_IN_OUT_QUAD)
				sceneView["messages"]?.speed = 0.7
				sceneView["messages"].playAndWaitEvent("tap", "tap_continue")
				camera.sleep(0.5.seconds)
				go {
					val background = sceneView["background"]
					background?.tween(background::alpha[1.0], time = 1.seconds, easing = Easing.EASE_OUT_ELASTIC)
				}
				camera.tweenTo(sceneView["ingameCamera"], time = 1.seconds, easing = Easing.EASE_OUT_ELASTIC)
			}
		}

		var score = 0

		private fun updateScore() {
			sceneView["scoreText"].setText("$score")
		}

		private fun incrementScore(delta: Int) {
			score += delta
			updateScore()
		}

		private suspend fun incrementScore(view: View, delta: Int) {
			val scoreView = sceneView["score"] ?: views.container()
			//val entityHigh = sceneView["entityHigh"] ?: views.container()
			val entityHigh = sceneView["hudContainer"] ?: views.container()
			val text = views.text("+$delta", textSize = 16.0).apply { entityHigh += this }
			text.globalX = view.globalX
			text.globalY = view.globalY
			text.tween(
				text::globalX[scoreView.globalX],
				text::globalY[scoreView.globalY],
				text::alpha[0.0],
				time = 0.3.seconds
			)
			text.removeFromParent()
			scoreView.tween(
				scoreView::scale[0.7],
				time = 0.2.seconds,
				easing = Easing.EASE_OUT_ELASTIC
			)
			incrementScore(delta)
			scoreView.tween(
				scoreView::scale[1.0],
				time = 0.2.seconds,
				easing = Easing.EASE_OUT_ELASTIC
			)
		}

		private val colorAddBlack = ColorTransform.Add(-255, -255, -255, 0).colorAdd
		private val colorAddNormal = ColorTransform.Add(0, 0, 0, 0).colorAdd

		class DismissKotlinMessage(val givePoints: Boolean)

		fun destroyAllEntities() {
			sceneView.dispatch(DismissKotlinMessage(givePoints = false))
		}

		var spawner = Promise.resolved(Unit)
		var running = true

		private fun updateHiScore() {
			gameStorage.HiScore = max(gameStorage.HiScore, score)
		}

		suspend fun allowSpeedUp(callback: suspend () -> Unit) {
			val events = listOf(
				views.stage.mouse.onDown { sceneView.speed = 5.0 },
				views.stage.mouse.onUpAnywhere { sceneView.speed = 1.0 }
			).closeable()
			try {
				callback()
			} finally {
				events.close()
				sceneView.speed = 1.0
			}
		}

		suspend fun gameOver() {
			allowSpeedUp {
				updateHiScore()
				sceneView["pauseButton"]?.alpha = 0.0
				destroyAllEntities()
				spawner.cancel()
				running = false
				camera.tweenTo(sceneView["showCamera"], time = 0.5.seconds, easing = Easing.EASE_OUT_ELASTIC)
				sceneView["action"].waitEvent("mainLoop")
				sceneView["action"].playAndWaitEvent("drop", "dropZoom")
				camera.tweenTo(sceneView["zoomCamera"], time = 0.5.seconds, easing = Easing.EASE_OUT_QUAD)
				sceneView["action"].waitStop()
				sceneView["messages"].playAndWaitStop("gameover")
				sceneView.sleep(1.seconds)
				sceneView.tween(sceneView::colorTransform[ColorTransform.Add(-255, -255, -255, 0)], time = 0.5.seconds)
				this.sceneContainer.changeTo<MainScene>(initialState ?: State.MAIN_MENU)
				//sceneView["action"].play("drop")
			}
		}

		fun createEntity(globalPos: Point2d, scale: Double, reachTime: TimeSpan): View {
			val kotlinHigh = lib.library.createMovieClip("KotlinHigh")
			val kotlinLow = lib.library.createMovieClip("KotlinLow")
			val entityHigh = sceneView["entityHigh"] ?: views.container()
			val entityLow = sceneView["entityLow"] ?: views.container()
			entityHigh += kotlinHigh
			entityLow += kotlinLow
			// Synchronize low part with high part
			entityHigh.addUpdatable {
				kotlinLow.globalMatrix = kotlinHigh.globalMatrix
				kotlinLow.alpha = kotlinHigh.alpha
				kotlinLow.colorAdd = kotlinHigh.colorAdd
			}

			val localDestination = kotlinHigh.globalToLocal(sceneView["rope"]!!.globalBounds.getAnchoredPosition(Anchor.MIDDLE_CENTER))

			kotlinHigh.globalX = globalPos.x
			kotlinHigh.globalY = globalPos.y

			var cancelled = false

			val movePromise = go {
				kotlinHigh.tween(
					kotlinHigh::scale[0.8, scale],
					kotlinHigh::colorAdd[colorAddBlack, colorAddNormal].color(),
					time = 0.3.seconds
				)

				//val distance = Math.hypot(globalDestination.x - kotlin.globalX, globalDestination.y - kotlin.globalY)

				kotlinHigh.tween(
					kotlinHigh::x[localDestination.x],
					kotlinHigh::y[localDestination.y],
					kotlinHigh::scale[kotlinHigh.scale * 0.75].easeOutQuad(),
					//time = (distance * 5).milliseconds
					time = reachTime
				)

				if (!cancelled) { // @TODO: Shouldn't be necessary since cancelling should cancell the whole process
					go {
						gameOver()
					}
				}
			}

			kotlinHigh.addEventListener<DismissKotlinMessage> {
				kotlinHigh.mouseEnabled = false
				cancelled = true
				movePromise.cancel()
				go {
					if (it.givePoints) {
						go {
							incrementScore(kotlinHigh, delta = +1)
						}
					}
					go {
						kotlinHigh.tween(
							kotlinHigh::colorAdd[colorAddBlack].color(),
							kotlinHigh::scale[0.0],
							time = 0.3.seconds
						)
						kotlinHigh.removeFromParent()
						kotlinLow.removeFromParent()
						//createEntity(x = random[0.0, 600.0 * 2], y = random[0.0, 1200.0 * 2], scale = 2.5)
					}
				}
			}

			kotlinHigh.mouse.apply { hitTestType = View.HitTestType.SHAPE }.onDown {
				kotlinHigh.dispatch(DismissKotlinMessage(givePoints = true))
			}
			go {
				while (true) {
					kotlinHigh.tween(kotlinHigh::rotationDegrees[0, 360], time = 2.seconds)
				}
			}
			return kotlinHigh
		}

		val random = MtRand()

		data class DifficultyConfig(
			val spawnCount: Int = 2,
			val nextSpawnTime: ClosedRange<TimeSpan> = 1.seconds..1.seconds,
			val scale: ClosedRange<Double> = 2.5..2.5,
			val reachTime: ClosedRange<TimeSpan> = 2.seconds..2.seconds
		)

		private fun buildDifficultyConfig(step: Int): DifficultyConfig {
			val spawnCount = when {
				step < 5 -> 1
				step < 20 -> random[listOf(1, 2)]
				step < 30 -> random[listOf(1, 2, 2)]
				step < 50 -> random[listOf(1, 2, 2, 2, 3)]
				else -> random[listOf(1, 2, 2, 2, 3, 3, 3, 3, 3, 3)]
			}

			val reachTime = when {
				step < 50 -> 2.seconds
				else -> 1.5.seconds
			} * spawnCount

			return DifficultyConfig(
				spawnCount = spawnCount,
				nextSpawnTime = when {
					step < 5 -> 1.8.seconds..2.2.seconds
					step < 10 -> 1.5.seconds..2.0.seconds
					step < 20 -> 1.seconds..1.5.seconds
					else -> 0.8.seconds..1.seconds
				},
				scale = when {
					step < 50 -> 2.5..2.5
					step < 100 -> 2.0..2.5
					step < 200 -> 1.5..2.0
					else -> 1.0..1.5
				},
				reachTime = reachTime..reachTime * 1.1
			)
		}

		suspend fun spawner() {
			val spawnZones = sceneView.descendantsWithProp("spawnZone")

			var step = 0
			while (running) {
				val config = buildDifficultyConfig(step)
				for (n in 0 until config.spawnCount) {
					val point = random[random[spawnZones].globalBounds]
					createEntity(globalPos = point, scale = random[config.scale], reachTime = random[config.reachTime])
				}
				ingame.sleep(random[config.nextSpawnTime])
				step++
			}
		}

		suspend fun ingame() {
			sceneView["pauseButton"]?.mouseEnabled = true
			spawner = go {
				spawner()
			}
		}
	}
}

