import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.korge.samples.bunnymarkfast"
	description = "A sample using FSprites in KorGE"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	targetDefault()
}
