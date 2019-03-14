package com.soywiz.korge.experimental.s3d.model.internal

import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.interpolate
import com.soywiz.korma.interpolation.*

fun Matrix3D.setToInterpolated(a: Matrix3D, b: Matrix3D, ratio: Double) = setColumns(
	ratio.interpolate(a.v00, b.v00), ratio.interpolate(a.v10, b.v10), ratio.interpolate(a.v20, b.v20), ratio.interpolate(a.v30, b.v30),
	ratio.interpolate(a.v01, b.v01), ratio.interpolate(a.v11, b.v11), ratio.interpolate(a.v21, b.v21), ratio.interpolate(a.v31, b.v31),
	ratio.interpolate(a.v02, b.v02), ratio.interpolate(a.v12, b.v12), ratio.interpolate(a.v22, b.v22), ratio.interpolate(a.v32, b.v32),
	ratio.interpolate(a.v03, b.v03), ratio.interpolate(a.v13, b.v13), ratio.interpolate(a.v23, b.v23), ratio.interpolate(a.v33, b.v33)
)
