import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

// Applied by build.gradle.kts after the Android test module and managed devices are configured.
// Capture and Play listing publication share this one task pipeline.

data class ScreenshotTarget(
    val deviceTaskName: String,
    val displayName: String,
    val directoryName: String,
    val outputRoot: Provider<Directory>
)

val intermediateOutputs =
    layout.buildDirectory.dir("intermediates/managed_device_android_test_additional_output/debug")

val publishedOutputs =
    layout.buildDirectory.dir("outputs/managed_device_android_test_additional_output/debug")

// AGP currently retains Pixel 6 and Nexus 7 screenshots in intermediates,
// while Pixel Tablet screenshots are available in the published outputs.
val screenshotTargets = listOf(
    ScreenshotTarget(
        deviceTaskName = "storePhoneApi35",
        displayName = "phone",
        directoryName = "phone-screenshots",
        outputRoot = intermediateOutputs
    ),
    ScreenshotTarget(
        deviceTaskName = "storeTabletApi35",
        displayName = "7-inch tablet",
        directoryName = "tablet-screenshots",
        outputRoot = intermediateOutputs
    ),
    ScreenshotTarget(
        deviceTaskName = "storeLargeTabletApi35",
        displayName = "10-inch tablet",
        directoryName = "large-tablet-screenshots",
        outputRoot = publishedOutputs
    )
)

val playGraphics = rootProject.layout.projectDirectory.dir("app/src/main/play/listings/en-US/graphics")

// The numeric prefixes preserve the intended pair order in Play listings and file browsers.
val expectedScreenshots = listOf(
    "01-widget-network-overview.png",
    "02-app-home.png",
    "03-widget-customized.png",
    "04-app-appearance.png",
    "05-widget-transparent.png",
    "06-app-properties.png"
)

// Filtered pipelines validate only their own subset, so the other test's captures are not required.
val widgetScreenshots = expectedScreenshots.filter { "-widget-" in it }
val inAppScreenshots = expectedScreenshots.filter { "-app-" in it }
val widgetTestClass = "com.w2sv.wifiwidget.storescreenshots.WidgetLauncherScreenshotTest"
val inAppTestClass = "com.w2sv.wifiwidget.storescreenshots.InAppScreenshotTest"
val instrumentationClassProperty = "android.testInstrumentationRunnerArguments.class"

fun String.toPascalCase(): String =
    split('-').joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }

abstract class ValidateStoreScreenshots : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val screenshotFiles: ConfigurableFileCollection

    @get:Input
    abstract val expectedFilenames: ListProperty<String>

    @get:Input
    abstract val categoryName: Property<String>

    @TaskAction
    fun validate() {
        logger.quiet("Screenshot Gen: validating ${categoryName.get()}...")

        val generated = screenshotFiles.files.map { it.name }.toSet()
        val missing = expectedFilenames.get().toSet() - generated

        if (missing.isNotEmpty()) {
            throw GradleException(
                "Missing generated ${categoryName.get()} screenshots: $missing"
            )
        }
    }
}

abstract class ValidateScreenshotClassFilter : DefaultTask() {
    @get:Input
    abstract val mode: Property<String>

    @get:Input
    @get:Optional
    abstract val expectedClassFilter: Property<String>

    @get:Input
    @get:Optional
    abstract val actualClassFilter: Property<String>

    @TaskAction
    fun validate() {
        val expected = expectedClassFilter.orNull
        val actual = actualClassFilter.orNull
        if (actual != expected) {
            val requirement = expected?.let { "-Pandroid.testInstrumentationRunnerArguments.class=$it" }
                ?: "the instrumentation class filter to be absent"
            throw GradleException("${mode.get()} screenshot generation requires $requirement; found ${actual ?: "no filter"}.")
        }
    }
}

// Missing properties remain unset inputs, so the full pipeline distinguishes an absent
// class filter from any literal property value. No Project API is read by the task action.
fun registerClassFilterValidation(mode: String, expectedClass: String?): TaskProvider<ValidateScreenshotClassFilter> =
    tasks.register<ValidateScreenshotClassFilter>("validate${mode}ScreenshotClassFilter") {
        group = "verification"
        description = "Checks the instrumentation class filter for $mode screenshot generation."
        this.mode.set(mode)
        expectedClass?.let(expectedClassFilter::set)
        actualClassFilter.set(providers.gradleProperty(instrumentationClassProperty))
    }

val fullClassFilterValidation = registerClassFilterValidation("Full", null)
val widgetClassFilterValidation = registerClassFilterValidation("Widget", widgetTestClass)
val inAppClassFilterValidation = registerClassFilterValidation("InApp", inAppTestClass)
val classFilterValidations = listOf(fullClassFilterValidation, widgetClassFilterValidation, inAppClassFilterValidation)

// Each pipeline selects its own captured filenames while sharing validation and Play listing copying.
fun registerScreenshotPipeline(
    target: ScreenshotTarget,
    taskPrefix: String,
    captureTask: Any,
    classFilterValidation: TaskProvider<ValidateScreenshotClassFilter>,
    filenames: List<String>
): TaskProvider<Copy> {
    val taskSuffix = target.directoryName.toPascalCase()

    val validationTask = tasks.register<ValidateStoreScreenshots>("validate$taskPrefix$taskSuffix") {
        group = "verification"
        description = "Validates generated ${target.displayName} screenshots."

        dependsOn(captureTask, classFilterValidation)

        categoryName.set(target.directoryName)
        expectedFilenames.set(filenames)

        // Search only the selected filenames in this category's AGP output root. Each
        // pipeline can therefore validate the same capture task independently.
        screenshotFiles.from(
            fileTree(target.outputRoot.get().asFile) {
                filenames.forEach { include("**/${target.directoryName}/$it") }
            }
        )
    }

    return tasks.register<Copy>("generate$taskPrefix$taskSuffix") {
        group = "publishing"
        description = "Generates ${target.displayName} screenshots and copies them to the Play Store listing."

        dependsOn(validationTask)

        // Keep the explicit screenshot order rather than depending on AGP's output traversal.
        filenames.forEach { filename ->
            from(target.outputRoot) {
                include("**/${target.directoryName}/$filename")
                eachFile { path = name }
                includeEmptyDirs = false
            }
        }

        into(playGraphics.dir(target.directoryName))

        val message = "Screenshot Gen: copying ${target.directoryName} to the Play listing..."
        doFirst { logger.quiet(message) }
    }
}

// Normal managed-device execution runs both independent screenshot tests.
val generateScreenshotTasks = screenshotTargets.associate { target ->
    target.deviceTaskName to registerScreenshotPipeline(
        target = target,
        taskPrefix = "",
        captureTask = "${target.deviceTaskName}DebugAndroidTest",
        classFilterValidation = fullClassFilterValidation,
        filenames = expectedScreenshots
    )
}

// AGP's instrumentation class argument is build-wide. Each filtered pipeline uses the
// same managed-device task with its own class argument supplied on that invocation.
val generateWidgetScreenshotTasks = screenshotTargets.associate { target ->
    target.deviceTaskName to registerScreenshotPipeline(
        target = target,
        taskPrefix = "Widget",
        captureTask = "${target.deviceTaskName}DebugAndroidTest",
        classFilterValidation = widgetClassFilterValidation,
        filenames = widgetScreenshots
    )
}

val generateInAppScreenshotTasks = screenshotTargets.associate { target ->
    target.deviceTaskName to registerScreenshotPipeline(
        target = target,
        taskPrefix = "InApp",
        captureTask = "${target.deviceTaskName}DebugAndroidTest",
        classFilterValidation = inAppClassFilterValidation,
        filenames = inAppScreenshots
    )
}

// A pipeline schedules only its own filter validation. These ordering rules do not add
// dependencies to shared AGP tasks; they run the selected check before emulator setup/capture.
tasks.configureEach {
    if (screenshotTargets.any { name == "${it.deviceTaskName}Setup" || name == "${it.deviceTaskName}DebugAndroidTest" }) {
        mustRunAfter(classFilterValidations)
    }
}

// AGP shares managed-device screenshot staging. Finish each device's capture,
// validation, and copy before starting the next device's capture. Setups may run in parallel.
//
// mustRunAfter only establishes ordering when both tasks are scheduled;
// standalone device generation tasks remain independent.
screenshotTargets.zipWithNext().forEach { (previous, next) ->
    val previousGeneration = generateScreenshotTasks.getValue(previous.deviceTaskName)
    val previousWidgetGeneration = generateWidgetScreenshotTasks.getValue(previous.deviceTaskName)
    val previousInAppGeneration = generateInAppScreenshotTasks.getValue(previous.deviceTaskName)

    tasks.configureEach {
        if (name == "${next.deviceTaskName}DebugAndroidTest") {
            mustRunAfter(previousGeneration)
            mustRunAfter(previousWidgetGeneration)
            mustRunAfter(previousInAppGeneration)
        }
    }
}

// AGP owns these tasks; configure only progress messages and aggregate execution order.
tasks.configureEach {
    screenshotTargets.forEach { target ->
        val message = when (name) {
            "${target.deviceTaskName}Setup" ->
                "Screenshot Gen: preparing ${target.displayName} emulator..."

            "${target.deviceTaskName}DebugAndroidTest" ->
                "Screenshot Gen: capturing ${target.displayName}..."

            else -> null
        }

        if (message != null) {
            doFirst { logger.quiet(message) }
        }
    }
}

// The mustRunAfter relationships above serialize captures when these aggregate tasks are used.
tasks.register("generateScreenshots") {
    group = "publishing"
    description = "Generates Play Store screenshots for all supported device categories."

    dependsOn(generateScreenshotTasks.values)

    doLast {
        logger.quiet("Screenshot Gen: generation complete.")
    }
}

tasks.register("generateInAppScreenshots") {
    group = "publishing"
    description = "Generates the three paired in-app screenshots for every device category."
    dependsOn(generateInAppScreenshotTasks.values)
    doLast { logger.quiet("Screenshot Gen: in-app generation complete.") }
}

tasks.register("generateWidgetScreenshots") {
    group = "publishing"
    description = "Generates the three launcher widget screenshots for every device category."
    dependsOn(generateWidgetScreenshotTasks.values)
    doLast { logger.quiet("Screenshot Gen: widget generation complete.") }
}
