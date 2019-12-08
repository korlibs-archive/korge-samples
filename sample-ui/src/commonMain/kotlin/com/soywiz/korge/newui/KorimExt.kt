package com.soywiz.korge.newui

import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*

// @TODO: Remove once Korim version is updated

fun Bitmap32.clone() = slice().extract()

fun Bitmap32.checkMatchDimensions(other: Bitmap32): Bitmap32 {
	check((this.width == other.width) && (this.height == other.height)) { "Bitmap doesn't have the same dimensions (${width}x${height}) != (${other.width}x${other.height})" }
	return other
}

fun Bitmap32.copyTo(other: Bitmap32): Bitmap32 = checkMatchDimensions(other).also { arraycopy(this.data, 0, other.data, 0, this.data.size) }
fun Bitmap32.inverted(target: Bitmap32 = Bitmap32(width, height)): Bitmap32 = copyTo(target).also { invert() }
//fun Bitmap32.colorTransform(target: ColorTransform = ColorTransform(width, height)): Bitmap32 = copyTo(target).also { invert() }

fun RGBA.transform(transform: ColorTransform) = RGBA(transform.applyToColor(this.value))
