/**
 * gemma_translator.js v2
 * Traduzione narrativa paragrafo per paragrafo.
 */
console.log("GemmaTranslator LOG: Script v2 caricato.");

const CONTAINER_SELECTOR = 'div.numbered, div.frontmatter div.maintext';
const PARAGRAPH_SELECTOR = 'p, h2, h3';

/**
 * Identifica, FILTRA e invia i paragrafi traducibili a Kotlin.
 */
function extractAndTranslateParagraphs() {
    const container = document.querySelector(CONTAINER_SELECTOR);
    if (!container) {
        console.warn("GemmaTranslator: Nessun contenitore di testo trovato.");
        return;
    }

    const elements = Array.from(container.querySelectorAll(PARAGRAPH_SELECTOR));
    let paragraphsToTranslate = [];

    elements.forEach((el, index) => {
        const paragraphId = `gemma-trans-${index}`;
        el.id = paragraphId;

        const htmlContent = el.innerHTML.trim();

        // --- MODIFICA CHIAVE: Logica di filtraggio ---
        // Ignora il paragrafo se è vuoto o se contiene solo un numero.
        // La regex /^\d+$/.test() controlla se la stringa è composta solo da cifre.
        if (htmlContent.length > 0 && !/^\d+$/.test(htmlContent)) {
            paragraphsToTranslate.push({
                id: paragraphId,
                html: htmlContent
            });
        } else {
            console.log(`GemmaTranslator: Paragrafo con ID ${paragraphId} ignorato (vuoto o solo numerico).`);
        }
    });

    if (paragraphsToTranslate.length > 0 && window.GemmaTranslator) {
        console.log(`GemmaTranslator: Trovati ${paragraphsToTranslate.length} paragrafi validi da tradurre.`);
        window.GemmaTranslator.translateParagraphs(JSON.stringify(paragraphsToTranslate));
    }
}

/**
 * Sostituisce l'innerHTML di un singolo elemento identificato dal suo ID.
 */
function replaceSingleParagraph(paragraphId, translatedHtml) {
    const element = document.getElementById(paragraphId);
    if (element) {
        element.innerHTML = translatedHtml;
    } else {
        console.error(`GemmaTranslator: Elemento con ID '${paragraphId}' non trovato.`);
    }
}