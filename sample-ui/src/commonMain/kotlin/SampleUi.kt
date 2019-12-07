import com.soywiz.korge.*
import com.soywiz.korgw.*
import kotlin.jvm.*

suspend fun main() = Korge(quality = GameWindow.Quality.PERFORMANCE, title = "UI") {
	addChild(UIButton(256.0, 32.0).apply {
		x = 64.0
		y = 64.0
	})
}
