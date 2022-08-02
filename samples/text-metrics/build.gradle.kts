import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

korge {
	id = "com.soywiz.samples.textmetrics"
	name = "Text2"
	description = "Shows show text metrics"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "MainKt"

	targetDefault()
}
