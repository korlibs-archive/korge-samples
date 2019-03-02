import com.soywiz.korge.*
import com.soywiz.korge.particle.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.file.std.*
import com.soywiz.korio.util.*

suspend fun main() = Korge(bgcolor = Colors.DARKBLUE) {
	val text1 = text("-").position(0, 0).also { it.filtering = false }
	val text2 = text("-").position(0, 15).also { it.filtering = false }

	addTouchGamepad(
		views.virtualWidth.toDouble(), views.virtualHeight.toDouble(),
		onStick = { x, y -> text1.setText("Stick: (${x.toStringDecimal(2)}, ${y.toStringDecimal(2)})") },
		onButton = { button, pressed -> text2.setText("Button: $button, $pressed") }
	)
}
