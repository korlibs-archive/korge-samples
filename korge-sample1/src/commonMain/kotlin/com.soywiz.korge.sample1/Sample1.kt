package com.soywiz.korge.sample1

import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*

fun main(args: Array<String>) = Korge {
	val rect = solidRect(100, 100, Colors.RED)
	launchImmediately {
		while (true) {
			rect.tween(rect::x[100], time = 1.seconds, easing = Easing.EASE_IN_OUT_QUAD)
			rect.tween(rect::x[0], time = 1.seconds, easing = Easing.EASE_IN_OUT_QUAD)
		}
	}
}
