import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.particle.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*
import com.soywiz.korio.util.*

suspend fun main() = Korge(bgcolor = Colors.DARKBLUE) {
	val text1 = text("-").position(0, 0).also { it.filtering = false }
	val buttonTexts = (0 until 2).map {
		text("-").position(0, 15 * (it + 1)).also { it.filtering = false }
	}

	addTouchGamepad(
		views.virtualWidth.toDouble(), views.virtualHeight.toDouble(),
		onStick = { x, y -> text1.setText("Stick: (${x.toStringDecimal(2)}, ${y.toStringDecimal(2)})") },
		onButton = { button, pressed -> buttonTexts[button].setText("Button: $button, $pressed") }
	)
}
