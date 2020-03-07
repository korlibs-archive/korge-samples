import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.samples.luak"
	dependencyMulti("com.soywiz.korlibs.luak:luak:0.2.0")
}
