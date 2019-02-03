import com.soywiz.korge.gradle.*
import com.soywiz.korge.gradle.util.*

apply(plugin = "korge")

dependencies {
	add("commonMainApi", "com.soywiz:korge-swf:$korgeVersion")
}

korge {
	id = "com.soywiz.samples.tictactoe"
	name = "tic-tac-toe"
	icon = projectDir["src/commonMain/resources/icon.png"]
}
