pluginManagement {
	resolutionStrategy {
		eachPlugin {
			if (requested.id.id == "kotlin-multiplatform") {
				useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
			}
		}
	}

	repositories {
		mavenLocal()
		mavenCentral()
		maven { url = uri("https://plugins.gradle.org/m2/") }
	}
}

enableFeaturePreview("GRADLE_METADATA")

include(":sample-box2d")
include(":sample-scenes")
include(":sample-tic-tac-toe-swf")
include(":sample-bitmap-font")
