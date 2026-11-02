package com.w2sv.domain.wifiproperty

/**
 * User-defined names for access points, keyed by normalized BSSID (lowercase, colon-separated).
 *
 * Serializable to/from a plain text format with one `bssid|name` entry per line; blank lines and lines starting with `#` are ignored.
 * The same format is meant to be shared with other apps, e.g. to label the access points of a multi-AP (roaming) setup.
 */
data class AccessPointNames(val map: Map<String, String> = emptyMap()) {

    val isEmpty: Boolean
        get() = map.isEmpty()

    /**
     * @return the name assigned to [bssid], or null if there is none.
     */
    fun nameOf(bssid: String?): String? =
        bssid?.let(::normalizeBssid)?.let(map::get)

    /**
     * @return a copy with [name] assigned to [bssid]. A blank [name] removes the assignment.
     * @throws IllegalArgumentException if [bssid] is not a valid MAC-48 address.
     */
    fun with(bssid: String, name: String): AccessPointNames {
        val key = requireNotNull(normalizeBssid(bssid)) { "Invalid BSSID: $bssid" }
        val trimmedName = name.trim()
        return AccessPointNames(
            if (trimmedName.isEmpty()) map - key else map + (key to trimmedName)
        )
    }

    fun without(bssid: String): AccessPointNames =
        normalizeBssid(bssid)?.let { AccessPointNames(map - it) } ?: this

    /**
     * @return a copy containing the entries of both, the ones of [other] taking precedence.
     */
    fun mergedWith(other: AccessPointNames): AccessPointNames =
        AccessPointNames(map + other.map)

    fun toFileContent(): String =
        map.entries
            .sortedBy { it.key }
            .joinToString(separator = "") { (bssid, name) -> "$bssid$SEPARATOR$name\n" }

    companion object {
        const val SEPARATOR = '|'

        private val bssidRegex = Regex("^[0-9a-f]{2}([:-][0-9a-f]{2}){5}$")

        /**
         * @return [bssid] in lowercase, colon-separated form, or null if it is not a valid MAC-48 address.
         */
        fun normalizeBssid(bssid: String): String? =
            bssid
                .trim()
                .lowercase()
                .takeIf { bssidRegex.matches(it) }
                ?.replace('-', ':')

        /**
         * Parses the `bssid|name` format, skipping malformed lines.
         */
        fun parse(text: String): AccessPointNames =
            AccessPointNames(
                text
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith('#') }
                    .mapNotNull { line ->
                        val bssid = normalizeBssid(line.substringBefore(SEPARATOR, missingDelimiterValue = ""))
                        val name = line.substringAfter(SEPARATOR, missingDelimiterValue = "").trim()
                        if (bssid != null && name.isNotEmpty()) bssid to name else null
                    }
                    .toMap()
            )
    }
}
