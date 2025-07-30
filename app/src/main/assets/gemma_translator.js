/**
 * gemma_translator.js v4 - Soluzione con Indicatore di Caricamento
 */
console.log("GemmaTranslator LOG: Script v4 (con clessidra) caricato.");

const CONTAINER_SELECTOR = 'div.numbered, div.frontmatter div.maintext';
const PARAGRAPH_SELECTOR = 'p, h2, h3';

/**
 * Inietta lo stile per la clessidra nel documento.
 * Viene eseguito una sola volta.
 */
function addIndicatorStyles() {
    if (document.getElementById('gemma-loader-style')) return; // Stile già presente

    const style = document.createElement('style');
    style.id = 'gemma-loader-style';
    style.innerHTML = `
        #gemma-loader {
            position: fixed;
            top: 70px; /* Sotto la barra dei pulsanti */
            left: 16px;
            font-size: 40px; /* Dimensione dell'icona */
            opacity: 0.7;
            z-index: 9999;
            pointer-events: none; /* Non intercettare i click */
            display: none; /* Nascosto di default */
            animation: spin 2s linear infinite;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    `;
    document.head.appendChild(style);
}

/**
 * Mostra l'indicatore di caricamento (clessidra).
 * Lo crea se non esiste.
 */
function showLoadingIndicator() {
    let loader = document.getElementById('gemma-loader');
    if (!loader) {
        addIndicatorStyles(); // Assicura che gli stili siano presenti
        loader = document.createElement('div');
        loader.id = 'gemma-loader';
        loader.innerHTML = '⏳'; // Icona della clessidra
        document.body.appendChild(loader);
    }
    loader.style.display = 'block';
    console.log("GemmaTranslator LOG: Indicatore di caricamento MOSTRATO.");
}

/**
 * Nasconde l'indicatore di caricamento.
 */
function hideLoadingIndicator() {
    const loader = document.getElementById('gemma-loader');
    if (loader) {
        loader.style.display = 'none';
        console.log("GemmaTranslator LOG: Indicatore di caricamento NASCOSTO.");
    }
}

/**
 * Funzione di avvio: identifica, filtra e invia i paragrafi a Kotlin.
 */
function extractAndTranslateParagraphs() {
    showLoadingIndicator(); // Mostra la clessidra all'inizio del processo

    const container = document.querySelector(CONTAINER_SELECTOR);
    if (!container) {
        console.warn("GemmaTranslator WARN: Contenitore principale del testo non trovato.");
        hideLoadingIndicator(); // Nascondi se non c'è nulla da fare
        return;
    }

    const elements = Array.from(container.querySelectorAll(PARAGRAPH_SELECTOR));
    let paragraphsToTranslate = [];

    elements.forEach((el, index) => {
        const paragraphId = `gemma-trans-${index}`;
        el.id = paragraphId;
        const htmlContent = el.innerHTML.trim();
        if (htmlContent.length > 0 && !/^\d+$/.test(htmlContent)) {
            paragraphsToTranslate.push({ id: paragraphId, html: htmlContent });
        }
    });

    if (paragraphsToTranslate.length > 0 && window.GemmaTranslator) {
        console.log(`GemmaTranslator LOG: Invio di ${paragraphsToTranslate.length} paragrafi a Kotlin.`);
        window.GemmaTranslator.translateParagraphs(JSON.stringify(paragraphsToTranslate));
    } else {
        hideLoadingIndicator(); // Nascondi se non ci sono paragrafi validi
    }
}

/**
 * Funzione di supporto per aggiornare un singolo elemento.
 */
function replaceSingleParagraph(paragraphId, translatedHtml) {
    const element = document.getElementById(paragraphId);
    if (element) {
        element.innerHTML = translatedHtml;
    } else {
        console.error(`GemmaTranslator ERROR: Elemento con ID '${paragraphId}' non trovato.`);
    }
}

/**
 * Funzione principale di sostituzione: riceve il batch JSON da Kotlin.
 */
function replaceBatchParagraphs(translationsJson) {
    try {
        const translations = JSON.parse(translationsJson);
        console.log(`GemmaTranslator LOG: Ricevuto batch di ${Object.keys(translations).length} traduzioni.`);

        for (const paragraphId in translations) {
            if (translations.hasOwnProperty(paragraphId)) {
                replaceSingleParagraph(paragraphId, translations[paragraphId]);
            }
        }
        console.log("GemmaTranslator LOG: Sostituzione batch completata.");
    } catch (e) {
        console.error("GemmaTranslator ERROR: Errore parsing JSON.", e);
    } finally {
        hideLoadingIndicator(); // Nascondi la clessidra alla fine, anche in caso di errore
    }
}