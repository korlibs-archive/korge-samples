import com.soywiz.korge.*
import com.soywiz.korge.atlas.*
import com.soywiz.korge.view.*
import com.soywiz.korio.*
import com.soywiz.korio.dynamic.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge(width = 640, height = 480, virtualWidth = 320, virtualHeight = 240) {
	val logos = resourcesVfs["logos.atlas.json"].readAtlas(views)
	image(logos["korau.png"].texture).position(0, 0)
	image(logos["korim.png"].texture).position(64, 32)
	image(logos["korge.png"].texture).position(128, 64)
}
