import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.experimental.s3d.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.format.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*

suspend fun main() = Korge {
	//image(resourcesVfs["korge.png"].readNativeImage())

	scene3D {
		camera.set(fov = 45.degrees, near = 1.0, far = 20.0)

		val rotAxis = Vector3D(1f, 1f, 1f)
		val cube = box(1, 1, 1) {
			position(-.5, 0, -5)
			modelMat.setToRotation(0.degrees, rotAxis)
		}
		val cube2 = box(2, 1, 1) {
			position(+.5, 0, -5)
			modelMat.setToRotation(0.degrees, rotAxis)
		}

		var pos = 0f
		var angle = 0.degrees
		addUpdatable {
			camera.transform
				.setTranslation(0, 0, -4)
				.setRotation(angle, 0.degrees, 0.degrees)
			/*
			camera.transform
				.setTranslation(0, -3, -3)
				.setRotation(0.degrees, angle, 0.degrees)
			*/
			//pos -= 0.01f
			angle += 0.2.degrees
		}

		/*
		launchImmediately {
			while (true) {
				tween(time = 4.seconds) {
					cube.modelMat.setToRotation((it * 360).degrees, rotAxis)
					cube2.modelMat.setToRotation(-(it * 360).degrees, rotAxis)
				}
			}
		}
		*/
	}

	//image(resourcesVfs["korge.png"].readNativeImage()).position(700, 0).alpha(1)
}
