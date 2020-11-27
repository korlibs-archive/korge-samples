package org.korge.sample.kuyo

/*
import com.soywiz.korge.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.format.*
import com.soywiz.korinject.*
import com.soywiz.korma.geom.*
import kotlin.jvm.*
import kotlin.reflect.*

object Kuyo {
    //val WINDOW_CONFIG = WindowConfig(
    //    width = (1280 * 0.7).toInt(),
    //    height = (720 * 0.7).toInt(),
    //    title = "Kuyo!"
    //)
//
    @JvmStatic
    suspend fun main(args: Array<String>) {
        Korge(Korge.Config(object : Module() {
            override val mainScene: KClass<out Scene> = KuyoScene::class

			override suspend fun AsyncInjector.configure() {
				mapPrototype { KuyoScene() }
			}
        }))
    }
//
    //object MainMenu {
    //    @JvmStatic
    //    fun main(args: Array<String>) {
    //        SceneApplication(WINDOW_CONFIG) { MainMenuScene() }
    //    }
    //}

    object SkinTester {
        @JvmStatic
        suspend fun main(args: Array<String>) {
            Korge(Korge.Config(object : Module() {
                override val mainScene: KClass<out Scene> = TestPuyoScene::class

				override suspend fun AsyncInjector.configure() {
                    mapPrototype { TestPuyoScene() }
                }
            }))
        }
    }
}

class TestPuyoScene : Scene() {
    override suspend fun Container.sceneInit() {
        //val tiles = KuyoTiles(resourcesRoot["kuyo/kuyos.png"].readBitmapNoNative(defaultImageFormats).slice())
        val tiles = KuyoTiles(resourcesRoot["kuyo/kuyos.png"].readBitmapSlice())
        val board = board(
            "1.11.111...........",
            ".............1.....",
            "1.11.11.11..111....",
            "1.1...1.11...1.....",
            "...................",
            "1.1...1.111.111....",
            "1.11.11.1.1.111....",
            "1.......111.111...."
        )
        for (y in 0 until board.height) {
            for (x in 0 until board.width) {
                val c = board[PointInt(x, y)]
                val up = board[PointInt(x, y - 1)] == c
                val down = board[PointInt(x, y + 1)] == c
                val left = board[PointInt(x - 1, y)] == c
                val right = board[PointInt(x + 1, y)] == c
                if (c != 0) {
                    Image(tiles.getTex(c, up, left, right, down)).also {
                        it.position(x * 32.0, y * 32.0)
                        root += it
                    }
                }
            }
        }
    }
}
*/
