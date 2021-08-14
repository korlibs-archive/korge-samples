import com.soywiz.korge.gradle.KorgeGradlePlugin
import com.soywiz.korge.gradle.korge

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.sample1"
	name = "Box2D-Sample"
	description = "A sample using Korge and the gradle plugin"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE

	//admob("ca-app-pub-xxxxxxxx~yyyyyy")

	supportBox2d()
	targetDefault()
}
