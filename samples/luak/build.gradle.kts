import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

korge {
	id = "com.soywiz.samples.luak"
	dependencyMulti("com.soywiz.korlibs.luak:luak:2.0.7", registerPlugin = false)
	targetDefault()
}
