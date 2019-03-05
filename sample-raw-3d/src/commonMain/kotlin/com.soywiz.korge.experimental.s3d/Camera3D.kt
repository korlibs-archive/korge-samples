package com.soywiz.korge.experimental.s3d

import com.soywiz.korma.geom.*
import kotlin.math.*

abstract class Camera3D {
	private var projMat = Matrix3D()
	private var width: Double = 0.0
	private var height: Double = 0.0
	protected var dirty = true

	protected inline fun dirty(cond: () -> Boolean = { true }, callback: () -> Unit) {
		if (cond()) {
			this.dirty = true
			callback()
		}
	}

	fun getProjMatrix(width: Double, height: Double): Matrix3D {
		if (this.width != width || this.height != height) {
			this.dirty = true
			this.width = width
			this.height = height
		}
		if (dirty) {
			dirty = false
			updateMatrix(projMat, this.width, this.height)
		}
		return projMat
	}

	protected abstract fun updateMatrix(mat: Matrix3D, width: Double, height: Double)

	class Perspective(
		fov: Angle = 45.degrees,
		near: Double = 1.0,
		far: Double = 20.0
	) : Camera3D() {
		var fov: Angle = fov; set(value) = dirty({ field != value }) { field = value }
		var near: Double = near; set(value) = dirty({ field != value }) { field = value }
		var far: Double = far; set(value) = dirty({ field != value }) { field = value }

		val transform = Transform3D()

		fun set(fov: Angle = this.fov, near: Double = this.near, far: Double = this.far) = this.apply {
			this.fov = fov
			this.near = near
			this.far = far
		}

		override fun updateMatrix(mat: Matrix3D, width: Double, height: Double) {
			mat.setToPerspective(fov, if (height != 0.0) width / height else 1.0, near, far)
		}
	}
}

private val tempMatrix3D = Matrix3D()
class Transform3D {
	private var matrixDirty = false
	private var transformDirty = false
	val matrix: Matrix3D = Matrix3D()
		get() = run {
			if (matrixDirty) {
				matrixDirty = false
				field
					.identity()
					.setToRotation(rotation) // @TODO: This is buggy. Seems that something is transposed
					.translate(translation)
			}
			field
		}
	val translation = Vector3D()
	var rotation = EulerRotation()

	fun setMatrix(mat: Matrix3D) {
		this.matrix.copyFrom(mat)
		transformDirty = true
	}

	fun setTranslation(x: Number, y: Number, z: Number, w: Number = 1f) = this.apply {
		matrixDirty = true
		translation.setTo(x, y, z, w)
	}

	fun setRotation(roll: Angle, pitch: Angle, yaw: Angle) = this.apply {
		matrixDirty = true
		rotation.setTo(roll, pitch, yaw)
		//rotation.setEuler(roll, pitch, yaw)
		//rotation.setEuler(pitch, roll, yaw)
	}
}

class EulerRotation(
	var x: Angle = 0.degrees,
	var y: Angle = 0.degrees,
	var z: Angle = 0.degrees
)

class Quaternion {
	var x = 0f
	var y = 0f
	var z = 0f
	var w = 0f
}

fun EulerRotation.setQuaternion(x: Number, y: Number, z: Number, w: Number): EulerRotation = quaternionToEuler(x, y, z, w, this)
fun EulerRotation.setQuaternion(quaternion: Quaternion): EulerRotation = quaternionToEuler(quaternion.x, quaternion.y, quaternion.z, quaternion.w, this)
fun EulerRotation.setTo(roll: Angle, pitch: Angle, yaw: Angle): EulerRotation = this
	.apply { this.x = roll }
	.apply { this.y = pitch }
	.apply { this.z = yaw }

fun Quaternion.setEuler(roll: Angle, pitch: Angle, yaw: Angle): Quaternion = eulerToQuaternion(roll, pitch, yaw, this)
fun Quaternion.setEuler(euler: EulerRotation): Quaternion = eulerToQuaternion(euler, this)

private val tempQuat = Quaternion()
fun EulerRotation.toMatrix(out: Matrix3D = Matrix3D()): Matrix3D = tempQuat.setEuler(this).toMatrix(out)
fun Quaternion.toMatrix(out: Matrix3D = Matrix3D()): Matrix3D = quaternionToMatrix(this, out)

fun eulerToQuaternion(euler: EulerRotation, quaternion: Quaternion = Quaternion()): Quaternion = eulerToQuaternion(euler.x, euler.y, euler.z, quaternion)

fun eulerToQuaternion(roll: Angle, pitch: Angle, yaw: Angle, quaternion: Quaternion = Quaternion()): Quaternion {
	val cr = cos(roll * 0.5)
	val sr = sin(roll * 0.5)
	val cp = cos(pitch * 0.5)
	val sp = sin(pitch * 0.5)
	val cy = cos(yaw * 0.5)
	val sy = sin(yaw * 0.5)
	quaternion.x = (cy * cp * sr - sy * sp * cr).toFloat()
	quaternion.y = (sy * cp * sr + cy * sp * cr).toFloat()
	quaternion.z = (sy * cp * cr - cy * sp * sr).toFloat()
	quaternion.w = (cy * cp * cr + sy * sp * sr).toFloat()
	return quaternion
}

fun quaternionToEuler(q: Quaternion, euler: EulerRotation = EulerRotation()): EulerRotation = quaternionToEuler(q.x, q.y, q.z, q.w, euler)

inline fun quaternionToEuler(x: Number, y: Number, z: Number, w: Number, euler: EulerRotation = EulerRotation()): EulerRotation {
	return quaternionToEuler(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat(), euler)
}

fun quaternionToEuler(x: Float, y: Float, z: Float, w: Float, euler: EulerRotation = EulerRotation()): EulerRotation {
	val sinrCosp = +2.0 * (w * x + y * z)
	val cosrCosp = +1.0 - 2.0 * (x * x + y * y)
	val roll = atan2(sinrCosp, cosrCosp)
	val sinp = +2.0 * (w * y - z * x)
	val pitch = when {
		abs(sinp) >= 1 -> if (sinp > 0) PI / 2 else -PI / 2
		else -> asin(sinp)
	}
	val sinyCosp = +2.0 * (w * z + x * y)
	val cosyCosp = +1.0 - 2.0 * (y * y + z * z)
	val yaw = atan2(sinyCosp, cosyCosp)
	euler.x = roll.radians
	euler.y = pitch.radians
	euler.z = yaw.radians
	return euler
}

private val tempMat1 = Matrix3D()
private val tempMat2 = Matrix3D()
fun quaternionToMatrix(quat: Quaternion, out: Matrix3D = Matrix3D(), temp1: Matrix3D = tempMat1, temp2: Matrix3D = tempMat2): Matrix3D {
	temp1.setRows(
		quat.w, quat.z, -quat.y, quat.x,
		-quat.z, quat.w, quat.x, quat.y,
		quat.y, -quat.x, quat.w, quat.z,
		-quat.x, -quat.y, -quat.z, quat.w
	)
	temp2.setRows(
		quat.w, quat.z, -quat.y, -quat.x,
		-quat.z, quat.w, quat.x, -quat.y,
		quat.y, -quat.x, quat.w, -quat.z,
		quat.x, quat.y, quat.z, quat.w
	)
	return out.multiply(temp1, temp2)
}

typealias PerspectiveCamera3D = Camera3D.Perspective
