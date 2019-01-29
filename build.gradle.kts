import com.soywiz.korge.gradle.*
import com.soywiz.korge.gradle.util.*
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeOutputKind

buildscript {
    repositories {
        mavenLocal()
        maven { url = uri("https://dl.bintray.com/soywiz/soywiz") }
		maven { url = uri("https://plugins.gradle.org/m2/") }
		mavenCentral()
    }
    dependencies {
        classpath("com.soywiz:korge-gradle-plugin:1.0.0")
    }
}

plugins {
	id("kotlin-multiplatform").version("1.3.20")
}

apply(plugin = "kotlin-multiplatform")
apply(plugin = "korge")

//mainClassName = "Sample1Kt"

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
	cordovaPlugin("cordova-launch-review")
	cordovaPlugin("cordova-plugin-camera")
	cordovaPlugin("cordova-plugin-admob-free", mapOf("ADMOB_APP_ID" to "ca-app-pub-xxxxxxxx~yyyyyy"))

	// This allows to support cordova android emulator + android < 5.0 (lollipop)
	//cordovaUseCrosswalk()
}

tasks.create("prepareKotlinNativeBootstrapIos") {
	doLast {
		File(buildDir, "platforms/native-ios/info.kt").apply {
			parentFile.mkdirs()
			writeText("""
				object MyIosGameWindow2 {
					val gameWindow get() = com.soywiz.korgw.MyIosGameWindow
				}
			""".trimIndent())
		}
	}
}

kotlin.apply {
	//for (target in listOf(iosX64(), iosArm64(), iosArm64())) {
	for (target in listOf(iosX64())) {
		target.apply {
			compilations["main"].apply {
				//for (type in listOf(NativeBuildType.DEBUG, NativeBuildType.RELEASE)) {
				//	//getLinkTask(NativeOutputKind.FRAMEWORK, type).embedBitcode = Framework.BitcodeEmbeddingMode.DISABLE
				//}
				outputKind(NativeOutputKind.FRAMEWORK)
				afterEvaluate {
					binaries {
						for (binary in this) {
							if (binary is Framework) {
								binary.baseName = "GameMain"
								binary.embedBitcode = Framework.BitcodeEmbeddingMode.DISABLE
							}
						}
					}
					for (type in listOf(NativeBuildType.DEBUG, NativeBuildType.RELEASE)) {
						getLinkTask(NativeOutputKind.FRAMEWORK, type).dependsOn("prepareKotlinNativeBootstrap")
						getLinkTask(NativeOutputKind.FRAMEWORK, type).dependsOn("prepareKotlinNativeBootstrapIos")
					}
				}

				defaultSourceSet.kotlin.srcDir(File(buildDir, "platforms/native-desktop"))
				defaultSourceSet.kotlin.srcDir(File(buildDir, "platforms/native-ios"))
			}


			//compilations["main"].bitcode
		}
	}
}
