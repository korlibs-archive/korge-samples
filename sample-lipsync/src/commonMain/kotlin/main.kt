import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.atlas.*
import com.soywiz.korge.lipsync.*
import com.soywiz.korge.view.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge {
	val atlas = resourcesVfs["lips.atlas.json"].readAtlas()
	val lips = image(atlas["lisa-A.png"])
	val lips2 = image(atlas["lisa-A.png"]).position(400, 0)
	addEventListener<LipSyncEvent> {
		println(it)
		if (it.name == "lisa") {
			lips2.texture = atlas["lisa-${it.lip}.png"]
		}
	}
	launchImmediately {
		fun handler(it: LipSyncEvent) {
			views.dispatch(it)
			lips.texture = atlas["lisa-${it.lip}.png"]
		}

		resourcesVfs["001.voice.wav"].readVoice().play("lisa") { handler(it) }
		//resourcesVfs["002.voice.wav"].readVoice().play("lisa") { handler(it) }
		//resourcesVfs["003.voice.wav"].readVoice().play("lisa") { handler(it) }
		//resourcesVfs["004.voice.wav"].readVoice().play("lisa") { handler(it) }
		//resourcesVfs["simple.voice.mp3"].readVoice().play("lisa") { handler(it) }
	}
}
