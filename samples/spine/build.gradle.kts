import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.sampleSpriteAnimation"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE

	targetDefault()
	supportSpine()
}
