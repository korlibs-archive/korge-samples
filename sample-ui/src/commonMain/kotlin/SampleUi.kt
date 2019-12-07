import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.newui.*
import com.soywiz.korge.view.*
import com.soywiz.korgw.*

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
	}
}
