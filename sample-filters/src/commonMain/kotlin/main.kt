import com.soywiz.korge.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korim.font.*
import com.soywiz.korim.vector.*
import com.soywiz.korim.vector.paint.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
	//val wave = WaveFilter()
	val font = resourcesVfs["myfont-bug.ttf"].readTtfFont()
	val bitmap = NativeImage(512, 128).context2d {
		this.font = font
		this.fontSize = 32.0
		this.fillStyle = ColorPaint(Colors.RED)
		this.verticalAlign = VerticalAlign.TOP
		fillText("HELLO WORLD", x = 0.0, y = 0.0)
	}

	image(bitmap)

	/*
	while (true) {
		image.tween(image::rotation[minDegrees], wave::time[1.seconds], time = 1.seconds, easing = Easing.EASE_IN_OUT)
		image.tween(image::rotation[maxDegrees], wave::time[0.seconds], time = 1.seconds, easing = Easing.EASE_IN_OUT)
	}
	 */
}
