import com.soywiz.kds.*
import com.soywiz.klock.*
import com.soywiz.korau.sound.*
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.component.*
import com.soywiz.korge.input.*
import com.soywiz.korge.input.mouse
import com.soywiz.korge.time.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.*
import com.soywiz.korio.file.std.*
import com.soywiz.korio.lang.*
import com.soywiz.korio.stream.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import com.soywiz.korvi.*
import kotlin.coroutines.*

suspend fun main() = Korge(width = 1280, height = 720, bgcolor = Colors["#2b2b2b"]) {
	val view = korviView(views, resourcesVfs["video.mp4"])
}

inline fun Container.korviView(views: Views, video: KorviVideo, callback: KorviView.() -> Unit = {}): KorviView = KorviView(views, video).also { addChild(it) }.also { callback(it) }
suspend inline fun Container.korviView(views: Views, video: VfsFile, callback: KorviView.() -> Unit = {}): KorviView = KorviView(views, video).also { addChild(it) }.also { callback(it) }
class KorviView(val views: Views, val video: KorviVideo, val stream: AsyncStream? = null) : Container(), AsyncCloseable, BaseKorviSeekable by video {
	val onCompleted = Signal<Unit>()
	var autoLoop = true

	companion object {
		operator suspend fun invoke(views: Views, file: VfsFile): KorviView {
			val stream = file.open()
			return KorviView(views, KorviVideo(stream), stream)
		}
	}

	var running = true
	val img = image(Bitmaps.transparent)
	val audio = nativeSoundProvider.createAudioStream()
	var elapsedTime: TimeSpan = 0.seconds

	private var videoLastFrame: KorviVideoFrame? = null
	private val videoJob = views.launchImmediately {
		while (true) {
			delayFrame()
			elapsedTime += 16.milliseconds
			if (videoLastFrame == null) {
				videoLastFrame = video.video.first().readFrame()
				if (videoLastFrame == null) {
					completed()
				}
			}
			if (videoLastFrame != null && elapsedTime >= videoLastFrame!!.position) {
				img.bitmap = videoLastFrame!!.data.slice()
				videoLastFrame = null
			}
		}
	}
	private val audioJob = views.launchImmediately {
		while (true) {
			delayFrame()
			for (n in 0 until 3) {
				val frame = video.audio.first().readFrame()
				if (frame != null) {
					audio.add(frame!!.data)
				}
			}
		}
	}

	private suspend fun completed() {
		running = false
		onCompleted(Unit)
		if (autoLoop) {
			seek(0L)
		}
	}

	override suspend fun seek(time: TimeSpan) {
		video.seek(time)
		elapsedTime = time
		running = true
	}

	override suspend fun seek(frame: Long) {
		video.seek(frame)
		running = true
		//elapsedTime = null
	}

	// @TODO: Autoclose
	override suspend fun close() {
		audio.stop()
		video.close()
		stream?.close()
		videoJob.cancel()
		audioJob.cancel()
	}
}
