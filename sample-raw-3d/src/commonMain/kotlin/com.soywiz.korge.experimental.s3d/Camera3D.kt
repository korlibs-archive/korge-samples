package com.soywiz.korge.experimental.s3d

import com.soywiz.korma.geom.*

abstract class Camera3D : Object3D() {
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
		fov: Angle = 60.degrees,
		near: Double = 0.3,
		far: Double = 1000.0
	) : Camera3D() {
		var fov: Angle = fov; set(value) = dirty({ field != value }) { field = value }
		var near: Double = near; set(value) = dirty({ field != value }) { field = value }
		var far: Double = far; set(value) = dirty({ field != value }) { field = value }

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
	@PublishedApi
	internal var matrixDirty = false
	@PublishedApi
	internal var transformDirty = false
	val matrix: Matrix3D = Matrix3D()
		get() = run {
			if (matrixDirty) {
				matrixDirty = false
				field.setTRS(translation, rotation, scale)
			}
			field
		}

	private val _translation = Position3D(0, 0, 0)
	private val _rotation = Quaternion()
	private val _scale = Scale3D(1, 1, 1)
	private fun updateTRS() {
		matrix.getTRS(_translation, rotation, _scale)
		transformDirty = false
	}

	val translation: Position3D get() {
		if (transformDirty) updateTRS()
		return _translation
	}
	val rotation: Quaternion get() {
		if (transformDirty) updateTRS()
		return _rotation
	}
	val scale: Scale3D get() {
		if (transformDirty) updateTRS()
		return _scale
	}

	fun setMatrix(mat: Matrix3D) {
		this.matrix.copyFrom(mat)
		transformDirty = true
	}

	@PublishedApi
	internal val UP = Vector3D(0f, 1f, 0f)

	@PublishedApi internal val tempMat1 = Matrix3D()
	@PublishedApi internal val tempMat2 = Matrix3D()
	@PublishedApi internal val tempVec1 = Vector3D()
	@PublishedApi internal val tempVec2 = Vector3D()

	inline fun lookAt(
		tx: Number, ty: Number, tz: Number,
		up: Vector3D = UP
	) = this.apply {
		tempMat1.setToLookAt(translation, tempVec1.setTo(tx, ty, tz, 1f), up)
		rotation.setFromRotationMatrix(tempMat1)
	}

	inline fun setTranslationAndLookAt(
		px: Number, py: Number, pz: Number,
		tx: Number, ty: Number, tz: Number,
		up: Vector3D = UP
	) = this.apply {
		//setTranslation(px, py, pz)
		//lookUp(tx, ty, tz, up)
		setMatrix(matrix.multiply(
			tempMat1.setToTranslation(px, py, pz),
			tempMat2.setToLookAt(tempVec1.setTo(px, py, pz), tempVec2.setTo(tx, ty, tz), up)
		))
	}

	inline fun setTranslation(x: Number, y: Number, z: Number, w: Number = 1f) = this.apply {
		matrixDirty = true
		translation.setTo(x, y, z, w)
	}

	fun setRotation(quat: Quaternion) = this.apply {
		matrixDirty = true
		rotation.setTo(quat)
	}

	inline fun setRotation(x: Number, y: Number, z: Number, w: Number) = this.apply {
		matrixDirty = true
		rotation.setTo(x, y, z, w)
	}

	fun setRotation(euler: EulerRotation) = this.apply {
		matrixDirty = true
		rotation.setEuler(euler)
	}

	fun setRotation(x: Angle, y: Angle, z: Angle) = this.apply {
		matrixDirty = true
		rotation.setEuler(x, y, z)
	}

	inline fun setScale(x: Number = 1f, y: Number = 1f, z: Number = 1f, w: Number = 1f) = this.apply {
		matrixDirty = true
		scale.setTo(x, y, z, w)
	}
}

typealias PerspectiveCamera3D = Camera3D.Perspective
typealias Position3D = Vector3D
typealias Scale3D = Vector3D
