import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

korge {
	id = "com.soywiz.sampleSpriteAnimation"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE

	targetDefault()
	supportSpine()
}
