import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.korlibs.korge.samples.dragonbones"
	name = "KorGE - DragonBones"
	description = "KorGE sample using DragonBones plugin"
	supportDragonbones()
	icon = file("src/commonMain/resources/icon.png")
}
