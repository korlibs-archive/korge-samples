import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.newui.*
import com.soywiz.korge.view.*
import com.soywiz.korgw.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*

suspend fun main() = Korge(quality = GameWindow.Quality.PERFORMANCE, title = "UI") {
	uiSkin(OtherUISkin) {
		uiButton(256.0, 32.0) {
			label = "Disabled Button"
			position(128, 128)
			onClick {
				println("CLICKED!")
			}
			disable()
		}
		uiButton(256.0, 32.0) {
			label = "Enabled Button"
			position(128, 128 + 32)
			onClick {
				println("CLICKED!")
			}
			enable()
		}
		uiScrollBar(256.0, 32.0, 0.0, 32.0, 64.0) {
			position(64, 64)
			onChange {
				println(it.ratio)
			}
		}
		uiScrollBar(32.0, 256.0, 0.0, 16.0, 64.0) {
			position(64, 128)
			onChange {
				println(it.ratio)
			}
		}

		uiCheckBox {
			position(128, 128 + 64)
		}

		uiScrollableArea(config = {
			position(480, 128)
		}) {

			for (n in 0 until 16) {
				uiButton(label = "HELLO $n").position(0, n * 64)
			}
		}
	}
}

private val otherColorTransform = ColorTransform(0.7, 0.9, 1.0)
private val OTHER_UI_SKIN_IMG by lazy {
	DEFAULT_UI_SKIN_IMG.withColorTransform(otherColorTransform)
}

object OtherUISkin : UISkin(
	normal = OTHER_UI_SKIN_IMG.sliceWithSize(0, 0, 64, 64),
	hover = OTHER_UI_SKIN_IMG.sliceWithSize(64, 0, 64, 64),
	down = OTHER_UI_SKIN_IMG.sliceWithSize(127, 0, 64, 64),
	backColor = DefaultUISkin.backColor.transform(otherColorTransform)
)
