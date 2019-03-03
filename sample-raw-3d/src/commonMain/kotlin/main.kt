import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.tween.*
import com.soywiz.korio.async.*
import com.soywiz.korma.geom.*

suspend fun main() = Korge {
	scene3D {
		camera.set(fov = 45.degrees, near = 1.0, far = 20.0)

		val rotAxis = Vector3D(1f, 1f, 1f)
		val cube = cube(100, 100, 100) {
			position(0, 0, -5)
			modelMat.setToRotation(0.degrees, rotAxis)
		}
		launchImmediately {
			while (true) {
				tween(time = 4.seconds) {
					cube.modelMat.setToRotation((it * 360).degrees, rotAxis)
				}
			}
		}
	}
}
