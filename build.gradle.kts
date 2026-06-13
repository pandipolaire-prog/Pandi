// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}

tasks.register("copyApk") {
    notCompatibleWithConfigurationCache("Accesses project/file structures which cannot be cached")
    doLast {
        val srcFile = file(".build-outputs/app-debug.apk")
        val destFile = file("Calculatrice.apk")
        if (srcFile.exists()) {
            srcFile.copyTo(destFile, overwrite = true)
            println("Successfully copied APK to workspace root as Calculatrice.apk")
        } else {
            // Check if there is another location (like app/build/outputs/apk/debug/app-debug.apk)
            val buildSrc = file("app/build/outputs/apk/debug/app-debug.apk")
            if (buildSrc.exists()) {
                buildSrc.copyTo(destFile, overwrite = true)
                println("Successfully copied APK from app/build layout to workspace root as Calculatrice.apk")
            } else {
                throw GradleException("No source APK found to copy!")
            }
        }
    }
}

