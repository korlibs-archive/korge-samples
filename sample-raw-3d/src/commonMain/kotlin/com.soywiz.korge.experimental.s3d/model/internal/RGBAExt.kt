package com.soywiz.korge.experimental.s3d.model.internal

import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*

internal fun Vector3D.setToColorPremultiplied(col: RGBA): Vector3D = this.apply { col.toPremultipliedVector3D(this) }
internal fun Vector3D.setToColor(col: RGBA): Vector3D = this.apply { col.toPremultipliedVector3D(this) }

internal fun RGBA.toPremultipliedVector3D(out: Vector3D = Vector3D()): Vector3D = out.setTo(
	rf * af, gf * af, bf * af, 1f
)

internal fun RGBA.toVector3D(out: Vector3D = Vector3D()): Vector3D = out.setTo(
	rf, gf, bf, af
)

fun Vector3D.scale(scale: Float) = this.setTo(this.x * scale, this.y * scale, this.z * scale, this.w * scale)
inline fun Vector3D.scale(scale: Number) = scale(scale.toFloat())
