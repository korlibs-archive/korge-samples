import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

korge {
	id = "com.soywiz.samplevideo"
	name = "SampleVideo"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "MainKt"

	targetDefault()
}

dependencies {
	add("commonMainApi", "com.soywiz.korlibs.korvi:korvi:$korgeVersion")
}
