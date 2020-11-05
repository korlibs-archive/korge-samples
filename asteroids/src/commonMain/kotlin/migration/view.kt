package com.soywiz.korge.view

import com.soywiz.klock.TimeSpan
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korio.lang.Cancellable
import com.soywiz.korio.resources.Resourceable
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.vector.VectorPath

@Deprecated(
	message = "Need migrate to addUpdater()",
	replaceWith = ReplaceWith("addUpdater(updatable)"),
	level = DeprecationLevel.ERROR
)
inline fun <T : View> T.addHrUpdater(updatable: T.(dt: TimeSpan) -> Unit): Cancellable {
	throw Error("migrate")
}

@Deprecated(
	message = "Need migrate to rotation",
	replaceWith = ReplaceWith("rotation"),
	level = DeprecationLevel.ERROR
)
inline var View.rotationDegrees: Angle
	get() {
		throw Error("migrate")
	}
	set(v) {
		throw Error("migrate")
	}


//todo unfortunately it not work, because class Image resolve first
@Deprecated(
	message = "Need migrate to BaseImage",
	replaceWith = ReplaceWith("BaseImage(bitmap, anchorX, anchorY, hitShape, hitShape, smoothing)"),
	level = DeprecationLevel.ERROR
)
inline fun Image(
	bitmap: Resourceable<out BmpSlice>,
	anchorX: Double = 0.0,
	anchorY: Double = anchorX,
	hitShape: VectorPath? = null,
	smoothing: Boolean = true
) : Image {
	throw Error("migrate")
}
