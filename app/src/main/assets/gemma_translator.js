/**
 * gemma_translator.js v5 - Con pulizia di sicurezza del testo.
 */
console.log("GemmaTranslator LOG: Script v5 (con pulizia sicurezza) caricato.");

const CONTAINER_SELECTOR = 'div.numbered, div.frontmatter div.maintext';
const PARAGRAPH_SELECTOR = 'p, h2, h3';

/**
 * Inietta lo stile per la clessidra nel documento.
 */
function addIndicatorStyles() {
    if (document.getElementById('gemma-loader-style')) return;

    const style = document.createElement('style');
    style.id = 'gemma-loader-style';
    style.innerHTML = `
        #gemma-loader {
            position: fixed;
            top: 70px;
            left: 16px;
            font-size: 40px;
            opacity: 0.7;
            z-index: 9999;
            pointer-events: none;
            display: none;
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
 * Mostra l'indicatore di caricamento.
 */
function showLoadingIndicator() {
    let loader = document.getElementById('gemma-loader');
    if (!loader) {
        addIndicatorStyles();
        loader = document.createElement('div');
        loader.id = 'gemma-loader';
        loader.innerHTML = '⏳';
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
 * Funzione di avvio: estrae i paragrafi da tradurre.
 */
function extractAndTranslateParagraphs() {
    const container = document.querySelector(CONTAINER_SELECTOR);
    if (!container) {
        console.warn("GemmaTranslator WARN: Contenitore non trovato.");
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
        hideLoadingIndicator(); // Nascondi se non c'è nulla da tradurre
    }
}

/**
 * Funzione di supporto per aggiornare un singolo elemento con pulizia di sicurezza.
 */
function replaceSingleParagraph(paragraphId, translatedHtml) {
    const element = document.getElementById(paragraphId);
    if (element) {
        // --- MECCANISMO DI SICUREZZA AGGIUNTO QUI ---
        // Rimuove globalmente ```html e ``` dalla stringa prima di usarla.
        const cleanedHtml = translatedHtml.replace(/```html|```/g, '');

        element.innerHTML = cleanedHtml; // Usa la stringa pulita
    } else {
        console.error(`GemmaTranslator ERROR: Elemento con ID '${paragraphId}' non trovato.`);
    }
}

/**
 * Funzione principale di sostituzione batch.
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
        hideLoadingIndicator(); // Nascondi sempre la clessidra alla fine
    }
}