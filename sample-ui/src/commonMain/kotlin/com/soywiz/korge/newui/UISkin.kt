package com.soywiz.korge.newui

import com.soywiz.kds.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*

@PublishedApi
internal var View.internalDefaultUiSkin: UISkin? by extraProperty("defaultUiSkin") { null }
var View.defaultUiSkin: UISkin
	set(value) = run { internalDefaultUiSkin = value }
	get() = internalDefaultUiSkin ?: parent?.defaultUiSkin ?: DefaultUISkin

fun Container.uiSkin(skin: UISkin, block: Container.() -> Unit) {
	defaultUiSkin = skin
	block()
}

open class UISkin(
	val normal: BmpSlice,
	val hover: BmpSlice,
	val down: BmpSlice,
	val backColor: RGBA = Colors.DARKGREY
)
