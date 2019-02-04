import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.view.*

suspend fun main() = Korge {
	val text = text("Key:")

	keys {
		onKeyDown { text.text = "Key:Down:${it.key}" }
		onKeyUp { text.text = "Key:Up:${it.key}" }
	}
}
