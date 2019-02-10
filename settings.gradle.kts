for (file in rootDir.listFiles()) {
	if (file.isDirectory && file.name.startsWith("sample-") && (File(file, "build.gradle").exists() || File(file, "build.gradle.kts").exists())) {
		include(":${file.name}")
	}
}
