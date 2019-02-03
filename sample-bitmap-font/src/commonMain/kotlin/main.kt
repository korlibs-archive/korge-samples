import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.filter.*
import com.soywiz.korim.color.*
import com.soywiz.korim.font.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge(bgcolor = Colors["#333"]) {
	val font1 = resourcesVfs["font1.fnt"].readBitmapFont()
	text("Hello World!", textSize = 128.0, font = font1)
	text("Hello World 2!", textSize = 32.0, font = font1) {
		val text = this
		fun center() {
			position(views.virtualWidth / 2 - width / 2, views.virtualHeight / 2)
		}
		//filter = Convolute3Filter(Convolute3Filter.KERNEL_GAUSSIAN_BLUR)
		center()
		launchImmediately {
			var n = 0
			while (true) {
				text.text = "Hello World! ${n++}"
				center()
				delay(1.milliseconds)
			}
		}
	}
}
