package com.soywiz.korge.experimental.s3d.model.internal

import com.soywiz.korio.util.*
import kotlin.math.*

internal fun StrReader.skipSpaces2() = this.apply { this.skipWhile { it == ' ' || it == '\t' || it == '\n' || it == '\r' } }

internal fun StrReader.tryReadInt(default: Int): Int {
	var digitCount = 0
	var integral = 0
	var mult = 1
	loop@ while (!eof) {
		when (val c = peek()) {
			'-' -> {
				skip(1)
				mult *= -1
			}
			in '0'..'9' -> {
				val digit = c - '0'
				skip(1)
				digitCount++
				integral *= 10
				integral += digit
			}
			else -> {
				break@loop
			}
		}
	}
	return if (digitCount == 0) default else integral
}

internal fun StrReader.tryReadNumber(default: Double = Double.NaN): Double {
	val start = pos
	skipWhile {
		@Suppress("ConvertTwoComparisonsToRangeCheck")
		(it >= '0' && it <= '9') || (it == '+') || (it == '-') || (it == 'e') || (it == 'E') || (it == '.')
	}
	val end = pos
	if (end == start) return default
	return NumberParser.parseDouble(this.str, start, end)
}

// Allocation-free matching
internal fun StrReader.tryExpect2(str: String): Boolean {
	for (n in 0 until str.length) {
		if (this.peekOffset(n) != str[n]) return false
	}
	skip(str.length)
	return true
}

internal fun StrReader.peekOffset(offset: Int = 0): Char = this.str.getOrElse(pos + offset) { '\u0000' }
