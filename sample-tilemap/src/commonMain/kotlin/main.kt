import com.soywiz.klock.*
import com.soywiz.kmem.*
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.tiled.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge(width = 512, height = 512) {
	val tiledMap = resourcesVfs["gfx/sample.tmx"].readTiledMap()
	fixedSizeContainer(256, 256, clip = true) {
		position(128, 128)
		val camera = camera {
			tiledMapView(tiledMap) {
			}
		}
		var dx = 0.0
		var dy = 0.0
		//this.keys.apply {
		//	down { key ->
		//		when (key) {
		//			Key.RIGHT -> dx -= 1.0
		//			Key.LEFT -> dx += 1.0
		//			Key.DOWN -> dy -= 1.0
		//			Key.UP -> dy += 1.0
		//		}
		//	}
		//}
		addUpdater {
			if (views.input.keys[Key.RIGHT]) dx -= 1.0
			if (views.input.keys[Key.LEFT]) dx += 1.0
			if (views.input.keys[Key.UP]) dy += 1.0
			if (views.input.keys[Key.DOWN]) dy -= 1.0
			dx = dx.clamp(-10.0, +10.0)
			dy = dy.clamp(-10.0, +10.0)
			camera.x += dx
			camera.y += dy
			dx *= 0.9
			dy *= 0.9
		}
	}
}
