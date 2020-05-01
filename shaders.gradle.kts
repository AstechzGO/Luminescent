import org.lwjgl.util.shaderc.Shaderc
import org.gradle.api.InvalidUserCodeException

buildscript {
    val lwjglNatives = when {
        org.gradle.internal.os.OperatingSystem.current().isLinux -> System.getProperty("os.arch").let {
            if (it.startsWith("arm") || it.startsWith("aarch64"))
                "natives-linux-${if (it.contains("64") || it.startsWith("armv8")) "arm64" else "arm32"}"
            else
                "natives-linux"
        }
        org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "natives-macos"
        org.gradle.internal.os.OperatingSystem.current().isWindows -> if (System.getProperty("os.arch").contains("64")) "natives-windows" else "natives-windows-x86"
        else -> throw Error("Unrecognized or unsupported Operating system. Please set \"lwjglNatives\" manually")
    }

    dependencies {
        classpath(platform("org.lwjgl:lwjgl-bom:${project.extra["lwjglVersion"]}"))
        classpath("org.lwjgl", "lwjgl")
        classpath("org.lwjgl", "lwjgl", classifier = lwjglNatives)
        classpath("org.lwjgl", "lwjgl-shaderc")
        classpath("org.lwjgl", "lwjgl-shaderc", classifier = lwjglNatives)
    }

    repositories {
        mavenCentral()
    }
}

tasks {
    getByName<ProcessResources>("processResources") {
        outputs.upToDateWhen { false }
        eachFile {
            if(!isDirectory && name.length > 5 && relativePath.startsWith("resources/shaders") && relativePath.endsWith(".glsl", ignoreCase = true)) {
                val newPath = path.substring(0, path.length - 4) + "spv"
                val program = file.readText()
                exclude()
                val out = File(destinationDir, newPath)

                val compiler = Shaderc.shaderc_compiler_initialize()
                val options = Shaderc.shaderc_compile_options_initialize()
                Shaderc.shaderc_compile_options_set_warnings_as_errors(options)
                val result = Shaderc.shaderc_compile_into_spv(compiler, program, Shaderc.shaderc_glsl_infer_from_source, name, "main", options)

                if(Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
                    throw InvalidUserCodeException(Shaderc.shaderc_result_get_error_message(result) ?: "unknown error")
                }

                val output = Shaderc.shaderc_result_get_bytes(result)

                out.parentFile.mkdirs()
                if(out.exists()) out.delete()
                out.createNewFile()
                val fc = out.outputStream().channel
                fc.write(output)
                fc.close()

                Shaderc.shaderc_result_release(result)
                Shaderc.shaderc_compile_options_release(options)
                Shaderc.shaderc_compiler_release(compiler)
            }
        }
    }
}
