package io.github.luposolitario.lonewolfredux.data

// Usiamo una sealed class per rappresentare i toni in modo sicuro e strutturato.
sealed class NarrativeTone(val key: String, val displayName: String) {
    object Neutro : NarrativeTone("NEUTRO", "Neutro")
    object Epico : NarrativeTone("EPICO", "Epico")
    object Horror : NarrativeTone("HORROR", "Horror")
    object Ironico : NarrativeTone("IRONICO", "Ironico")

    companion object {
        val allTones = listOf(Neutro, Epico, Horror, Ironico)

        fun fromKey(key: String): NarrativeTone {
            return allTones.find { it.key == key } ?: Neutro
        }
    }
}