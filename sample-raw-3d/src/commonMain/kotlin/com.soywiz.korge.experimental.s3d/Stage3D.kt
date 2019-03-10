package com.soywiz.korge.experimental.s3d

import com.soywiz.kds.*
import com.soywiz.kds.iterators.*
import com.soywiz.kmem.*
import com.soywiz.korag.*
import com.soywiz.korag.shader.*
import com.soywiz.korag.shader.gl.*
import com.soywiz.korge.render.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*
import kotlin.native.concurrent.*

inline fun Container.scene3D(views: Views3D = Views3D(), callback: Stage3D.() -> Unit = {}): Stage3DView =
	Stage3DView(Stage3D(views).apply(callback)).addTo(this)

class Views3D {
}

fun Container3D.light(callback: Light3D.() -> Unit = {}) = Light3D().apply(callback).addTo(this)

class Light3D : View3D() {
	var diffuseColor = Colors.WHITE
	var specularColor = Colors.WHITE
	var ambientColor = Colors.WHITE
	var diffusePower = 1.0
	var specularPower = 0.2
	var ambientPower = 0.1
	var power = 1.0
	internal val tempArray1 = FloatArray(4)
	internal val tempArray2 = FloatArray(4)
	internal val tempArray3 = FloatArray(4)

	//val diffuseColorPremult get() = diffuseColor.withAd(power).premultiplied
	//val specularColorPremult get() = specularColor.withAd(power).premultiplied
	//val ambientColorPremult get() = ambientColor.withAd(power).premultiplied

	fun diffuseColor(color: RGBA): Light3D = this.apply { this.diffuseColor = color }
	fun specularColor(color: RGBA): Light3D = this.apply { this.specularColor = color }
	fun ambientColor(color: RGBA): Light3D = this.apply { this.ambientColor = color }

	fun colors(ambient: RGBA, diffuse: RGBA = ambient, specular: RGBA = diffuse) = this.diffuseColor(diffuse).specularColor(specular).ambientColor(ambient)

	override fun render(ctx: RenderContext3D) {
	}
}

class Stage3D(val views: Views3D) : Container3D() {
	lateinit var view: Stage3DView
	var camera = Camera3D.Perspective().apply {
		positionLookingAt(0, 1, -10, 0, 0, 0)
	}
}

class Stage3DView(val stage3D: Stage3D) : View() {
	init {
		stage3D.view = this
	}

	private val ctx3D = RenderContext3D()
	override fun renderInternal(ctx: RenderContext) {
		ctx.flush()
		ctx.ag.clear(depth = 1f, clearColor = false)
		//ctx.ag.clear(color = Colors.RED)
		ctx3D.ag = ctx.ag
		ctx3D.projMat.copyFrom(stage3D.camera.getProjMatrix(ctx.ag.backWidth.toDouble(), ctx.ag.backHeight.toDouble()))
		ctx3D.cameraMat.copyFrom(stage3D.camera.localTransform.matrix)
		ctx3D.cameraMatInv.invert(stage3D.camera.localTransform.matrix)
		ctx3D.projCameraMat.multiply(ctx3D.projMat, ctx3D.cameraMatInv)
		ctx3D.lights.clear()
		stage3D.foreachDescendant {
			if (it is Light3D) {
				ctx3D.lights.add(it)
			}
		}
		stage3D.render(ctx3D)
	}
}

fun View3D?.foreachDescendant(handler: (View3D) -> Unit) {
	if (this != null) {
		handler(this)
		if (this is Container3D) {
			this.children.fastForEach { child ->
				child.foreachDescendant(handler)
			}
		}
	}
}

class RenderContext3D() {
	lateinit var ag: AG
	val bindMat4 = Matrix3D()
	val bones = Array(128) { Matrix3D() }
	val tmepMat = Matrix3D()
	val projMat: Matrix3D = Matrix3D()
	val lights = arrayListOf<Light3D>()
	val projCameraMat: Matrix3D = Matrix3D()
	val cameraMat: Matrix3D = Matrix3D()
	val cameraMatInv: Matrix3D = Matrix3D()
	val dynamicVertexBufferPool = Pool { ag.createVertexBuffer() }
}

abstract class View3D {
	var name: String? = null
	val localTransform = Transform3D()

	var localX: Double
		set(localX) = run { localTransform.setTranslation(localX, localY, localZ, localW) }
		get() = localTransform.translation.x.toDouble()

	var localY: Double
		set(localY) = run { localTransform.setTranslation(localX, localY, localZ, localW) }
		get() = localTransform.translation.y.toDouble()

	var localZ: Double
		set(localZ) = run { localTransform.setTranslation(localX, localY, localZ, localW) }
		get() = localTransform.translation.z.toDouble()

	var localW: Double
		set(localW) = run { localTransform.setTranslation(localX, localY, localZ, localW) }
		get() = localTransform.translation.w.toDouble()

	var parent: Container3D? = null
	val modelMat = Matrix3D()
	//val position = Vector3D()

	abstract fun render(ctx: RenderContext3D)
}

open class Container3D : View3D() {
	val children = arrayListOf<View3D>()

	override fun render(ctx: RenderContext3D) {
		children.fastForEach {
			it.render(ctx)
		}
	}
}

inline fun <reified T : View3D> View3D?.findByType() = sequence<T> {
	for (it in descendants()) {
		if (it is T) yield(it)
	}
}

fun View3D?.descendants(): Sequence<View3D> = sequence<View3D> {
	val view = this@descendants ?: return@sequence
	yield(view)
	if (view is Container3D) {
		view.children.fastForEach {
			yieldAll(it.descendants())
		}
	}
}

operator fun View3D?.get(name: String): View3D? {
	if (this?.name == name) return this
	if (this is Container3D) {
		this.children.fastForEach {
			val result = it[name]
			if (result != null) return result
		}
	}
	return null
}


fun <T : View3D> T.name(name: String) = this.apply { this.name = name }

inline fun <T : View3D> T.position(x: Number, y: Number, z: Number, w: Number = 1f): T = this.apply {
	localTransform.setTranslation(x, y, z, w)
}

inline fun <T : View3D> T.rotation(x: Angle, y: Angle, z: Angle): T = this.apply {
	localTransform.setRotation(x, y, z)
}

inline fun <T : View3D> T.scale(x: Number = 1, y: Number = 1, z: Number = 1, w: Number = 1): T = this.apply {
	localTransform.setScale(x, y, z, w)
}

inline fun <T : View3D> T.lookAt(x: Number, y: Number, z: Number): T = this.apply {
	localTransform.lookAt(x, y, z)
}

inline fun <T : View3D> T.positionLookingAt(px: Number, py: Number, pz: Number, tx: Number, ty: Number, tz: Number): T = this.apply {
	localTransform.setTranslationAndLookAt(px, py, pz, tx, ty, tz)
}

fun <T : View3D> T.addTo(container: Container3D) = this.apply {
	this.parent?.children?.remove(this)
	container.children += this
	this.parent = container
}

data class Bone3D(
	val name: String,
	val matrix: Matrix3D
)

data class Skeleton3D(val bindShapeMatrix: Matrix3D, val bones: List<Bone3D>) {
	//val matrices = Array(bones.size) { Matrix3D() }
	val matrices = Array(Shaders3D.MAX_BONE_MATS) { Matrix3D() }
}

class Mesh3D constructor(val data: FloatArray, val layout: VertexLayout, val program: Program?, val drawType: AG.DrawType, val maxWeights: Int = 0) {
	var skeleton: Skeleton3D? = null

	var shiness = 0.5
	//val modelMat = Matrix3D()
	val vertexSizeInBytes = layout.totalSize
	val vertexSizeInFloats = vertexSizeInBytes / 4
	val vertexCount = data.size / vertexSizeInFloats

	init {
		println("vertexCount: $vertexCount, vertexSizeInFloats: $vertexSizeInFloats, data.size: ${data.size}")
	}
}

inline fun Container3D.box(width: Number = 1, height: Number = width, depth: Number = height, callback: Cube.() -> Unit = {}): Cube {
	return Cube(width.toDouble(), height.toDouble(), depth.toDouble()).apply(callback).addTo(this)
}

class Cube(var width: Double, var height: Double, var depth: Double) : ViewWithMesh3D(Cube.mesh) {
	override fun prepareExtraModelMatrix(mat: Matrix3D) {
		mat.identity().scale(width, height, depth)
	}

	companion object {
		private val cubeSize = .5f

		private val vertices = floatArrayOf(
			-cubeSize, -cubeSize, -cubeSize, 1f, 0f, 0f,  //p1
			-cubeSize, -cubeSize, +cubeSize, 1f, 0f, 0f,  //p2
			-cubeSize, +cubeSize, +cubeSize, 1f, 0f, 0f,  //p3
			-cubeSize, -cubeSize, -cubeSize, 1f, 0f, 0f,  //p1
			-cubeSize, +cubeSize, +cubeSize, 1f, 0f, 0f,  //p3
			-cubeSize, +cubeSize, -cubeSize, 1f, 0f, 0f,  //p4

			+cubeSize, +cubeSize, -cubeSize, 0f, 1f, 0f,  //p5
			-cubeSize, -cubeSize, -cubeSize, 0f, 1f, 0f,  //p1
			-cubeSize, +cubeSize, -cubeSize, 0f, 1f, 0f,  //p4
			+cubeSize, +cubeSize, -cubeSize, 0f, 1f, 0f,  //p5
			+cubeSize, -cubeSize, -cubeSize, 0f, 1f, 0f,  //p7
			-cubeSize, -cubeSize, -cubeSize, 0f, 1f, 0f,  //p1

			+cubeSize, -cubeSize, +cubeSize, 0f, 0f, 1f,  //p6
			-cubeSize, -cubeSize, -cubeSize, 0f, 0f, 1f,  //p1
			+cubeSize, -cubeSize, -cubeSize, 0f, 0f, 1f,  //p7
			+cubeSize, -cubeSize, +cubeSize, 0f, 0f, 1f,  //p6
			-cubeSize, -cubeSize, +cubeSize, 0f, 0f, 1f,  //p2
			-cubeSize, -cubeSize, -cubeSize, 0f, 0f, 1f,  //p1

			+cubeSize, +cubeSize, +cubeSize, 0f, 1f, 1f,  //p8
			+cubeSize, +cubeSize, -cubeSize, 0f, 1f, 1f,  //p5
			-cubeSize, +cubeSize, -cubeSize, 0f, 1f, 1f,  //p4
			+cubeSize, +cubeSize, +cubeSize, 0f, 1f, 1f,  //p8
			-cubeSize, +cubeSize, -cubeSize, 0f, 1f, 1f,  //p4
			-cubeSize, +cubeSize, +cubeSize, 0f, 1f, 1f,  //p3

			+cubeSize, +cubeSize, +cubeSize, 1f, 1f, 0f,  //p8
			-cubeSize, +cubeSize, +cubeSize, 1f, 1f, 0f,  //p3
			+cubeSize, -cubeSize, +cubeSize, 1f, 1f, 0f,  //p6
			-cubeSize, +cubeSize, +cubeSize, 1f, 1f, 0f,  //p3
			-cubeSize, -cubeSize, +cubeSize, 1f, 1f, 0f,  //p2
			+cubeSize, -cubeSize, +cubeSize, 1f, 1f, 0f,  //p6

			+cubeSize, +cubeSize, +cubeSize, 1f, 0f, 1f,  //p8
			+cubeSize, -cubeSize, -cubeSize, 1f, 0f, 1f,  //p7
			+cubeSize, +cubeSize, -cubeSize, 1f, 0f, 1f,  //p5
			+cubeSize, -cubeSize, -cubeSize, 1f, 0f, 1f,  //p7
			+cubeSize, +cubeSize, +cubeSize, 1f, 0f, 1f,  //p8
			+cubeSize, -cubeSize, +cubeSize, 1f, 0f, 1f   //p6
		)

		val mesh = Mesh3D(vertices, Shaders3D.layoutPosCol, Shaders3D.programColor3D, AG.DrawType.TRIANGLES)
	}
}

inline fun Container3D.mesh(mesh: Mesh3D, callback: ViewWithMesh3D.() -> Unit = {}): ViewWithMesh3D {
	return ViewWithMesh3D(mesh).apply(callback).addTo(this)
}

open class ViewWithMesh3D(var mesh: Mesh3D) : View3D() {
	private val uniformValues = AG.UniformValues()
	private val rs = AG.RenderState(depthFunc = AG.CompareMode.LESS_EQUAL)
	//private val rs = AG.RenderState(depthFunc = AG.CompareMode.ALWAYS)

	private val tempMat1 = Matrix3D()
	private val tempMat2 = Matrix3D()
	private val tempMat3 = Matrix3D()

	protected open fun prepareExtraModelMatrix(mat: Matrix3D) {
		mat.identity()
	}

	override fun render(ctx: RenderContext3D) {
		val ag = ctx.ag

		ctx.dynamicVertexBufferPool.alloc { vertexBuffer ->
			vertexBuffer.upload(mesh.data)
			//tempMat2.invert()
			//tempMat3.multiply(ctx.cameraMatInv, this.localTransform.matrix)
			//tempMat3.multiply(ctx.cameraMatInv, Matrix3D().invert(this.localTransform.matrix))
			//tempMat3.multiply(this.localTransform.matrix, ctx.cameraMat)

			Shaders3D.apply {
				ag.draw(
					vertexBuffer,
					type = mesh.drawType,
					program = mesh.program ?: getProgram3D(ctx.lights.size.clamp(0, 4), mesh.maxWeights),
					vertexLayout = mesh.layout,
					vertexCount = mesh.vertexCount,
					//vertexCount = 6 * 6,
					uniforms = uniformValues.apply {
						this[u_ProjMat] = ctx.projCameraMat
						this[u_ViewMat] = localTransform.matrix
						this[u_ModMat] = tempMat2.multiply(tempMat1.apply { prepareExtraModelMatrix(this) }, modelMat)
						this[u_NormMat] = tempMat3.multiply(tempMat2, localTransform.matrix).invert().transpose()

						this[u_Shiness] = mesh.shiness

						val skeleton = mesh.skeleton
						if (skeleton != null) {
							this[u_BindMat] = ctx.bindMat4.copyFrom(skeleton.bindShapeMatrix)
							skeleton.bones.fastForEachWithIndex { index, bone ->
								skeleton.matrices[index].copyFrom(bone.matrix)
								//skeleton.matrices[index].identity()
							}
							//skeleton.matrices[0].rotate(10.degrees, 15.degrees, 0.degrees)
							//skeleton.matrices[1].rotate(20.degrees, 15.degrees, 0.degrees)
							//skeleton.matrices[2].rotate(30.degrees, 15.degrees, 0.degrees)
							//skeleton.matrices[3].rotate(40.degrees, 15.degrees, 0.degrees)
							//skeleton.matrices[0][0, 0] = 0.1f
							this[u_BoneMats] = skeleton.matrices
						} else {
							this[u_BindMat] = ctx.bindMat4.identity()
						}

						ctx.lights.fastForEachWithIndex { index, light: Light3D ->
							this[lights[index].sourcePos] = light.localTransform.translation.data
							val diffuse = light.diffuseColor
							val specular = light.specularColor
							val ambient = light.ambientColor

							//println(light.diffuseColor.withAd(1.0))
							//println(diffuse)
							//println(specular)
							//println(ambient)

							this[lights[index].diffuse] = light.tempArray1.apply {
								val scale = (light.power * light.diffusePower).toFloat()
								this[0] = (diffuse.rf * scale)
								this[1] = (diffuse.gf * scale)
								this[2] = (diffuse.bf * scale)
							}
							this[lights[index].specular] = light.tempArray2.apply {
								val scale = (light.power * light.specularPower).toFloat()
								this[0] = (specular.rf * scale)
								this[1] = (specular.gf * scale)
								this[2] = (specular.bf * scale)
							}
							this[lights[index].ambient] = light.tempArray3.apply {
								val scale = (light.power * light.ambientPower).toFloat()
								this[0] = (ambient.rf * scale)
								this[1] = (ambient.gf * scale)
								this[2] = (ambient.bf * scale)
							}
						}
					},
					renderState = rs
				)
			}
		}
	}
}
