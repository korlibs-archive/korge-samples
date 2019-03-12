package com.soywiz.korge.experimental.s3d.model

import com.soywiz.kds.*
import com.soywiz.korge.experimental.s3d.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*

data class Library3D(
	val cameraDefs: FastStringMap<CameraDef> = FastStringMap(),
	val lightDefs: FastStringMap<LightDef> = FastStringMap(),
	val materialDefs: FastStringMap<MaterialDef> = FastStringMap(),
	val geometryDefs: FastStringMap<GeometryDef> = FastStringMap(),
	val skins: FastStringMap<ColladaParser.Skin> = FastStringMap()
) {
	val library = this

	val mainScene = Scene3D().apply {
		id = "MainScene"
		name = "MainScene"
	}
	var scenes = FastStringMap<Scene3D>()

	open class Instance3D {
		val transform = Matrix3D()
		var def: Def? = null
		val children = arrayListOf<Instance3D>()
		var id: String = ""
		var name: String = ""
		var type: String = ""
	}

	open class Scene3D : Instance3D() {
	}

	open class Def

	open class ObjectDef : Def()

	open class MaterialDef : Def()

	open class LightDef : ObjectDef()

	open class CameraDef : ObjectDef()
	data class PerspectiveCameraDef(val xfov: Angle, val zmin: Double, val zmax: Double) : CameraDef()

	data class GeometryDef(
		val mesh: Mesh3D,
		val skin: SkinDef? = null
	) : ObjectDef()

	data class BoneDef(val name: String, val pose: Matrix3D) : Def() {
		fun toBone() = Bone3D(name, pose.clone())
	}

	data class SkinDef(
		val bindShapeMatrix: Matrix3D,
		val bones: List<BoneDef>
	) : Def()


	class PointLightDef(
		val color: RGBA,
		val constantAttenuation: Double,
		val linearAttenuation: Double,
		val quadraticAttenuation: Double
	) : LightDef()
}

fun Library3D.Instance3D.instantiate(): View3D {
	val def = this.def
	val view: View3D = when (def) {
		null -> {
			Container3D().also { container ->
				for (child in children) {
					container.addChild(child.instantiate())
				}
			}
		}
		is Library3D.GeometryDef -> {
			ViewWithMesh3D(def.mesh)
		}
		is Library3D.PerspectiveCameraDef -> {
			Camera3D.Perspective(def.xfov, def.zmin, def.zmax)
		}
		is Library3D.PointLightDef -> {
			Light3D(def.color, def.constantAttenuation, def.linearAttenuation, def.quadraticAttenuation)
		}
		else -> TODO("def=$def")
	}
	view.id = this.id
	view.name = this.name
	//view.localTransform.setMatrix(this.transform.clone().transpose())
	view.localTransform.setMatrix(this.transform)
	if (def is Library3D.PointLightDef) {
		println(view.localTransform.matrix)
		println(view.localTransform.translation)
		println(view.localTransform.rotation)
		println(view.localTransform.scale)
		println("def: $def")
	}
	return view
}
