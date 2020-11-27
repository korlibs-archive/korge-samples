import com.soywiz.korge.*
import com.soywiz.korge.view.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge {
	val text1 = text("hello.demo".resource.readString())
	val text2 = text("hello.demo.uppercased".resource.readString()).alignTopToBottomOf(text1)
}

private val String.resource get() = resourcesVfs[this]
