// Apply the java plugin to add support for Java & add IntelliJ plugin
plugins {
	java
	idea
}

buildscript {
	extra["lwjglVersion"] = "3.2.3"
	extra["jomlVersion"] = "1.10.1"
	extra["gsonVersion"] = "2.8.8"
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.google.code.gson:gson:${project.extra["gsonVersion"]}")
	implementation(platform("org.lwjgl:lwjgl-bom:${project.extra["lwjglVersion"]}"))

	implementation("org.lwjgl", "lwjgl")
	implementation("org.lwjgl", "lwjgl-glfw")
	implementation("org.lwjgl", "lwjgl-jemalloc")
	implementation("org.lwjgl", "lwjgl-openal")
	implementation("org.lwjgl", "lwjgl-stb")
	implementation("org.lwjgl", "lwjgl-vulkan")
	implementation("org.lwjgl", "lwjgl-vma")
	runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-jemalloc", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl-jemalloc", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl-jemalloc", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-vulkan", classifier = "natives-macos")
	runtimeOnly("org.lwjgl", "lwjgl-vma", classifier = "natives-linux")
	runtimeOnly("org.lwjgl", "lwjgl-vma", classifier = "natives-windows")
	runtimeOnly("org.lwjgl", "lwjgl-vma", classifier = "natives-macos")

	implementation("org.joml", "joml", project.extra["jomlVersion"] as String)
}

//create a single Jar with all dependencies
task<Jar>("deploy") {
	manifest {
		attributes(Pair("Main-Class", "astechzgo.luminescent.main.Main"))
	}

	from({
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		configurations.runtimeClasspath.get().map { if(it.isDirectory) it else zipTree(it) }
	})
	with(tasks["jar"] as CopySpec)
}.finalizedBy("copyToRoot")

// Don't add resources to JAR again
sourceSets {
	main {
		resources {
			exclude("*")
		}
	}
}

tasks.processResources {
	from("src/main/resources") {
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		into ("resources")
	}
}

apply(from = "shaders.gradle.kts")

// Copy to base directory
task("copyToRoot") {
	doLast {
		copy {
			duplicatesStrategy = DuplicatesStrategy.EXCLUDE
			from("build/libs/Luminescent.jar")
			into(".")
		}
	}
}

task("run") {
	doLast {
		javaexec {
			main="-jar"
			args = listOf("Luminescent.jar")
		}
	}
}.dependsOn("deploy")

tasks.compileJava {
	sourceCompatibility = "11"
	targetCompatibility = "11"
}

tasks.wrapper {
	gradleVersion = "7.2"
}
