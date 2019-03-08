package com.soywiz.korge.experimental.s3d

import com.soywiz.kds.*
import com.soywiz.kds.iterators.*
import com.soywiz.korag.*
import com.soywiz.korag.shader.*
import com.soywiz.korge.internal.*
import com.soywiz.korge.render.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*

inline fun Container.scene3D(views: Views3D = Views3D(), callback: Stage3D.() -> Unit = {}): Stage3DView =
	Stage3DView(Stage3D(views).apply(callback)).addTo(this)

class Views3D {
}

fun Container3D.light(callback: Light3D.() -> Unit = {}) = Light3D().apply(callback).addTo(this)

class Light3D : View3D() {
	val diffuseColor = Colors.RED
	internal val tempArray1 = FloatArray(4)
	internal val tempArray2 = FloatArray(4)

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
	val tmepMat = Matrix3D()
	val projMat: Matrix3D = Matrix3D()
	val lights = arrayListOf<Light3D>()
	val projCameraMat: Matrix3D = Matrix3D()
	val cameraMat: Matrix3D = Matrix3D()
	val cameraMatInv: Matrix3D = Matrix3D()
	val dynamicVertexBufferPool = Pool { ag.createVertexBuffer() }
}

abstract class Object3D {
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


}

abstract class View3D : Object3D() {
	companion object {
		fun transpose(a: Operand) = Program.Func("transpose", a)
		fun inverse(a: Operand) = Program.Func("inverse", a)

		val u_ProjMat = Uniform("u_ProjMat", VarType.Mat4)
		val u_ViewMat = Uniform("u_ViewMat", VarType.Mat4)
		val u_ModMat = Uniform("u_ModMat", VarType.Mat4)
		val u_NormMat = Uniform("u_NormMat", VarType.Mat4)
		val a_pos = Attribute("a_Pos", VarType.Float3, normalized = false)
		val a_norm = Attribute("a_Norm", VarType.Float3, normalized = true)
		val a_col = Attribute("a_Col", VarType.Float3, normalized = true)
		val v_col = Varying("v_Col", VarType.Float3)

		val v_Pos = Varying("v_Pos", VarType.Float3)
		val v_Norm = Varying("v_Norm", VarType.Float3)

		val programColor3D = Program(
			vertex = VertexShader {
				SET(v_col, a_col)
				SET(out, u_ProjMat * u_ModMat * u_ViewMat * vec4(a_pos, 1f.lit))
			},
			fragment = FragmentShader {
				SET(out, vec4(v_col, 1f.lit))
				//SET(out, vec4(1f.lit, 1f.lit, 1f.lit, 1f.lit))
			},
			name = "programColor3D"
		)

		class LightAttributes(val id: Int) {
			val sourcePos = Uniform("light${id}_pos", VarType.Float3)
			val diffuse = Uniform("light${id}_diffuse", VarType.Float4)
		}

		val lights = (0 until 4).map { LightAttributes(it) }

		val programNorm3D = Program(
			vertex = VertexShader {
				val modelViewMat = createTemp(VarType.Mat4)
				val normalMat = createTemp(VarType.Mat4)
				SET(modelViewMat, u_ModMat * u_ViewMat)
				SET(normalMat, u_NormMat)
				SET(v_Pos, vec3(modelViewMat * vec4(a_pos, 1f.lit)))
				SET(v_Norm, vec3(normalMat * vec4(a_norm, 1f.lit)))
				SET(out, u_ProjMat * modelViewMat * vec4(a_pos, 1f.lit))
			},
			fragment = FragmentShader {
				val N = v_Norm
				val v = v_Pos

				val light = lights[0]

				val L = createTemp(VarType.Float3)
				val Idiff = createTemp(VarType.Float4)

				SET(L, normalize(light.sourcePos["xyz"] - v))
				SET(Idiff, light.diffuse * max(dot(N, L), 0f.lit))
				SET(Idiff, clamp(Idiff, 0f.lit, 1f.lit))

				//SET(out, vec4(1f.lit, 1f.lit, 1f.lit, 1f.lit))
				SET(out, vec4(Idiff["rgb"], 1f.lit))
			},
			name = "programColor3D"
		)
		val layoutPosCol = VertexLayout(a_pos, a_col)

		private val FLOATS_PER_VERTEX = layoutPosCol.totalSize / Int.SIZE_BYTES /*Float.SIZE_BYTES is not defined*/
	}

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

inline fun <T : Object3D> T.position(x: Number, y: Number, z: Number, w: Number = 1f): T = this.apply {
	localTransform.setTranslation(x, y, z, w)
}

inline fun <T : Object3D> T.rotation(x: Angle, y: Angle, z: Angle): T = this.apply {
	localTransform.setRotation(x, y, z)
}

inline fun <T : Object3D> T.scale(x: Number = 1, y: Number = 1, z: Number = 1, w: Number = 1): T = this.apply {
	localTransform.setScale(x, y, z, w)
}

inline fun <T : Object3D> T.lookAt(x: Number, y: Number, z: Number): T = this.apply {
	localTransform.lookAt(x, y, z)
}

inline fun <T : Object3D> T.positionLookingAt(px: Number, py: Number, pz: Number, tx: Number, ty: Number, tz: Number): T = this.apply {
	localTransform.setTranslationAndLookAt(px, py, pz, tx, ty, tz)
}

fun <T : View3D> T.addTo(container: Container3D) = this.apply {
	this.parent?.children?.remove(this)
	container.children += this
	this.parent = container
}

class Mesh3D(val data: FloatArray, val layout: VertexLayout, val program: Program, val drawType: AG.DrawType) {
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
			-cubeSize, -cubeSize, -cubeSize,  1f, 0f, 0f,  //p1
			-cubeSize, -cubeSize, +cubeSize,  1f, 0f, 0f,  //p2
			-cubeSize, +cubeSize, +cubeSize,  1f, 0f, 0f,  //p3
			-cubeSize, -cubeSize, -cubeSize,  1f, 0f, 0f,  //p1
			-cubeSize, +cubeSize, +cubeSize,  1f, 0f, 0f,  //p3
			-cubeSize, +cubeSize, -cubeSize,  1f, 0f, 0f,  //p4

			+cubeSize, +cubeSize, -cubeSize,  0f, 1f, 0f,  //p5
			-cubeSize, -cubeSize, -cubeSize,  0f, 1f, 0f,  //p1
			-cubeSize, +cubeSize, -cubeSize,  0f, 1f, 0f,  //p4
			+cubeSize, +cubeSize, -cubeSize,  0f, 1f, 0f,  //p5
			+cubeSize, -cubeSize, -cubeSize,  0f, 1f, 0f,  //p7
			-cubeSize, -cubeSize, -cubeSize,  0f, 1f, 0f,  //p1

			+cubeSize, -cubeSize, +cubeSize,  0f, 0f, 1f,  //p6
			-cubeSize, -cubeSize, -cubeSize,  0f, 0f, 1f,  //p1
			+cubeSize, -cubeSize, -cubeSize,  0f, 0f, 1f,  //p7
			+cubeSize, -cubeSize, +cubeSize,  0f, 0f, 1f,  //p6
			-cubeSize, -cubeSize, +cubeSize,  0f, 0f, 1f,  //p2
			-cubeSize, -cubeSize, -cubeSize,  0f, 0f, 1f,  //p1

			+cubeSize, +cubeSize, +cubeSize,  0f, 1f, 1f,  //p8
			+cubeSize, +cubeSize, -cubeSize,  0f, 1f, 1f,  //p5
			-cubeSize, +cubeSize, -cubeSize,  0f, 1f, 1f,  //p4
			+cubeSize, +cubeSize, +cubeSize,  0f, 1f, 1f,  //p8
			-cubeSize, +cubeSize, -cubeSize,  0f, 1f, 1f,  //p4
			-cubeSize, +cubeSize, +cubeSize,  0f, 1f, 1f,  //p3

			+cubeSize, +cubeSize, +cubeSize,  1f, 1f, 0f,  //p8
			-cubeSize, +cubeSize, +cubeSize,  1f, 1f, 0f,  //p3
			+cubeSize, -cubeSize, +cubeSize,  1f, 1f, 0f,  //p6
			-cubeSize, +cubeSize, +cubeSize,  1f, 1f, 0f,  //p3
			-cubeSize, -cubeSize, +cubeSize,  1f, 1f, 0f,  //p2
			+cubeSize, -cubeSize, +cubeSize,  1f, 1f, 0f,  //p6

			+cubeSize, +cubeSize, +cubeSize,  1f, 0f, 1f,  //p8
			+cubeSize, -cubeSize, -cubeSize,  1f, 0f, 1f,  //p7
			+cubeSize, +cubeSize, -cubeSize,  1f, 0f, 1f,  //p5
			+cubeSize, -cubeSize, -cubeSize,  1f, 0f, 1f,  //p7
			+cubeSize, +cubeSize, +cubeSize,  1f, 0f, 1f,  //p8
			+cubeSize, -cubeSize, +cubeSize,  1f, 0f, 1f   //p6
		)

		val mesh = Mesh3D(vertices, View3D.layoutPosCol, View3D.programColor3D, AG.DrawType.TRIANGLES)
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

			ag.draw(
				vertexBuffer,
				type = mesh.drawType,
				program = mesh.program,
				vertexLayout = mesh.layout,
				vertexCount = mesh.vertexCount,
				//vertexCount = 6 * 6,
				uniforms = uniformValues.apply {
					this[u_ProjMat] = ctx.projCameraMat
					this[u_ViewMat] = localTransform.matrix
					this[u_ModMat] = tempMat2.multiply(tempMat1.apply { prepareExtraModelMatrix(this) }, modelMat)
					this[u_NormMat] = tempMat3.multiply(tempMat2, localTransform.matrix).invert().transpose()
					ctx.lights.fastForEachWithIndex { index, light: Light3D ->
						this[lights[index].sourcePos] = light.localTransform.translation.data
						this[lights[index].diffuse] = light.tempArray1.apply  {
							this[0] = light.diffuseColor.rf
							this[1] = light.diffuseColor.gf
							this[2] = light.diffuseColor.bf
							this[3] = 1f
						}
					}
				},
				renderState = rs
			)
		}
	}
}
