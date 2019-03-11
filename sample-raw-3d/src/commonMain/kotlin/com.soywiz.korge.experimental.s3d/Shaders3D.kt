package com.soywiz.korge.experimental.s3d

import com.soywiz.korag.shader.*
import com.soywiz.korag.shader.gl.*
import kotlin.native.concurrent.*

object Shaders3D {
	fun transpose(a: Operand) = Program.Func("transpose", a)
	fun inverse(a: Operand) = Program.Func("inverse", a)
	fun int(a: Operand) = Program.Func("int", a)
	operator fun Operand.get(index: Operand) = Program.ArrayAccess(this, index)

	val u_Shiness = Uniform("u_shiness", VarType.Float1)
	val u_ProjMat = Uniform("u_ProjMat", VarType.Mat4)
	val u_ViewMat = Uniform("u_ViewMat", VarType.Mat4)
	val u_BindMat = Uniform("u_BindMat", VarType.Mat4)
	val u_ModMat = Uniform("u_ModMat", VarType.Mat4)
	val u_NormMat = Uniform("u_NormMat", VarType.Mat4)
	//val MAX_BONE_MATS = 16
	val MAX_BONE_MATS = 64
	val u_BoneMats = Uniform("u_BoneMats", VarType.Mat4, arrayCount = MAX_BONE_MATS)
	val u_TexUnit = Uniform("u_TexUnit", VarType.TextureUnit)
	val a_pos = Attribute("a_Pos", VarType.Float3, normalized = false)
	val a_norm = Attribute("a_Norm", VarType.Float3, normalized = false)
	val a_tex = Attribute("a_TexCoords", VarType.Float2, normalized = false)
	val a_boneIndex0 = Attribute("a_BoneIndex0", VarType.Float4, normalized = false)
	val a_weight0 = Attribute("a_Weight0", VarType.Float4, normalized = false)
	val a_col = Attribute("a_Col", VarType.Float3, normalized = true)
	val v_col = Varying("v_Col", VarType.Float3)

	val v_Pos = Varying("v_Pos", VarType.Float3)
	val v_Norm = Varying("v_Norm", VarType.Float3)
	val v_TexCoords = Varying("v_TexCoords", VarType.Float2)

	val v_Temp1 = Varying("v_Temp1", VarType.Float4)

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
		val specular = Uniform("light${id}_specular", VarType.Float4)
		val ambient = Uniform("light${id}_ambient", VarType.Float4)
	}

	val lights = (0 until 4).map { LightAttributes(it) }

	fun Program.Builder.addLight(light: LightAttributes, out: Operand) {
		val v = v_Pos
		val N = v_Norm

		val L = createTemp(VarType.Float3)
		val E = createTemp(VarType.Float3)
		val R = createTemp(VarType.Float3)

		SET(L, normalize(light.sourcePos["xyz"] - v))
		SET(E, normalize(-v)) // we are in Eye Coordinates, so EyePos is (0,0,0)
		SET(R, normalize(-reflect(L, N)))

		SET(out["rgb"], out["rgb"] + light.ambient["rgb"])
		SET(out["rgb"], out["rgb"] + clamp(light.diffuse * max(dot(N, L), 0f.lit), 0f.lit, 1f.lit)["rgb"])
		SET(out["rgb"], out["rgb"] + clamp(light.specular * pow(max(dot(R, E), 0f.lit), 0.3f.lit * u_Shiness), 0f.lit, 1f.lit)["rgb"])
	}

	@ThreadLocal
	val programCache = LinkedHashMap<String, Program>()

	private fun Program.Builder.getBoneIndex(index: Int) = int(a_boneIndex0[index])
	private fun Program.Builder.getWeight(index: Int) = a_weight0[index]
	private fun Program.Builder.getBone(index: Int) = u_BoneMats[getBoneIndex(index)]

	fun Program.Builder.mat4Identity() = Program.Func("mat4",
		1f.lit, 0f.lit, 0f.lit, 0f.lit,
		0f.lit, 1f.lit, 0f.lit, 0f.lit,
		0f.lit, 0f.lit, 1f.lit, 0f.lit,
		0f.lit, 0f.lit, 0f.lit, 1f.lit
	)

	@Suppress("RemoveCurlyBracesFromTemplate")
	fun getProgram3D(nlights: Int, nweights: Int, hasTexture: Boolean): Program {
		return programCache.getOrPut("program_L${nlights}_W${nweights}_T${hasTexture}") {
			Program(
				vertex = VertexShader {
					val modelViewMat = createTemp(VarType.Mat4)
					val normalMat = createTemp(VarType.Mat4)

					val skinMatrix = createTemp(VarType.Mat4)

					if (nweights == 0) {
						SET(skinMatrix, mat4Identity())
					} else {
						for (wIndex in 0 until nweights) {
							IF(getBoneIndex(wIndex) ge 0.lit) {
								SET(skinMatrix, skinMatrix + (getBone(wIndex) * getWeight(wIndex)))
							}
						}
					}

					SET(modelViewMat, u_ModMat * u_ViewMat)
					SET(normalMat, u_NormMat)
					SET(v_Pos, vec3(modelViewMat * u_BindMat * skinMatrix * vec4(a_pos, 1f.lit)))
					SET(v_Norm, vec3(normalMat * u_BindMat * skinMatrix * vec4(a_norm, 1f.lit)))
					if (hasTexture) {
						SET(v_TexCoords, a_tex["xy"])
					}

					SET(out, u_ProjMat * vec4(v_Pos, 1f.lit))

					//SET(v_Temp1.x, u_BoneMats[int(a_weight0.x)][0][0])

					//SET(v_Temp1, a_weight0)
					//SET(v_Temp1, a_boneIndex0 / 4f.lit)
				},
				fragment = FragmentShader {
					//SET(out, vec4(1f.lit, 1f.lit, 1f.lit, 1f.lit))
					//SET(out, vec4(0f.lit, 0f.lit, 0f.lit, 1f.lit))
					if (hasTexture) {
						SET(out, vec4(texture2D(u_TexUnit, v_TexCoords["xy"])["rgb"], 1f.lit))
					} else {
						SET(out, vec4(0f.lit, 0f.lit, 0f.lit, 1f.lit))
					}
					for (n in 0 until nlights) {
						addLight(lights[n], out)
					}
					//SET(out, vec4(v_Temp1.x, v_Temp1.y, v_Temp1.z, 1f.lit))
				},
				name = "programColor3D"
			).apply {
				println(GlslGenerator(kind = ShaderType.VERTEX).generate(this.vertex.stm))
				println(GlslGenerator(kind = ShaderType.FRAGMENT).generate(this.fragment.stm))
			}
		}
	}

	val layoutPosCol = VertexLayout(a_pos, a_col)

	private val FLOATS_PER_VERTEX = layoutPosCol.totalSize / Int.SIZE_BYTES /*Float.SIZE_BYTES is not defined*/
}
