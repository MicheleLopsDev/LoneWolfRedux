/**
 * gemma_translator.js v3 - Soluzione Batch Definitiva
 * Contiene tutte le funzioni per estrarre e sostituire il testo.
 */
console.log("GemmaTranslator LOG: Script v3 (batch) caricato.");

const CONTAINER_SELECTOR = 'div.numbered, div.frontmatter div.maintext';
const PARAGRAPH_SELECTOR = 'p, h2, h3';

/**
 * Funzione di avvio: identifica, filtra e invia i paragrafi a Kotlin.
 * Viene chiamata da GameScreen.kt dopo il caricamento della pagina.
 */
function extractAndTranslateParagraphs() {
    const container = document.querySelector(CONTAINER_SELECTOR);
    if (!container) {
        console.warn("GemmaTranslator WARN: Contenitore principale del testo non trovato.");
        return;
    }

    const elements = Array.from(container.querySelectorAll(PARAGRAPH_SELECTOR));
    let paragraphsToTranslate = [];

    elements.forEach((el, index) => {
        const paragraphId = `gemma-trans-${index}`;
        el.id = paragraphId;

        const htmlContent = el.innerHTML.trim();
        if (htmlContent.length > 0 && !/^\d+$/.test(htmlContent)) {
            paragraphsToTranslate.push({
                id: paragraphId,
                html: htmlContent
            });
        }
    });

    if (paragraphsToTranslate.length > 0 && window.GemmaTranslator) {
        console.log(`GemmaTranslator LOG: Invio di ${paragraphsToTranslate.length} paragrafi a Kotlin per la traduzione batch.`);
        window.GemmaTranslator.translateParagraphs(JSON.stringify(paragraphsToTranslate));
    }
}

/**
 * Funzione di supporto usata da replaceBatchParagraphs per aggiornare un singolo elemento.
 */
function replaceSingleParagraph(paragraphId, translatedHtml) {
    const element = document.getElementById(paragraphId);
    if (element) {
        element.innerHTML = translatedHtml;
    } else {
        console.error(`GemmaTranslator ERROR: Elemento con ID '${paragraphId}' non trovato durante la sostituzione.`);
    }
}

/**
 * Funzione principale di sostituzione: riceve il batch JSON da Kotlin
 * e aggiorna tutti i paragrafi in un unico ciclo.
 */
function replaceBatchParagraphs(translationsJson) {
    try {
        // Logghiamo la stringa *prima* del parsing per vedere esattamente cosa riceve
        // console.log("GemmaTranslator DEBUG: JSON ricevuto:", translationsJson);

        const translations = JSON.parse(translationsJson);

        console.log(`GemmaTranslator LOG: [replaceBatch] Ricevuto batch di ${Object.keys(translations).length} traduzioni da Kotlin.`);

        for (const paragraphId in translations) {
            if (translations.hasOwnProperty(paragraphId)) {
                // Riutilizziamo la funzione singola che già funziona e ha i log corretti
                replaceSingleParagraph(paragraphId, translations[paragraphId]);
            }
        }
        console.log("GemmaTranslator LOG: [replaceBatch] Sostituzione batch completata.");
    } catch (e) {
        console.error("GemmaTranslator ERROR: Errore grave durante il parsing del JSON.", e);
        // In caso di errore, logghiamo anche la stringa che ha causato il problema
        console.error("GemmaTranslator DEBUG: JSON fallito:", translationsJson);
    }
}