package com.soywiz.klock

@Deprecated(
	message = "Need migrate to milliseconds",
	replaceWith = ReplaceWith("milliseconds"),
	level = DeprecationLevel.ERROR)

inline val Int.hrMilliseconds: TimeSpan get() {
	throw Error("migrate")
}
