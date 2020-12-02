import com.soywiz.korge.*
import com.soywiz.korge.scene.*
import com.soywiz.korim.color.*
import com.soywiz.korinject.*
import com.soywiz.korma.geom.*
import scene.*

suspend fun main() = Korge(Korge.Config(module = TicTacToeModule))

object TicTacToeModule : Module() {
	override val mainScene = InGameScene::class
	override val bgcolor = Colors["#2b2b2b"]
	override val size = SizeInt(640, 480)

	override suspend fun AsyncInjector.configure() {
		mapPrototype { InGameScene() }
	}
}

