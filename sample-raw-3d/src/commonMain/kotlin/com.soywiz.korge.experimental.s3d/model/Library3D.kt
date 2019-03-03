package com.soywiz.korge.experimental.s3d.model

import com.soywiz.kds.*
import com.soywiz.korma.geom.*

class Library3D {
	open class CameraDef
	data class PerspectiveCameraDef(val xfov: Angle, val zmin: Double, val zmax: Double) : CameraDef()

	open class GeometryDef
	data class RawGeometryDef(val data: FloatArray) : GeometryDef()

	val cameraDefs = FastStringMap<CameraDef>()
	val geometryDefs = FastStringMap<GeometryDef>()
}
