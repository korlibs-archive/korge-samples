import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

korge {
	supportDragonbones()

	id = "com.soywiz.korlibs.korge.samples.dragonbones"
	name = "KorGE - DragonBones"
	description = "KorGE sample using DragonBones plugin"
	orientation = Orientation.LANDSCAPE
	icon = file("src/commonMain/resources/icon.png")

	targetDefault()
}
