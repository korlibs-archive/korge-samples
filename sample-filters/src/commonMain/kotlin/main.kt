import com.soywiz.klock.seconds
import com.soywiz.korge.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.filter.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.*
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.degrees
import com.soywiz.korma.interpolation.Easing

suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
	val minDegrees = (-16).degrees
	val maxDegrees = (+16).degrees

	val wave = WaveFilter()
	val image = image(resourcesVfs["korge.png"].readBitmap()) {
		rotation = maxDegrees
		anchor(.5, .5)
		scale(.8)
		position(256, 256)
		filter = wave
	}

	launchImmediately {
		while (true) {
			image.tween(image::rotation[minDegrees], wave::time[1.0], time = 1.seconds, easing = Easing.EASE_IN_OUT)
			image.tween(image::rotation[maxDegrees], wave::time[0.0], time = 1.seconds, easing = Easing.EASE_IN_OUT)
		}
	}
}
