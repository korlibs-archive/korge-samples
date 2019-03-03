package com.soywiz.korge.experimental.s3d.model

class WavefrontScene(
	val objects: Map<String, WavefrontMesh>
)

class WavefrontMesh(
	// (x, y, z, w), (u, v, w), (nx, ny, nz)
	val vertexData: FloatArray,
	val indices: IntArray
) {

}
