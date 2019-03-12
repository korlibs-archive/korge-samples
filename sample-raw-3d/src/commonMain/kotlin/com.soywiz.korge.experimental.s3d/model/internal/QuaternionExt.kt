package com.soywiz.korge.experimental.s3d.model.internal

import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.interpolate
import com.soywiz.korma.interpolation.*
import kotlin.math.*

fun Vector3D.Companion.lengthSq(x: Double, y: Double, z: Double, w: Double) = x * x + y * y + z * z + w * w
fun Vector3D.Companion.length(x: Double, y: Double, z: Double, w: Double) = sqrt(lengthSq(x, y, z, w))

fun Quaternion.normalize(v: Quaternion = this): Quaternion {
	val length = 1.0 / Vector3D.length(v.x, v.y, v.z, v.w)
	return this.setTo(v.x / length, v.y / length, v.z / length, v.w / length)
}

// @TODO: Make Quaternions interpolable

object Quaternion_Companion

fun dotProduct(l: Quaternion, r: Quaternion): Double = l.x * r.x + l.y * r.y + l.z * r.z + l.w * r.w

operator fun Quaternion.unaryMinus(): Quaternion = Quaternion(-x, -y, -z, -w)
operator fun Quaternion.plus(other: Quaternion): Quaternion = Quaternion(x + other.x, y + other.y, z + other.z, w + other.w)
operator fun Quaternion.minus(other: Quaternion): Quaternion = Quaternion(x - other.x, y - other.y, z - other.z, w - other.w)
operator fun Quaternion.times(scale: Double): Quaternion = Quaternion(x * scale, y * scale, z * scale, w * scale)
operator fun Double.times(scale: Quaternion): Quaternion = scale.times(this)

fun Quaternion.negate() = this.setTo(-x, -y, -z, -w)

inline fun Quaternion.setToFunc(l: Quaternion, r: Quaternion, func: (l: Double, r: Double) -> Double) = setTo(
	func(l.x, r.x),
	func(l.y, r.y),
	func(l.z, r.z),
	func(l.w, r.w)
)

inline fun Vector3D.setToFunc(l: Vector3D, r: Vector3D, func: (l: Float, r: Float) -> Float) = setTo(
	func(l.x, r.x),
	func(l.y, r.y),
	func(l.z, r.z),
	func(l.w, r.w)
)

// @TODO: Allocations and temps!
private val tleft: Quaternion = Quaternion()
private val tright: Quaternion = Quaternion()
fun Quaternion.setToSlerp(left: Quaternion, right: Quaternion, t: Double): Quaternion {
	val tleft = tleft.copyFrom(left).normalize()
	val tright = tright.copyFrom(right).normalize()

	var dot = dotProduct(tleft, right)

	if (dot < 0.0f) {
		tright.negate()
		dot = -dot
	}

	if (dot > 0.99995f) return setToFunc(tleft, tright) { l, r -> l + t * (r - l) }

	val angle0 = acos(dot)
	val angle1 = angle0 * t

	val s1 = sin(angle1) / sin(angle0)
	val s0 = cos(angle1) - dot * s1

	return setToFunc(tleft, tright) { l, r -> (s0 * l) + (s1 * r) }
}

fun Quaternion.setToInterpolated(left: Quaternion, right: Quaternion, t: Double): Quaternion = setToSlerp(left, right, t)

fun Vector3D.setToInterpolated(left: Vector3D, right: Vector3D, t: Double): Vector3D = setToFunc(left, right) { l, r -> t.interpolate(l, r) }
