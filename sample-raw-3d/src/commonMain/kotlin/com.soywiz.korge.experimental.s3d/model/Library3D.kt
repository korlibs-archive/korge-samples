package com.soywiz.korge.experimental.s3d.model

import com.soywiz.kds.*
import com.soywiz.korge.experimental.s3d.*
import com.soywiz.korma.geom.*

data class Library3D(
	val cameraDefs: FastStringMap<CameraDef> = FastStringMap(),
	val geometryDefs: FastStringMap<GeometryDef> = FastStringMap(),
	val skins: FastStringMap<ColladaParser.Skin> = FastStringMap()
) {
	val library = this

	open class CameraDef
	data class PerspectiveCameraDef(val xfov: Angle, val zmin: Double, val zmax: Double) : CameraDef()

	open class GeometryDef
	//data class RawGeometryDef(val data: FloatArray, val hasPos: Boolean, val hasNormal: Boolean, val hasUV: Boolean) : GeometryDef()
	data class RawGeometryDef(
		val mesh: Mesh3D,
		val skin: SkinDef? = null
	) : GeometryDef()

	data class BoneDef(val name: String, val pose: Matrix3D) {
		fun toBone() = Bone3D(name, pose.clone())
	}

	data class SkinDef(
		val bindShapeMatrix: Matrix3D,
		val bones: List<BoneDef>
	)
}
