import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

korge {
	id = "com.soywiz.samples.text"
	name = "Text"
	description = "Shows show to use Text"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "MainKt"

	targetDefault()
}
