package com.soywiz.korge.experimental.s3d.model

import com.soywiz.kds.*
import com.soywiz.korge.experimental.s3d.*
import com.soywiz.korma.geom.*

data class Library3D(
	val cameraDefs: FastStringMap<CameraDef> = FastStringMap(),
	val geometryDefs: FastStringMap<GeometryDef> = FastStringMap()
) {
	open class CameraDef
	data class PerspectiveCameraDef(val xfov: Angle, val zmin: Double, val zmax: Double) : CameraDef()

	open class GeometryDef
	//data class RawGeometryDef(val data: FloatArray, val hasPos: Boolean, val hasNormal: Boolean, val hasUV: Boolean) : GeometryDef()
	data class RawGeometryDef(val mesh: Mesh3D) : GeometryDef()
}
