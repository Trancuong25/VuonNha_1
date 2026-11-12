package com.example.vuonnha.ui.plants

internal fun lookupResultTitle(name: String, scientificName: String?): String =
    scientificName?.trim()?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
        ?.let { "$name · $it" } ?: name
