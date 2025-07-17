// File: src/main/assets/translator.js

// Oggetto per memorizzare le callback in attesa di una risposta dal codice nativo.
window.translationCallbacks = {};
window.translationCallbackId = 0;

/**
 * Funzione per chiamare un metodo nativo e ricevere una risposta tramite Promise.
 * @param {string} text - Il testo da tradurre.
 * @returns {Promise<string>} Una Promise che si risolve con il testo tradotto.
 */
function callNativeTranslator(text) {
    return new Promise((resolve) => {
        const callbackId = window.translationCallbackId++;
        window.translationCallbacks[callbackId] = resolve;
        if (window.Translator && typeof window.Translator.translate === 'function') {
            window.Translator.translate(text, callbackId);
        } else {
            console.error("Funzione nativa 'Translator.translate' non trovata.");
            resolve(text); // Ritorna il testo originale in caso di errore
        }
    });
}

/**
 * Funzione globale che il codice Kotlin chiama per restituire la traduzione.
 * @param {string} translatedText - Il testo tradotto.
 * @param {number} callbackId - L'ID della callback da risolvere.
 */
window.onTranslationResult = (translatedText, callbackId) => {
    if (window.translationCallbacks[callbackId]) {
        window.translationCallbacks[callbackId](translatedText);
        delete window.translationCallbacks[callbackId];
    }
};

/**
 * Attraversa ricorsivamente tutti i nodi del DOM a partire da un elemento dato,
 * identifica i nodi di testo e li sostituisce con la loro traduzione.
 * @param {Node} node - Il nodo da cui iniziare la traversata.
 */
async function translateNode(node) {
    // Ignora gli script e gli stili per performance e per evitare bug
    if (node.tagName === 'SCRIPT' || node.tagName === 'STYLE') {
        return;
    }

    // Se è un nodo di testo e non è vuoto, traducilo
    if (node.nodeType === Node.TEXT_NODE && node.nodeValue.trim().length > 0) {
        const originalText = node.nodeValue;
        const translatedText = await callNativeTranslator(originalText);
        node.nodeValue = translatedText;
    }

    // Continua la traversata sui figli del nodo
    for (const child of node.childNodes) {
        await translateNode(child);
    }
}

/**
 * Funzione principale che avvia il processo di traduzione sull'intero body del documento.
 */
function startTranslation() {
    console.log("Avvio traduzione della pagina...");
    // Inizia la traduzione dal body del documento per coprire tutto il contenuto visibile
    translateNode(document.body).then(() => {
        console.log("Traduzione della pagina completata.");
    });
}

// Avvia la traduzione non appena lo script viene caricato.
startTranslation();