package com.soywiz.korge.experimental.s3d

import com.soywiz.klogger.*
import com.soywiz.korma.geom.*
import kotlin.math.*

@PublishedApi
internal val tempMat3D = Matrix3D()

inline fun Matrix3D.translate(x: Number, y: Number, z: Number, w: Number = 1f, temp: Matrix3D = tempMat3D) = this.apply {
	temp.setToTranslation(x, y, z, w)
	this.multiply(this, temp)
}

inline fun Matrix3D.rotate(angle: Angle, x: Number, y: Number, z: Number, temp: Matrix3D = tempMat3D) = this.apply {
	temp.setToRotation(angle, x, y, z)
	this.multiply(this, temp)
}

inline fun Matrix3D.scale(x: Number, y: Number, z: Number, w: Number = 1f, temp: Matrix3D = tempMat3D) = this.apply {
	temp.setToScale(x, y, z, w)
	this.multiply(this, temp)
}

inline fun Matrix3D.setToRotation(quat: Quaternion, temp: Matrix3D = tempMat3D) = this.apply {
	quat.toMatrix(temp)
	this.multiply(this, temp)
}

inline fun Matrix3D.setToRotation(euler: EulerRotation, temp: Matrix3D = tempMat3D) = this.apply {
	euler.toMatrix(temp)
	this.multiply(this, temp)
}

inline fun Matrix3D.rotate(x: Angle, y: Angle, z: Angle, temp: Matrix3D = tempMat3D) = this.apply {
	rotate(x, 1f, 0f, 0f)
	rotate(y, 0f, 1f, 0f)
	rotate(z, 0f, 0f, 1f)
}

inline fun Matrix3D.rotate(euler: EulerRotation, temp: Matrix3D = tempMat3D) = this.apply {
	temp.setToRotation(euler)
	this.multiply(this, temp)
}

inline fun Matrix3D.rotate(quat: Quaternion, temp: Matrix3D = tempMat3D) = this.apply {
	temp.setToRotation(quat)
	this.multiply(this, temp)
}

fun Vector3D.sub(l: Vector3D, r: Vector3D): Vector3D = setTo(l.x - r.x, l.y - r.y, l.z - r.z, l.w - r.w)
fun Vector3D.add(l: Vector3D, r: Vector3D): Vector3D = setTo(l.x + r.x, l.y + r.y, l.z + r.z, l.w + r.w)
fun Vector3D.cross(a: Vector3D, b: Vector3D): Vector3D = setTo(
	(a.y * b.z - a.z * b.y),
	(a.z * b.x - a.x * b.z),
	(a.x * b.y - a.y * b.x),
	1f
)

private val tempVec1 = Vector3D()
private val tempVec2 = Vector3D()
private val tempVec3 = Vector3D()

fun Matrix3D.setToLookAt(
	eye: Vector3D,
	target: Vector3D,
	up: Vector3D
): Matrix3D {
	val z = tempVec1.sub(eye, target)
	if (z.length3Squared == 0f) z.z = 1f
	z.normalize()
	val x = tempVec2.cross(up, z)
	if (x.length3Squared == 0f) {
		when {
			abs(up.z) == 1f -> z.x += 0.0001f
			else -> z.z += 0.0001f
		}
		z.normalize()
		x.cross(up, z)
	}
	x.normalize()
	val y = tempVec3.cross(z, x)
	return this.setRows(
		x.x, y.x, z.x, 0f,
		x.y, y.y, z.y, 0f,
		x.z, y.z, z.z, 0f,
		0f, 0f, 0f, 1f
	)
}

inline fun Matrix3D.translate(v: Vector3D, temp: Matrix3D = tempMat3D) = translate(v.x, v.y, v.z, v.w, temp)
inline fun Matrix3D.rotate(angle: Angle, v: Vector3D, temp: Matrix3D = tempMat3D) = rotate(angle, v.x, v.y, v.z, temp)
inline fun Matrix3D.scale(v: Vector3D, temp: Matrix3D = tempMat3D) = scale(v.x, v.y, v.z, v.w, temp)

fun Matrix3D.setTRS(translation: Position3D, rotation: Quaternion, scale: Scale3D): Matrix3D {
	val rx = rotation.x
	val ry = rotation.y
	val rz = rotation.z
	val rw = rotation.w

	val xt = rx + rx
	val yt = ry + ry
	val zt = rz + rz

	val xx = rx * xt
	val xy = rx * yt
	val xz = rx * zt

	val yy = ry * yt
	val yz = ry * zt
	val zz = rz * zt

	val wx = rw * xt
	val wy = rw * yt
	val wz = rw * zt

	return setRows(
		((1 - (yy + zz)) * scale.x), ((xy - wz) * scale.y), ((xz + wy) * scale.z), translation.x,
		((xy + wz) * scale.x), ((1 - (xx + zz)) * scale.y), ((yz - wx) * scale.z), translation.y,
		((xz - wy) * scale.x), ((yz + wx) * scale.y), ((1 - (xx + yy)) * scale.z), translation.z,
		0, 0, 0, 1
	)
}

private val tempMat1 = Matrix3D()

fun Matrix3D.getTRS(position: Position3D, rotation: Quaternion, scale: Scale3D): Matrix3D = this.apply {
	val det = determinant
	position.setTo(v03, v13, v23, 1)
	scale.setTo(Vector3D.length(v00, v10, v20) * det.sign, Vector3D.length(v01, v11, v21), Vector3D.length(v02, v12, v22), 1)
	val invSX = 1.0 / scale.x
	val invSY = 1.0 / scale.y
	val invSZ = 1.0 / scale.z
	rotation.setFromRotationMatrix(tempMat1.setRows(
		v00 * invSX, v01 * invSY, v02 * invSZ, v03,
		v10 * invSX, v11 * invSY, v12 * invSZ, v13,
		v20 * invSX, v21 * invSY, v22 * invSZ, v23,
		v30, v31, v32, v33
	))
}

fun Matrix3D.invert(m: Matrix3D = this): Matrix3D {
	val target = this
	m.apply {
		val t11 = v12 * v23 * v31 - v13 * v22 * v31 + v13 * v21 * v32 - v11 * v23 * v32 - v12 * v21 * v33 + v11 * v22 * v33
		val t12 = v03 * v22 * v31 - v02 * v23 * v31 - v03 * v21 * v32 + v01 * v23 * v32 + v02 * v21 * v33 - v01 * v22 * v33
		val t13 = v02 * v13 * v31 - v03 * v12 * v31 + v03 * v11 * v32 - v01 * v13 * v32 - v02 * v11 * v33 + v01 * v12 * v33
		val t14 = v03 * v12 * v21 - v02 * v13 * v21 - v03 * v11 * v22 + v01 * v13 * v22 + v02 * v11 * v23 - v01 * v12 * v23

		val det = v00 * t11 + v10 * t12 + v20 * t13 + v30 * t14

		if (det == 0f) {
			Console.error("Matrix doesn't have inverse")
			return this.identity()
		}

		val detInv = 1 / det

		return target.setRows(
			t11 * detInv,
			t12 * detInv,
			t13 * detInv,
			t14 * detInv,

			(v13 * v22 * v30 - v12 * v23 * v30 - v13 * v20 * v32 + v10 * v23 * v32 + v12 * v20 * v33 - v10 * v22 * v33) * detInv,
			(v02 * v23 * v30 - v03 * v22 * v30 + v03 * v20 * v32 - v00 * v23 * v32 - v02 * v20 * v33 + v00 * v22 * v33) * detInv,
			(v03 * v12 * v30 - v02 * v13 * v30 - v03 * v10 * v32 + v00 * v13 * v32 + v02 * v10 * v33 - v00 * v12 * v33) * detInv,
			(v02 * v13 * v20 - v03 * v12 * v20 + v03 * v10 * v22 - v00 * v13 * v22 - v02 * v10 * v23 + v00 * v12 * v23) * detInv,

			(v11 * v23 * v30 - v13 * v21 * v30 + v13 * v20 * v31 - v10 * v23 * v31 - v11 * v20 * v33 + v10 * v21 * v33) * detInv,
			(v03 * v21 * v30 - v01 * v23 * v30 - v03 * v20 * v31 + v00 * v23 * v31 + v01 * v20 * v33 - v00 * v21 * v33) * detInv,
			(v01 * v13 * v30 - v03 * v11 * v30 + v03 * v10 * v31 - v00 * v13 * v31 - v01 * v10 * v33 + v00 * v11 * v33) * detInv,
			(v03 * v11 * v20 - v01 * v13 * v20 - v03 * v10 * v21 + v00 * v13 * v21 + v01 * v10 * v23 - v00 * v11 * v23) * detInv,

			(v12 * v21 * v30 - v11 * v22 * v30 - v12 * v20 * v31 + v10 * v22 * v31 + v11 * v20 * v32 - v10 * v21 * v32) * detInv,
			(v01 * v22 * v30 - v02 * v21 * v30 + v02 * v20 * v31 - v00 * v22 * v31 - v01 * v20 * v32 + v00 * v21 * v32) * detInv,
			(v02 * v11 * v30 - v01 * v12 * v30 - v02 * v10 * v31 + v00 * v12 * v31 + v01 * v10 * v32 - v00 * v11 * v32) * detInv,
			(v01 * v12 * v20 - v02 * v11 * v20 + v02 * v10 * v21 - v00 * v12 * v21 - v01 * v10 * v22 + v00 * v11 * v22) * detInv
		)
	}
}

fun Vector3D.Companion.lengthSq(x: Double, y: Double, z: Double): Double = x * x + y * y + z * z
fun Vector3D.Companion.length(x: Double, y: Double, z: Double): Double = sqrt(lengthSq(x, y, z))
fun Vector3D.Companion.length(x: Number, y: Number, z: Number): Double = length(x.toDouble(), y.toDouble(), z.toDouble())

inline fun Vector3D.setTo(x: Number, y: Number, z: Number) = setTo(x, y, z, 1f)

inline fun Matrix3D.setToMap(filter: (Float) -> Float) = setRows(
	filter(v00), filter(v01), filter(v02), filter(v03),
	filter(v10), filter(v11), filter(v12), filter(v13),
	filter(v20), filter(v21), filter(v22), filter(v23),
	filter(v30), filter(v31), filter(v32), filter(v33)
)
