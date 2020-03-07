import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.sample1"
	name = "Sample1"
	description = "A sample using Korge and the gradle plugin"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "Sample1Kt"

	admob("ca-app-pub-xxxxxxxx~yyyyyy")

	supportShapeOps()
	supportTriangulation()
	supportDragonbones()
	supportBox2d()
}
