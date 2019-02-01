import com.soywiz.korge.gradle.*

buildscript {
	repositories {
		mavenLocal()
		maven { url = uri("https://dl.bintray.com/soywiz/soywiz") }
		maven { url = uri("https://plugins.gradle.org/m2/") }
		mavenCentral()
	}
	dependencies {
		classpath("com.soywiz:korge-gradle-plugin:1.0.2")
	}
}

plugins {
	id("kotlin-multiplatform").version("1.3.20")
}

apply(plugin = "kotlin-multiplatform")
apply(plugin = "korge")

dependencies {
	add("commonMainApi", "com.soywiz:korma-shape-ops:$kormaVersion")
	add("commonMainApi", "com.soywiz:korma-triangulate-pathfind:$kormaVersion")
	add("commonMainApi", "com.soywiz:korge-dragonbones:$korgeVersion")
	add("commonMainApi", "com.soywiz:korge-box2d:$korgeVersion")
}

val entryPoint = "main"

korge {
	id = "com.soywiz.sample1"
	name = "Sample1"
	description = "A sample using Korge and the gradle plugin"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "Sample1Kt"

	admob("ca-app-pub-xxxxxxxx~yyyyyy")
}
