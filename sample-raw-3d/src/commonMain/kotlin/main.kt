import com.soywiz.kds.*
import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.experimental.s3d.*
import com.soywiz.korge.experimental.s3d.model.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import kotlin.jvm.*

suspend fun main(args: Array<String>) = Demo3.main(args)

object Demo1 {
	@JvmStatic
	suspend fun main(args: Array<String>) = Korge(title = "KorGE 3D") {
		image(resourcesVfs["korge.png"].readNativeImage()).alpha(0.5)

		scene3D {
			//camera.set(fov = 60.degrees, near = 0.3, far = 1000.0)

			val cube1 = box()
			val cube2 = box().position(0, 2, 0).scale(1, 2, 1).rotation(0.degrees, 0.degrees, 45.degrees)
			val cube3 = box().position(-5, 0, 0)
			val cube4 = box().position(+5, 0, 0)
			val cube5 = box().position(0, -5, 0)
			val cube6 = box().position(0, +5, 0)
			val cube7 = box().position(0, 0, -5)
			val cube8 = box().position(0, 0, +5)

			var tick = 0
			addUpdatable {
				val angle = (tick / 4.0).degrees
				camera.positionLookingAt(
					cos(angle * 2) * 4, cos(angle * 3) * 4, -sin(angle) * 4, // Orbiting camera
					0, 1, 0
				)
				tick++
			}

			launchImmediately {
				while (true) {
					tween(time = 16.seconds) {
						cube1.modelMat.identity().rotate((it * 360).degrees, 0.degrees, 0.degrees)
						cube2.modelMat.identity().rotate(0.degrees, (it * 360).degrees, 0.degrees)
					}
				}
			}
		}

		image(resourcesVfs["korge.png"].readNativeImage()).position(views.virtualWidth, 0).anchor(1, 0).alpha(0.5)
	}

}

object Demo2 {
	@JvmStatic
	suspend fun main(args: Array<String>) = Korge(title = "KorGE 3D", bgcolor = Colors.DARKGREY) {
		//delay(10.seconds)
		//println("delay")
		scene3D {
			val light1 = light().position(0, 10, +10).diffuseColor(Colors.RED)
			val light2 = light().position(10, 0, +10).diffuseColor(Colors.BLUE)

			launchImmediately {
				while (true) {
					tween(light1::localY[-20], light2::localX[-20], time = 1.seconds, easing = Easing.SMOOTH)
					tween(light1::localY[+20], light2::localX[+20], time = 1.seconds, easing = Easing.SMOOTH)
				}
			}

			//val library = resourcesVfs["scene.dae"].readColladaLibrary()
			//val library = resourcesVfs["cilinder.dae"].readColladaLibrary()
			//val library = resourcesVfs["monkey.dae"].readColladaLibrary()
			val library = resourcesVfs["monkey-smooth.dae"].readColladaLibrary()
			//val library = resourcesVfs["plane.dae"].readColladaLibrary()
			//val cubeGeom = library.geometryDefs["Cube-mesh"]!! as Library3D.RawGeometryDef
			val cubeGeom = library.geometryDefs.values.first() as Library3D.RawGeometryDef
			val cube = mesh(cubeGeom.mesh).rotation(-90.degrees, 0.degrees, 0.degrees)
			println(library)
			/*
            launchImmediately {
                orbit(cube, 4.0, time = 10.seconds)
            }
            */

			var tick = 0
			addUpdatable {
				val angle = (tick / 1.0).degrees
				camera.positionLookingAt(
					cos(angle * 1) * 4, 0.0, -sin(angle * 1) * 4, // Orbiting camera
					0, 0, 0
				)
				tick++
			}
		}
	}


	suspend fun Stage3D.orbit(v: View3D, distance: Double, time: TimeSpan) {
		view.tween(time = time) { ratio ->
			val angle = 360.degrees * ratio
			camera.positionLookingAt(
				cos(angle) * distance, 0.0, sin(angle) * distance, // Orbiting camera
				v.localTransform.translation.x, v.localTransform.translation.y, v.localTransform.translation.z
			)
		}
	}
}

object Demo3 {
	@JvmStatic
	suspend fun main(args: Array<String>) = Korge(title = "KorGE 3D", bgcolor = Colors.DARKGREY) {
		//delay(10.seconds)
		//println("delay")
		scene3D {
			val light1 = light().position(0, 10, +10).colors(Colors.RED, Colors.RED, Colors.RED)
			val light2 = light().position(10, 0, +10).colors(Colors.BLUE, Colors.BLUE, Colors.BLUE)

			for (light in findByType<Light3D>()) {
				println("LIGHT: $light")
			}

			launchImmediately {
				while (true) {
					tween(light1::localY[-20], light2::localX[-20], time = 1.seconds, easing = Easing.SMOOTH)
					tween(light1::localY[+20], light2::localX[+20], time = 1.seconds, easing = Easing.SMOOTH)
				}
			}

			//val library = resourcesVfs["scene.dae"].readColladaLibrary()
			//val library = resourcesVfs["cilinder.dae"].readColladaLibrary()
			//val library = resourcesVfs["monkey.dae"].readColladaLibrary()
			val library = resourcesVfs["monkey-smooth.dae"].readColladaLibrary()
			//val library = resourcesVfs["plane.dae"].readColladaLibrary()
			//val cubeGeom = library.geometryDefs["Cube-mesh"]!! as Library3D.RawGeometryDef
			val cubeGeom = library.geometryDefs.values.first() as Library3D.RawGeometryDef
			val cube = mesh(cubeGeom.mesh).rotation(-90.degrees, 0.degrees, 0.degrees)
			println(library)
			/*
            launchImmediately {
                orbit(cube, 4.0, time = 10.seconds)
            }
            */

			var tick = 0
			addUpdatable {
				val angle = (tick / 1.0).degrees
				camera.positionLookingAt(
					cos(angle * 1) * 4, 0.0, -sin(angle * 1) * 4, // Orbiting camera
					0, 0, 0
				)
				tick++
			}
		}
	}
}
