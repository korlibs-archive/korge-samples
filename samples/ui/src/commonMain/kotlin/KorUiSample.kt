import com.soywiz.korge.Korge
import com.soywiz.korge.ui.*
import com.soywiz.korge.ui.korui.*
import com.soywiz.korge.view.fixedSizeContainer
import com.soywiz.korgw.GameWindow
import com.soywiz.korui.button
import com.soywiz.korui.checkBox
import com.soywiz.korui.comboBox
import com.soywiz.korui.layout.horizontal
import com.soywiz.korui.layout.preferredHeight
import com.soywiz.korui.layout.preferredWidth
import com.soywiz.korui.layout.vertical

/**
 * Uses the [korUI](https://korlibs.soywiz.com/old/korui/) to build "React like" applications with a Kotlin DSL.
 */
class KorUiSample {

	suspend fun main() = Korge(quality = GameWindow.Quality.PERFORMANCE, title = "UI", width = 800, height = 800) {
		val container = fixedSizeContainer(width, height, clip = true) { }
		container.korui {
			//addChild(UiEditProperties(app, container, views)) // Broken

			vertical {
				horizontal {
					preferredWidth = 100.percent
					//minimumWidth = 100.percent
					button("HELLO", {
						//minimumWidth = 50.percent
						preferredWidth = 70.percent
						//preferredHeight = 32.pt
					})
					button("WORLD", {
						preferredWidth = 30.percent
						preferredHeight = 32.pt
					})
				}
				button("DEMO").apply {
					visible = false
				}
				button("TEST")
				checkBox("CheckBox", checked = true)
				comboBox("test", listOf("test", "demo"))
			}

		}
	}
}
