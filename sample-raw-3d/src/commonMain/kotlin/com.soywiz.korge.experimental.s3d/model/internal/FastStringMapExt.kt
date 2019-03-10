package com.soywiz.korge.experimental.s3d.model.internal

import com.soywiz.kds.*

fun <T> Map<String, T>.toFast() = FastStringMap<T>().apply {
	for (k in this@toFast.keys) {
		this[k] = this@toFast[k]!!
	}
}
