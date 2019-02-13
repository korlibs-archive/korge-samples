import com.soywiz.korge.gradle.*

apply(plugin = "korge")

korge {
	id = "com.soywiz.samples.dragonbones"
	dependencyMulti("com.soywiz:korge-dragonbones:$korgeVersion")
}
