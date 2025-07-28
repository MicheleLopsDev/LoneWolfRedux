/**
 * gemma_translator.js
 * Traduzione narrativa paragrafo per paragrafo.
 */
console.log("GemmaTranslator LOG: Script v2 caricato.");

const CONTAINER_SELECTOR = 'div.numbered, div.frontmatter div.maintext';
const PARAGRAPH_SELECTOR = 'p, h2, h3'; // Selettori per i paragrafi e i titoli da tradurre

/**
 * Identifica tutti i paragrafi traducibili, assegna loro un ID unico
 * e invia i loro contenuti a Kotlin per la traduzione.
 */
function extractAndTranslateParagraphs() {
    const container = document.querySelector(CONTAINER_SELECTOR);
    if (!container) {
        console.warn("GemmaTranslator: Nessun contenitore di testo trovato.");
        return;
    }

    const paragraphs = Array.from(container.querySelectorAll(PARAGRAPH_SELECTOR));
    let paragraphsToTranslate = [];

    paragraphs.forEach((p, index) => {
        const paragraphId = `gemma-trans-${index}`;
        p.id = paragraphId; // Assegna un ID univoco all'elemento
        paragraphsToTranslate.push({
            id: paragraphId,
            html: p.innerHTML
        });
    });

    if (paragraphsToTranslate.length > 0) {
        console.log(`GemmaTranslator: Trovati ${paragraphsToTranslate.length} paragrafi da tradurre.`);
        if (window.GemmaTranslator) {
            // Invia l'intero array di oggetti a Kotlin in formato JSON
            window.GemmaTranslator.translateParagraphs(JSON.stringify(paragraphsToTranslate));
        }
    }
}

/**
 * Sostituisce l'innerHTML di un singolo paragrafo identificato dal suo ID.
 * @param {string} paragraphId L'ID dell'elemento <p> da aggiornare.
 * @param {string} translatedHtml L'HTML tradotto.
 */
function replaceSingleParagraph(paragraphId, translatedHtml) {
    const element = document.getElementById(paragraphId);
    if (element) {
        element.innerHTML = translatedHtml;
    } else {
        console.error(`GemmaTranslator: Elemento con ID '${paragraphId}' non trovato.`);
    }
}