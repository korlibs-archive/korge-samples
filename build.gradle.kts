import com.soywiz.korge.gradle.*
import com.soywiz.korge.gradle.util.*

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

//apply(plugin = "kotlin-multiplatform")
apply(plugin = "korge")

//mainClassName = "Sample1Kt"

dependencies {
	add("commonMainApi", "com.soywiz:korma-shape-ops:$kormaVersion")
	add("commonMainApi", "com.soywiz:korma-triangulate-pathfind:$kormaVersion")
    add("commonMainApi", "com.soywiz:korge-dragonbones:$korgeVersion")
	add("commonMainApi", "com.soywiz:korge-box2d:$korgeVersion")
}

korge {
	id = "com.soywiz.sample1"
	name = "Sample1"
	description = "A sample using Korge and the gradle plugin"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	cordovaPlugin("cordova-launch-review")
	cordovaPlugin("cordova-plugin-camera")
	cordovaPlugin("cordova-plugin-admob-free", mapOf("ADMOB_APP_ID" to "ca-app-pub-xxxxxxxx~yyyyyy"))

	// This allows to support android emulator + android < 5.0 (lollipop)
	//cordovaUseCrosswalk()
}

val prepareKotlinNativeBootstrap = tasks.create("prepareKotlinNativeBootstrap") {
	group = "korge"
	val output = File(buildDir, "platforms/native-desktop/bootstrap.kt")
	outputs.file(output)
	doLast {
		output.apply {
			parentFile.mkdirs()
			val text = "fun main(args: Array<String>) = com.soywiz.korio.Korio { main() }"
			if (!exists() || readText() != text) writeText(text)
		}
	}
}

afterEvaluate {
	kotlin.macosX64 {
		compilations["main"].defaultSourceSet.kotlin.srcDir(File(buildDir, "platforms/native-desktop/bootstrap"))
		binaries {
			executable {
				linkTask.dependsOn(prepareKotlinNativeBootstrap)
				entryPoint = "sample.main"
			}
		}
	}
}

fun File.ensureParents() = this.apply { parentFile.mkdirs() }
fun <T> File.conditionally(ifNotExists: Boolean = true, block: File.() -> T): T? = if (!ifNotExists || !this.exists()) block() else null

val androidSdkPath by lazy {
	"${System.getProperty("user.home")}/Library/Android/sdk"
}

val resolvedArtifacts = LinkedHashMap<String, String>()

val coroutinesVersion = "1.1.1"
val androidPackageName = "com.example.myapplication"
val prepareAndroidBootstrap = tasks.create("prepareAndroidBootstrap") {
	group = "korge"
	var overwrite = true
	val outputFolder = File(buildDir, "platforms/android/MyApplication")
	doLast {
		val DOLLAR = "\\$"
		val ifNotExists = !overwrite
		//File(outputFolder, "build.gradle").conditionally(ifNotExists) {
		//	ensureParents().writeText("""
		//		// Top-level build file where you can add configuration options common to all sub-projects/modules.
		//		buildscript {
		//			repositories { google(); jcenter() }
		//			dependencies { classpath 'com.android.tools.build:gradle:3.3.0'; classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion" }
		//		}
		//		allprojects {
		//			repositories {
		//				mavenLocal(); maven { url = "https://dl.bintray.com/soywiz/soywiz" }; google(); jcenter()
		//			}
		//		}
		//		task clean(type: Delete) { delete rootProject.buildDir }
		//""".trimIndent())
		//}
		File(outputFolder, "local.properties").conditionally(ifNotExists) { ensureParents().writeText("sdk.dir=$androidSdkPath") }
		File(outputFolder, "settings.gradle").conditionally(ifNotExists) { ensureParents().writeText("") }
		File(outputFolder, "proguard-rules.pro").conditionally(ifNotExists) { ensureParents().writeText("#Rules here\n") }

		File(outputFolder, "build.gradle").conditionally(ifNotExists) {
			ensureParents().writeText(Indenter {
				line("buildscript") {
					line("repositories { google(); jcenter() }")
					line("dependencies { classpath 'com.android.tools.build:gradle:3.3.0'; classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion' }")
				}
				line("repositories") {
					line("mavenLocal()")
					line("maven { url = 'https://dl.bintray.com/soywiz/soywiz' }")
					line("google()")
					line("jcenter()")
				}

				line("apply plugin: 'com.android.application'")
				line("apply plugin: 'kotlin-android'")
				line("apply plugin: 'kotlin-android-extensions'")

				line("android") {
					line("compileSdkVersion 28")
					line("defaultConfig") {
						line("applicationId '$androidPackageName'")
						line("minSdkVersion 19")
						line("targetSdkVersion 28")
						line("versionCode 1")
						line("versionName '1.0'")
						line("testInstrumentationRunner 'android.support.test.runner.AndroidJUnitRunner'")
					}
					line("buildTypes") {
						line("debug") {
							line("minifyEnabled false")
						}
						line("release") {
							line("minifyEnabled false")
							line("proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'")
						}
					}
					line("sourceSets") {
						line("main") {
							line("java.srcDirs += ['../../../../src/commonMain/kotlin', '../../../../src/androidMain/kotlin']")
							line("assets.srcDirs += ['../../../../src/commonMain/resources', '../../../../src/androidMain/resources']")
						}
					}
				}

				line("dependencies") {
					line("implementation fileTree(dir: 'libs', include: ['*.jar'])")
					line("implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion'")

					line("api 'org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion'")
					for ((name, version) in resolvedArtifacts) {
						if (name.startsWith("org.jetbrains.kotlin")) continue
						line("api '$name-android:$version'")
					}

					line("implementation 'com.android.support:appcompat-v7:28.0.0'")
					line("implementation 'com.android.support.constraint:constraint-layout:1.1.3'")
					line("testImplementation 'junit:junit:4.12'")
					line("androidTestImplementation 'com.android.support.test:runner:1.0.2'")
					line("androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'")
				}
			}.toString())
		}

		File(outputFolder, "src/main/AndroidManifest.xml").conditionally(ifNotExists) {
			ensureParents().writeText("""
				<?xml version="1.0" encoding="utf-8"?>
				<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="$androidPackageName">
					<application
							android:allowBackup="true"
							android:icon="@android:drawable/sym_def_app_icon"
							android:label="My Awesome APP Name"
							android:roundIcon="@android:drawable/sym_def_app_icon"
							android:supportsRtl="true"
							android:theme="@android:style/Theme.Black.NoTitleBar.Fullscreen">
						<activity android:name=".MainActivity">
							<intent-filter>
								<action android:name="android.intent.action.MAIN"/>
								<category android:name="android.intent.category.LAUNCHER"/>
							</intent-filter>
						</activity>
					</application>
				</manifest>
			""".trimIndent())
		}

		File(outputFolder, "src/main/java/MainActivity.kt").conditionally(ifNotExists) {
			ensureParents().writeText("""
				package $androidPackageName

				import com.soywiz.klock.*
				import com.soywiz.korge.*
				import com.soywiz.korge.tween.*
				import com.soywiz.korge.view.*
				import com.soywiz.korgw.*
				import com.soywiz.korim.color.*
				import com.soywiz.korma.geom.*
				import kotlinx.coroutines.*
				import main

				class MainActivity : KorgwActivity() {
					override suspend fun activityMain() {
						main()
					}
				}
			""".trimIndent())
		}


		File(outputFolder, "gradle.properties").conditionally(ifNotExists) {
			ensureParents().writeText("org.gradle.jvmargs=-Xmx1536m")
		}
	}
}

// adb shell am start -n com.package.name/com.package.name.ActivityName
for (debug in listOf(false, true)) {
	val suffixDebug = if (debug) "Debug" else "Release"
	tasks.create<GradleBuild>("installAndroid$suffixDebug") {
		group = "korge"
		dependsOn(prepareAndroidBootstrap)
		buildFile = File(buildDir, "platforms/android/MyApplication/build.gradle")
		version = "4.10.1"
		setTasks(listOf("install$suffixDebug"))
	}

	for (emulator in listOf(null, false, true)) {
		val suffixDevice = when (emulator) {
			null -> ""
			false -> "Device"
			true -> "Emulator"
		}

		val extra = when (emulator) {
			null -> arrayOf()
			false -> arrayOf("-d")
			true -> arrayOf("-e")
		}

		tasks.create<Exec>("runAndroid$suffixDevice$suffixDebug") {
			group = "korge"
			dependsOn("installAndroid$suffixDebug")
			afterEvaluate {
				commandLine("$androidSdkPath/platform-tools/adb", *extra, "shell", "am", "start", "-n", "$androidPackageName/$androidPackageName.MainActivity")
			}
		}
	}
}



/*
afterEvaluate {

	for (config in configurations) {
		println(config.name)

		println(doResolve(config.allDependencies.toList()))
	}


	//kotlin.sourceSets.maybeCreate("commonMain").dependencies {  }
}
*/
configurations.all {
	resolutionStrategy.eachDependency {
		resolvedArtifacts["${requested.group}:${requested.name}".removeSuffix("-js").removeSuffix("-jvm")] = requested.version.toString()
		//println("details: ${this.requested.group}, ${this.requested.name}, ${this.requested.version}")
		//if (details.requested.group == 'org.gradle') {
		//	details.useVersion '1.4'
		//	details.because 'API breakage in higher versions'
		//}
	}
}


//fun doResolve(deps: Collection<ExternalModuleDependency>): Set<ResolvedDependency> {
//	println("deps: $deps")
//	val config = project.getConfigurations().detachedConfiguration();
//	val dependencySet = config.getDependencies();
//
//	for (spec in deps) {
//		val d = DefaultExternalModuleDependency(spec.group, spec.name, spec.version);
//		val da = DefaultDependencyArtifact(spec.name, "", "", null, null);
//		d.addArtifact(da);
//		//d.getExcludeRules().add(DefaultExcludeRule());
//		dependencySet.add(d);
//	}
//
//	return config.getResolvedConfiguration().getFirstLevelModuleDependencies();
//}

/*
korge {
    id = "com.soywiz.sample1"
    name = "Sample1"
    description = "A sample using Korge and the gradle plugin"
    orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
    cordovaPlugin("cordova-launch-review")
    cordovaPlugin("cordova-plugin-camera")
    cordovaPlugin("cordova-plugin-admob-free", ["ADMOB_APP_ID": "ca-app-pub-xxxxxxxx~yyyyyy"])

    // This allows to support android emulator + android < 5.0 (lollipop)
    cordovaUseCrosswalk()
}
*/

//components.main {
//	entryPoint = "bootstrap"
//}

//main.target('linux_x64').srcDirs += 'src/main/linux'

//println(kotlin.sourceSets.macosX64Main)
