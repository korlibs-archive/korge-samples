package com.soywiz.korge.samples.simon

import com.soywiz.korge.Korge
import com.soywiz.korge.atlas.Atlas
import com.soywiz.korge.audio.SoundFile
import com.soywiz.korge.audio.SoundSystem
import com.soywiz.korge.audio.readSoundFile
import com.soywiz.korge.input.mouse
import com.soywiz.korge.plugin.KorgePlugin
import com.soywiz.korge.resources.getPath
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.ScaledScene
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.scene.sleep
import com.soywiz.korge.time.seconds
import com.soywiz.korge.ui.UIFactory
import com.soywiz.korge.ui.UIPlugin
import com.soywiz.korge.ui.korui.koruiFrame
import com.soywiz.korge.util.AutoClose
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Image
import com.soywiz.korge.view.image
import com.soywiz.korim.color.Colors
import com.soywiz.korio.async.Signal
import com.soywiz.korio.async.go
import com.soywiz.korio.async.waitOne
import com.soywiz.korio.inject.AsyncInjector
import com.soywiz.korio.inject.InjectorAsyncDependency
import com.soywiz.korio.lang.JvmStatic
import com.soywiz.korma.geom.ISize
import com.soywiz.korma.geom.SizeInt
import com.soywiz.korma.random.MtRand
import com.soywiz.korma.random.get
import com.soywiz.korui.geom.len.Padding
import com.soywiz.korui.geom.len.em
import com.soywiz.korui.style.padding
import com.soywiz.korui.ui.button
import com.soywiz.korui.ui.click
import com.soywiz.korui.ui.horizontal

object Simon : Module() {
	@JvmStatic
	fun main(args: Array<String>) = Korge(this, injector = AsyncInjector()
		.mapPrototype { SelectLevelScene() }
		.mapPrototype { IngameScene() }
	)

	override val size = SizeInt(1280, 720)
	override val windowSize = size * 0.75
	override val title = "Kotlin Simon"
	override val icon = "kotlin/0.png"
	override val mainScene = SelectLevelScene::class

	override val plugins: List<KorgePlugin> = super.plugins + listOf(UIPlugin)

	class Sequence(
		val max: Int,
		val random: MtRand = MtRand()
	) {
		val items = ArrayList<Int>()

		fun ensure(num: Int): List<Int> {
			while (items.size < num) items.add(random[0, max])
			return items
		}
	}

	enum class Difficulty(val items: Int) {
		EASY(3), MEDIUM(4), HARD(6)
	}

	class SelectLevelScene : Scene(), InjectorAsyncDependency {
		lateinit var atlas: Atlas; private set
		lateinit var ui: UIFactory; private set

		override suspend fun init(injector: AsyncInjector) {
			super.init(injector)

			atlas = injector.getPath(Atlas::class, "kotlin.atlas")
			ui = injector.get(UIFactory::class)
		}

		suspend override fun sceneInit(sceneView: Container) {
			// BUG: Kotlin-JS

			sceneView += ui.koruiFrame {
				horizontal {
					padding = Padding(0.2.em)
					button("EASY").click { sceneContainer.pushTo<IngameScene>(Difficulty.EASY) }
					button("MEDIUM").click { sceneContainer.pushTo<IngameScene>(Difficulty.MEDIUM) }
					button("HARD").click { sceneContainer.pushTo<IngameScene>(Difficulty.HARD) }
				}
			}

			//sceneView += ui.koruiFrame {
			//	add(com.soywiz.korui.ui.Container(this.app, HorizontalLayout(app)).apply {
			//		padding = Padding(0.2.em)
			//		button("EASY").click { sceneContainer.pushTo<IngameScene>(Difficulty.EASY) }
			//		button("MEDIUM").click { sceneContainer.pushTo<IngameScene>(Difficulty.MEDIUM) }
			//		button("HARD").click { sceneContainer.pushTo<IngameScene>(Difficulty.HARD) }
			//	})
			//}
		}
	}

	class IngameScene : ScaledScene() {
		private lateinit var atlas: Atlas
		private lateinit var successSound: SoundFile
		private lateinit var failSound: SoundFile
		private lateinit var difficulty: Difficulty
		private lateinit var soundSystem: SoundSystem

		override val sceneSize = ISize(128, 72)
		override val sceneScale: Double = 10.0

		val sequence by lazy { Sequence(difficulty.items) }
		val images = arrayListOf<Image>()
		lateinit var sounds: List<SoundFile>

		override suspend fun init(injector: AsyncInjector) {
			super.init(injector)
			atlas = injector.getPath(Atlas::class, "kotlin.atlas")
			successSound = injector.getPath(SoundFile::class, "sounds/success.wav")
			failSound = injector.getPath(SoundFile::class, "sounds/fail.mp3")
			difficulty = injector.getOrNull(Difficulty::class) ?: Difficulty.MEDIUM
			soundSystem = injector.get(SoundSystem::class)
			println("IngameScene.difficulty: $difficulty")
		}

		suspend override fun sceneInit(sceneView: Container) {
			sounds = (0..8).map { resourcesRoot["sounds/$it.mp3"].readSoundFile(soundSystem) }

			//val sv = views.scaleView(128, 72, 10.0)
			//sv += views.solidRect(72, 72, Colors.RED).apply { y = 10.0 }
			for (n in 0 until difficulty.items) {
				val image = views.image(atlas["$n.png"].texture).apply {
					//anchorX = 0.5
					anchorY = 0.5
					val w = (difficulty.items) * 20
					x = (128 / 2).toDouble() - (w / 2) + n * 20 - 4
					y = (72 / 2).toDouble()
				}
				images += image
				sceneView += image
			}
			//sceneView += sv

			go {
				sleep(1.seconds)
				ingame()
				sceneContainer.back()
			}
		}

		suspend fun ingame() {
			var turn = 1
			while (true) {
				cpuTurn(turn)
				if (!playerTurn(turn)) {
					println("FAILED!")
					val sound = failSound.play()
					//sound.await()
					sleep(0.5.seconds)
					break
				} else {
					println("SUCCESS START")
					val sound = successSound.play()
					//sound.await()
					sleep(0.5.seconds)
					println("SUCCESS END")
				}
				turn++
			}
		}

		suspend fun cpuTurn(turn: Int) {
			val seq = sequence.ensure(turn)
			for (item in seq) {
				highlight(item)
			}
		}

		suspend fun playerTurn(turn: Int): Boolean {
			val seq = sequence.ensure(turn)
			for (pos in 0 until seq.size) {
				val item = readOne()
				if (seq[pos] != item) return false
			}
			return true
		}

		private suspend fun readOne(): Int {
			return AutoClose { toclose ->
				val signal = Signal<Int>()
				for ((index, image) in images.withIndex()) {
					toclose += image.mouse.onUp {
						toclose.cancel()
						signal(index)
						go {
							highlight(index)
						}
					}
				}
				signal.waitOne()
			}
		}

		suspend fun highlight(index: Int) {
			println("PLAY START")
			val sound = soundSystem.play(sounds[index])
			println("PLAY PROGRESS")
			images[index].colorMul = Colors["#ff7f7f"]
			sleep(0.3.seconds)
			images[index].colorMul = Colors.WHITE
			//sound.await()
			sleep(0.5.seconds)
			println("PLAY END")
		}
	}
}
