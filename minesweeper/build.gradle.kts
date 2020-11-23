import com.soywiz.korge.gradle.*

// Ported from here: https://github.com/soywiz/lunea/tree/master/samples/busca
apply(plugin = "korge")

korge {
	entryPoint = "com.soywiz.korge.samples.minesweeper.main"
	jvmMainClassName = "com.soywiz.korge.samples.minesweeper.MainKt"
	id = "com.soywiz.samples.minesweeper"
	targetDefault()
}
