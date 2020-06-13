import com.soywiz.korge.gradle.*

buildscript {
	val korgeVersion: String by project

	repositories {
		mavenLocal()
		maven { url = uri("https://dl.bintray.com/korlibs/korlibs") }
		maven { url = uri("https://plugins.gradle.org/m2/") }
		mavenCentral()
	}
	dependencies {
		classpath("com.soywiz.korlibs.korge.plugins:korge-gradle-plugin:1.12.8.0")
	}
}

apply<KorgeGradlePlugin>()

korge {
	id = "com.example.example"
}