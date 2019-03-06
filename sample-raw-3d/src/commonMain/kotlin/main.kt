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
		camera.set(fov = 60.degrees, near = 0.3, far = 1000.0)

		val rotAxis = Vector3D(1f, 1f, 1f)
		val cube = box(1, 1, 1) {
			//this.localTransform.setScale(2.0, 2.0, 2.0)
			//this.localTransform.setRotation(0.degrees, 0.degrees, 45.degrees)
			//this.localTransform.setRotation(45.degrees, 0.degrees, 0.degrees)
			//position(-.5, 0, -5)
			//position(0, 0, 0)
			//modelMat.setToRotation(0.degrees, rotAxis)
		}
		val cube2 = box(2, 1, 1) {
			position(0, 2, 0)
			modelMat.setToRotation(0.degrees, rotAxis)
		}

		var pos = 0f
		var angle = 0.degrees
		addUpdatable {
			camera.transform
				//.setTranslation(0, 0, -4)
				//.setRotation(angle, 0.degrees, 0.degrees)
				//.setRotation(30.degrees, 0.degrees, 0.degrees)
				.setTranslationAndLookAt(
					cos(angle * 2) * 4, cos(angle * 3) * 4, -sin(angle) * 4, // Orbiting camera
					0, 0, 0
				)
				//.setTranslation(0, angle.degrees * 0.1, -4)
				//.setRotation(angle, 0.degrees, 0.degrees)
				/*
				.setTranslationAndLookAt(
					0, -pos, +4,
					0, 0, 0
				)
				*/
			/*
			camera.transform
				.setTranslation(0, -3, -3)
				.setRotation(0.degrees, angle, 0.degrees)
			*/
			pos -= 0.005f
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
