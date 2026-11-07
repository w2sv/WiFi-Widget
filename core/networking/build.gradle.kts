plugins {
    alias(libs.plugins.wifiwidget.library)
    alias(libs.plugins.wifiwidget.hilt)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.resources)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.kotlinutils.core)
    implementation(libs.w2sv.kotlinutils.coroutines)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.slimber)

    implementation(libs.squareup.okhttp3)
    implementation(libs.squareup.okhttp3.coroutines)

    testImplementation(libs.bundles.unitTest)
}

tasks.withType<Test>().configureEach {
    // Robolectric's API 36 sandbox accesses jdk.internal.access.SharedSecrets while
    // creating Android's ApplicationSharedMemory. Export the package to the test JVM;
    // otherwise setup fails before tests run with a FileDescriptor internals error.
    jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
}
