import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

korge {
	id = "com.soywiz.korge.samples.bunnymark"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	targetDefault()
}
