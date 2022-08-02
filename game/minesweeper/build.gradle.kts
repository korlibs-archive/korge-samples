import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

// Ported from here: https://github.com/soywiz/lunea/tree/master/samples/busca

//apply<KorgeGradlePlugin>()

korge {
	entryPoint = "com.soywiz.korge.samples.minesweeper.main"
	jvmMainClassName = "com.soywiz.korge.samples.minesweeper.MainKt"
	id = "com.soywiz.samples.minesweeper"
	targetDefault()
}
