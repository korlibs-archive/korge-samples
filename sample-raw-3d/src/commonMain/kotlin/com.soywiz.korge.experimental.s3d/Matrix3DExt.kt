package com.soywiz.korge.experimental.s3d

import com.soywiz.korma.geom.*

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

inline fun Matrix3D.translate(v: Vector3D, temp: Matrix3D = tempMat3D) = translate(v.x, v.y, v.z, v.w, temp)
inline fun Matrix3D.rotate(angle: Angle, v: Vector3D, temp: Matrix3D = tempMat3D) = rotate(angle, v.x, v.y, v.z, temp)
inline fun Matrix3D.scale(v: Vector3D, temp: Matrix3D = tempMat3D) = scale(v.x, v.y, v.z, v.w, temp)
