package com.soywiz.korge.samples.plugin

import com.soywiz.korge.resources.*
import com.soywiz.korio.file.*

class MyCustomResourceProcessor : ResourceProcessor("demo") {
	override val outputExtension: String get() = "demo.uppercased"
	override val version: Int = 1

	override suspend fun processInternal(inputFile: VfsFile, outputFile: VfsFile) {
		outputFile.writeString(inputFile.readString().toUpperCase())
	}
}
