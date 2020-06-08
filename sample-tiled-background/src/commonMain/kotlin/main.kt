import com.soywiz.klock.timesPerSecond
import com.soywiz.korge.*
import com.soywiz.korge.time.*
import com.soywiz.korge.view.addFixedUpdater
import com.soywiz.korge.view.tiles.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.format.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge(width = 512, height = 512) {
	val tileset = TileSet(mapOf(0 to bitmap("korge.png").toBMP32().scaleLinear(0.5, 0.5).slice()))
	val tilemap = tileMap(Bitmap32(1, 1), repeatX = TileMap.Repeat.REPEAT, repeatY = TileMap.Repeat.REPEAT, tileset = tileset)
	addFixedUpdater(60.timesPerSecond) {
		tilemap.x += 1
		tilemap.y += 0.25
	}
}

suspend fun bitmap(path: String) = resourcesVfs[path].readBitmap()
