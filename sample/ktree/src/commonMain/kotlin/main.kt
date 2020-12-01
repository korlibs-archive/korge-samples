import com.soywiz.korge.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.ktree.*
import com.soywiz.korim.color.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge(bgcolor = Colors["#172335"]) {
	addChild(resourcesVfs["scene.ktree"].readKTree(views()))
}
