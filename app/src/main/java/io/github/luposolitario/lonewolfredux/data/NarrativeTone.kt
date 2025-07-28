package io.github.luposolitario.lonewolfredux.data

// Usiamo una sealed class per rappresentare i toni in modo sicuro e strutturato.
sealed class NarrativeTone(val key: String, val displayName: String) {
    object Neutro : NarrativeTone("NEUTRO", "Neutro")
    object Epico : NarrativeTone("EPICO", "Epico")
    object Horror : NarrativeTone("HORROR", "Horror")
    object Ironico : NarrativeTone("IRONICO", "Ironico")

    companion object {
        val allTones = listOf(Neutro, Epico, Horror, Ironico)

        /**
         * MODIFICATO: La funzione ora accetta una Stringa nullabile (String?)
         * per prevenire crash se il valore da DataStore è null o non ancora impostato.
         */
        fun fromKey(key: String?): NarrativeTone {
            // Se la chiave è nulla o vuota, ritorna il tono di default (Neutro).
            if (key.isNullOrBlank()) {
                return Neutro
            }
            return allTones.find { it.key == key } ?: Neutro
        }
    }
}