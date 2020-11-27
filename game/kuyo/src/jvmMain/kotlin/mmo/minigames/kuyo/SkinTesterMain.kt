package mmo.minigames.kuyo

import com.soywiz.korge.*
import com.soywiz.korge.scene.*
import com.soywiz.korinject.*
import kotlinx.coroutines.*
import org.korge.sample.kuyo.*
import kotlin.reflect.*

object SkinTester {
	@JvmStatic
	fun main(args: Array<String>) {
		runBlocking {
			Korge(Korge.Config(object : Module() {
				override val mainScene: KClass<out Scene> = TestPuyoScene::class

				override suspend fun AsyncInjector.configure() {
					mapPrototype { TestPuyoScene() }
				}
			}))
		}
	}
}
