import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

korge {
	id = "com.soywiz.sample1"
	name = "Box2D-Sample"
	description = "A sample using Korge and the gradle plugin"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE

	//admob("ca-app-pub-xxxxxxxx~yyyyyy")

	supportBox2d()
	targetDefault()
}
