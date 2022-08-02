import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.time.*
import com.soywiz.korge.view.tiles.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.format.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge(width = 512, height = 512) {
	val tileset = TileSet(bitmap("korge.png").toBMP32().scaleLinear(0.5, 0.5).slice(), 256, 256)
	val tilemap = tileMap(Bitmap32(1, 1), repeatX = TileMapRepeat.REPEAT, repeatY = TileMapRepeat.REPEAT, tileset = tileset)
	launchImmediately {
		frameBlock(144.timesPerSecond) {
			while (true) {
				tilemap.x += 1
				tilemap.y += 0.25
				frame()
			}
		}
	}
}

suspend fun bitmap(path: String) = resourcesVfs[path].readBitmap()
