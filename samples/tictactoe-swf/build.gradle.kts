import com.soywiz.korge.gradle.util.*
import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

korge {
	id = "com.soywiz.samples.tictactoe"
	name = "tic-tac-toe"
	icon = projectDir["src/commonMain/resources/icon.png"]

	//dependencyMulti("com.soywiz:korge-swf:$korgeVersion")
	supportSwf()

	targetDefault()
}
