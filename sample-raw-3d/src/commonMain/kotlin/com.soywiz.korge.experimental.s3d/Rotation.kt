package com.soywiz.korge.experimental.s3d

import com.soywiz.korio.lang.*
import com.soywiz.korma.geom.*
import kotlin.math.*

class EulerRotation(
	var x: Angle = 0.degrees,
	var y: Angle = 0.degrees,
	var z: Angle = 0.degrees
)

class Quaternion(
	var x: Float = 0f,
	var y: Float = 0f,
	var z: Float = 0f,
	var w: Float = 1f
)

fun EulerRotation.setQuaternion(x: Number, y: Number, z: Number, w: Number): EulerRotation = quaternionToEuler(x, y, z, w, this)
fun EulerRotation.setQuaternion(quaternion: Quaternion): EulerRotation = quaternionToEuler(quaternion.x, quaternion.y, quaternion.z, quaternion.w, this)
fun EulerRotation.setTo(roll: Angle, pitch: Angle, yaw: Angle): EulerRotation = this
	.apply { this.x = roll }
	.apply { this.y = pitch }
	.apply { this.z = yaw }
fun EulerRotation.setTo(other: EulerRotation): EulerRotation = setTo(other.x, other.y, other.z)

fun Quaternion.setEuler(roll: Angle, pitch: Angle, yaw: Angle): Quaternion = eulerToQuaternion(roll, pitch, yaw, this)
fun Quaternion.setEuler(euler: EulerRotation): Quaternion = eulerToQuaternion(euler, this)
fun Quaternion.setTo(euler: EulerRotation): Quaternion = eulerToQuaternion(euler, this)
inline fun Quaternion.setTo(x: Number, y: Number, z: Number, w: Number): Quaternion = this
	.apply { this.x = x.toFloat() }
	.apply { this.y = y.toFloat() }
	.apply { this.y = y.toFloat() }
	.apply { this.w = w.toFloat() }
inline fun Quaternion.setTo(other: Quaternion): Quaternion = setTo(other.x, other.y, other.z, other.w)

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
