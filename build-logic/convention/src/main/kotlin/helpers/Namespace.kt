package helpers

internal sealed interface Namespace {
    object Auto : Namespace

    @JvmInline
    value class Manual(val namespace: String) : Namespace

    fun get(path: String): String = when (this) {
        is Auto -> "com.w2sv." + path
            .removePrefix(":")
            .replace(':', '.')
            .replace('-', '_')  // Sets namespace to "com.w2sv.<module-name>"
        is Manual -> namespace
    }
}
