package com.soywiz.korge.experimental.s3d.model.internal

import com.soywiz.korio.util.*

internal fun StrReader.readFloats(list: com.soywiz.kds.FloatArrayList = com.soywiz.kds.FloatArrayList(7)): com.soywiz.kds.FloatArrayList {
	while (!eof) {
		val pos0 = pos
		val float = skipSpaces().tryReadNumber().toFloat()
		skipSpaces()
		val pos1 = pos
		if (pos1 == pos0) error("Invalid number at $pos0 in '$str'")
		list.add(float)
		//println("float: $float, ${reader.pos}/${reader.length}")
	}
	return list
}

internal fun StrReader.readIds(list: ArrayList<String> = ArrayList(7)): ArrayList<String> {
	while (!eof) {
		val pos0 = pos
		val id = skipSpaces().tryReadId() ?: ""
		skipSpaces()
		val pos1 = pos
		if (pos1 == pos0) error("Invalid identifier at $pos0 in '$str'")
		list.add(id)
		//println("float: $float, ${reader.pos}/${reader.length}")
	}
	return list
}

internal fun StrReader.readInts(list: com.soywiz.kds.IntArrayList = com.soywiz.kds.IntArrayList(7)): com.soywiz.kds.IntArrayList {
	while (!eof) {
		val pos0 = pos
		val v = skipSpaces().tryReadInt(0)
		skipSpaces()
		val pos1 = pos
		if (pos1 == pos0) error("Invalid int at $pos0 in '$str'")
		list.add(v)
		//println("float: $float, ${reader.pos}/${reader.length}")
	}
	return list
}

internal fun StrReader.readVector3D():  com.soywiz.korma.geom.Vector3D {
	val f = readFloats(com.soywiz.kds.FloatArrayList())
	return when {
		f.size == 4 -> com.soywiz.korma.geom.Vector3D(f[0], f[1], f[2], f[3])
		else -> com.soywiz.korma.geom.Vector3D(f[0], f[1], f[2])
	}
}

//fun com.soywiz.korma.geom.Matrix3D.setFromColladaData(f: FloatArray, o: Int) = setColumns(
fun com.soywiz.korma.geom.Matrix3D.setFromColladaData(f: FloatArray, o: Int) = setRows(
	f[o + 0], f[o + 1], f[o + 2], f[o + 3],
	f[o + 4], f[o + 5], f[o + 6], f[o + 7],
	f[o + 8], f[o + 9], f[o + 10], f[o + 11],
	f[o + 12], f[o + 13], f[o + 14], f[o + 15]
)

internal fun StrReader.readMatrix3D(): com.soywiz.korma.geom.Matrix3D {
	val f = readFloats(com.soywiz.kds.FloatArrayList())
	if (f.size == 16) {
		//return com.soywiz.korma.geom.Matrix3D().setRows(
		return com.soywiz.korma.geom.Matrix3D().setFromColladaData(f.data, 0)
	} else {
		error("Invalid matrix size ${f.size} : str='$str'")
	}
}

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

internal fun StrReader.tryReadId(): String? {
	val start = pos
	skipWhile {
		@Suppress("ConvertTwoComparisonsToRangeCheck")
		(it >= '0' && it <= '9') || (it >= 'a' && it <= 'z') || (it >= 'A' && it <= 'Z') || (it == '_') || (it == '.')
	}
	val end = pos
	if (end == start) return null
	return this.str.substring(start, end)
}

// Allocation-free matching
internal fun StrReader.tryExpect(str: String): Boolean {
	for (n in 0 until str.length) {
		if (this.peekOffset(n) != str[n]) return false
	}
	skip(str.length)
	return true
}

internal fun StrReader.peekOffset(offset: Int = 0): Char = this.str.getOrElse(pos + offset) { '\u0000' }
