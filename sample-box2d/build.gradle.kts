import com.soywiz.korge.gradle.*

apply(plugin = "korge")

korge {
	id = "com.soywiz.sample1"
	name = "Sample1"
	description = "A sample using Korge and the gradle plugin"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "Sample1Kt"

	admob("ca-app-pub-xxxxxxxx~yyyyyy")

	dependencyMulti("com.soywiz:korma-shape-ops:$kormaVersion")
	dependencyMulti("com.soywiz:korma-triangulate-pathfind:$kormaVersion")
	dependencyMulti("com.soywiz:korge-dragonbones:$korgeVersion")
	dependencyMulti("com.soywiz:korge-box2d:$korgeVersion")
}
