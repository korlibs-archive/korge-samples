package com.soywiz.korge.samples.simon

import com.soywiz.korge.Korge
import com.soywiz.korge.atlas.Atlas
import com.soywiz.korge.audio.SoundFile
import com.soywiz.korge.audio.SoundSystem
import com.soywiz.korge.audio.readSoundFile
import com.soywiz.korge.html.Html
import com.soywiz.korge.input.mouse
import com.soywiz.korge.resources.Path
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.ScaledScene
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.scene.sleep
import com.soywiz.korge.time.seconds
import com.soywiz.korge.ui.UIFactory
import com.soywiz.korge.ui.korui.koruiFrame
import com.soywiz.korge.util.AutoClose
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korio.async.Signal
import com.soywiz.korio.async.go
import com.soywiz.korio.async.waitOne
import com.soywiz.korio.inject.Optional
import com.soywiz.korio.util.substr
import com.soywiz.korma.geom.ISize
import com.soywiz.korma.random.get
import com.soywiz.korui.geom.len.Padding
import com.soywiz.korui.geom.len.em
import com.soywiz.korui.style.padding
import com.soywiz.korui.ui.button
import com.soywiz.korui.ui.click
import com.soywiz.korui.ui.horizontal
import java.util.*

object Simon : Module() {
	@JvmStatic fun main(args: Array<String>) = Korge(this)

	override val virtualWidth: Int = 1280
	override val virtualHeight: Int = 720

	override val width: Int = (virtualWidth * 0.75).toInt()
	override val height: Int = (virtualHeight * 0.75).toInt()

	override val title: String = "Kotlin Simon"

	override val icon: String = "kotlin/0.png"
	//override val mainScene: Class<out Scene> = MainScene::class.java
	override val mainScene: Class<out Scene> = SelectLevelScene::class.java


	class Sequence(
		val max: Int,
		val random: Random = Random()
	) {
		val items = arrayListOf<Int>()

		fun ensure(num: Int): List<Int> {
			while (items.size < num) items += random[0, max]
			return items
		}
	}

	enum class Difficulty(val items: Int) {
		EASY(3), MEDIUM(4), HARD(6)
	}

	class SelectLevelScene(
		@Path("kotlin.atlas") val atlas: Atlas,
		val ui: UIFactory
	) : Scene() {
		suspend override fun sceneInit(sceneView: Container) {
			sceneView += ui.koruiFrame {
				horizontal {
					padding = Padding(0.2.em)
					button("EASY").click { sceneContainer.pushTo<IngameScene>(Difficulty.EASY) }
					button("MEDIUM").click { sceneContainer.pushTo<IngameScene>(Difficulty.MEDIUM) }
					button("HARD").click { sceneContainer.pushTo<IngameScene>(Difficulty.HARD) }
				}
			}
		}
	}

	class IngameScene(
		@Path("kotlin.atlas") val atlas: Atlas,
		@Path("sounds/success.wav") val successSound: SoundFile,
		@Path("sounds/fail.mp3") val failSound: SoundFile,
		@Optional val optDifficulty: Difficulty?,
		val soundSystem: SoundSystem
	) : ScaledScene() {
		val difficulty = optDifficulty ?: Difficulty.MEDIUM
		override val sceneSize = ISize(128, 72)
		override val sceneScale: Double = 10.0

		val sequence = Sequence(difficulty.items)
		val images = arrayListOf<Image>()
		lateinit var sounds: List<SoundFile>

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
					failSound.play().await()
					break
				} else {
					println("SUCCESS START")
					successSound.play().await()
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
			val sound = soundSystem.play(sounds[index])
			images[index].colorMul = Colors["#ff7f7f"]
			sleep(0.3.seconds)
			images[index].colorMul = Colors.WHITE
			sound.await()
		}
	}
}
