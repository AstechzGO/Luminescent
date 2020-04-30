import org.gradle.internal.os.OperatingSystem

// Apply the java plugin to add support for Java & add IntelliJ plugin
plugins {
	java
	idea
}

val lwjglVersion = "3.2.3"
val jomlVersion = "1.9.24"

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.google.code.gson:gson:2.8.6")
	implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

	implementation("org.lwjgl", "lwjgl")
	implementation("org.lwjgl", "lwjgl-glfw")
	implementation("org.lwjgl", "lwjgl-jemalloc")
	implementation("org.lwjgl", "lwjgl-openal")
	implementation("org.lwjgl", "lwjgl-stb")
	implementation("org.lwjgl", "lwjgl-vulkan")
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
	implementation("org.joml", "joml", jomlVersion)
}

// Don't add resources to JAR again
sourceSets {
	main {
		resources {
			exclude("*")
		}
	}
}

//create a single Jar with all dependencies
task<Jar>("deploy") {
	manifest {
		attributes(Pair("Main-Class", "astechzgo.luminescent.main.Main"))
	}

	from("src/main/resources"){
		into ("resources")
	}

	from({
		configurations.runtimeClasspath.get().map { if(it.isDirectory) it else zipTree(it) }
	})
	with(tasks["jar"] as CopySpec)
}.finalizedBy("copyToRoot")

// Copy to base directory
task("copyToRoot") {
	doLast {
		copy {
			from("build/libs/Luminescent.jar")
			into("/")
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
	gradleVersion = "6.3"
}
