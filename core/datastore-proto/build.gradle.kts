plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.google.protobuf)
}

// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc { artifact = libs.google.protobuf.protoc.get().toString() }
    generateProtoTasks {
        all().configureEach {
            builtins {
                named("java") { option("lite") }
                register("kotlin") { option("lite") }
            }
        }
    }
}

dependencies {
    api(libs.google.protobuf.kotlin.lite)
}
