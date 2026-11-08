import groovy.json.JsonSlurper
import java.io.File
import java.io.Serializable
import org.gradle.api.Transformer

plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.wifiwidget.hilt.work)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.paparazzi)
}

android { buildFeatures { compose = true } }

val previewTestClass = "WifiGlancePaparazziPreviewTest"
val clearGlancePreviewReport = tasks.register<Delete>("clearGlancePreviewReport") {
    delete(layout.buildDirectory.dir("reports/paparazzi"))
}
val paparazziReport = layout.buildDirectory.dir("reports/paparazzi/debug").get().asFile
val exportGlancePreviews = tasks.register<Sync>("exportGlancePreviews") {
    group = "verification"
    description = "Renders device-independent Glance previews to PNG."
    // Sync must inspect the destination even when the rendered input PNGs are unchanged.
    outputs.upToDateWhen { false }
    dependsOn("testDebugUnitTest")
    from(paparazziReport.resolve("images")) {
        include("*.png")
        rename(PreviewImageRenamer(paparazziReport.resolve("runs")))
    }
    into(layout.buildDirectory.dir("generated/glance-previews"))
}

private class PreviewImageRenamer(private val runsDirectory: File) :
    Transformer<String?, String>,
    Serializable {
    @Transient private var previewNamesByImage: Map<String, String>? = null

    override fun transform(image: String): String {
        val names = previewNamesByImage ?: readPreviewNames().also { previewNamesByImage = it }
        return "${checkNotNull(names[image]) { "No named Paparazzi snapshot for $image" }}.png"
    }

    private fun readPreviewNames(): Map<String, String> =
        runsDirectory.listFiles()
            .orEmpty()
            .filter { it.extension == "js" }
            .flatMap { run ->
                val snapshots = JsonSlurper().parseText(run.readText().substringAfter(" = ").trim().removeSuffix(";")) as List<*>
                snapshots.map { snapshot ->
                    val entry = snapshot as Map<*, *>
                    val name = checkNotNull(entry["name"] as? String) { "Paparazzi snapshot has no preview name: $run" }
                    val filename = (entry["file"] as String).substringAfterLast('/')
                    filename to name
                }
            }
            .toMap()
}

tasks.withType<Test>().matching { it.name == "testDebugUnitTest" }.configureEach {
    if (gradle.startParameter.taskNames.any { it.substringAfterLast(':') == exportGlancePreviews.name }) {
        dependsOn(clearGlancePreviewReport)
        filter { includeTestsMatching("com.w2sv.widget.ui.$previewTestClass") }
    } else {
        exclude("**/$previewTestClass.class")
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.resources)
    implementation(projects.core.domain)

    implementation(libs.google.android.material)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.kotlinutils.core)
    implementation(libs.slimber)
    implementation(libs.androidx.glance.appwidget)

    debugImplementation(libs.androidx.glance.appwidget.preview)
    debugImplementation(libs.androidx.glance.preview)

    testImplementation(libs.bundles.unitTest)
    testImplementation(libs.androidx.glance.appwidget.testing)
    testImplementation(libs.androidx.glance.testing)
    testImplementation(libs.paparazzi)
}
