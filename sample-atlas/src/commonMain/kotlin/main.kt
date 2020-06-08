import com.soywiz.korge.Korge
import com.soywiz.korge.atlas.readAtlas
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.image
import com.soywiz.korge.view.position
import com.soywiz.korio.file.std.resourcesVfs

suspend fun main() = Korge(width = 640, height = 480, virtualWidth = 320, virtualHeight = 240) {
	atlasMain()
}

suspend fun Stage.atlasMain() {
	val logos = resourcesVfs["logos.atlas.json"].readAtlas()
	image(logos["korau.png"]).position(0, 0)
	image(logos["korim.png"]).position(64, 32)
	image(logos["korge.png"]).position(128, 64)
}
