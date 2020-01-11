import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.tiled.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge(width = 512, height = 512) {
	val tiledMap = resourcesVfs["gfx/sample.tmx"].readTiledMap()
	fixedSizeContainer(256, 256, clip = true) {
		position(128, 128)
		val camera = camera {
			tiledMapView(tiledMap) {
			}
		}
		keys {
			down {
				launchImmediately {
					when (key) {
						Key.RIGHT -> camera.moveBy(-16, 0, 0.25.seconds)
						Key.LEFT -> camera.moveBy(+16, 0, 0.25.seconds)
						Key.DOWN -> camera.moveBy(0, -16, 0.25.seconds)
						Key.UP -> camera.moveBy(0, +16, 0.25.seconds)
					}
				}
			}
		}
	}
}
