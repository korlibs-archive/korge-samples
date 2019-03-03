package com.soywiz.korge.experimental.s3d

import com.soywiz.korma.geom.*

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

	fun getMatrix(width: Double, height: Double): Matrix3D {
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
	): Camera3D() {
		var fov: Angle = fov; set(value) = dirty({ field != value}) { field = value }
		var near: Double = near; set(value) = dirty({ field != value}) { field = value }
		var far: Double = far; set(value) = dirty({ field != value}) { field = value }

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

typealias PerspectiveCamera3D = Camera3D.Perspective
