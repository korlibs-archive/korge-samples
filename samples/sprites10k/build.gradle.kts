import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

korge {
	id = "com.soywiz.spine"
	name = "SpineSample"
	description = "A sample using Korge and the gradle plugin"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE

	targetDefault()
}
