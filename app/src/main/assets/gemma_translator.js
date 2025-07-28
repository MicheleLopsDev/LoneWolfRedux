/**
 * gemma_translator.js
 */
alert("gemma_translator.js CARICATO!");
// LOG DI DEBUG: Se vedi questo nel Logcat (sotto il tag 'chromium'), lo script è stato caricato.
console.log("GemmaTranslator LOG: Script gemma_translator.js caricato ed eseguito.");

const STORY_CONTAINER_SELECTOR = 'div.numbered';

function extractStoryHtml() {
    const storyContainer = document.querySelector(STORY_CONTAINER_SELECTOR);
    if (storyContainer) {
        // Rimuoviamo gli script interni per sicurezza prima di inviare l'HTML
        const cleanHtml = storyContainer.cloneNode(true);
        Array.from(cleanHtml.querySelectorAll('script')).forEach(script => script.remove());
        return cleanHtml.innerHTML;
    } else {
        console.warn("GemmaTranslator: Contenitore storia '" + STORY_CONTAINER_SELECTOR + "' non trovato.");
        return "";
    }
}

function replaceStoryHtml(translatedHtml) {
    const storyContainer = document.querySelector(STORY_CONTAINER_SELECTOR);
    if (storyContainer) {
        storyContainer.innerHTML = translatedHtml;
    } else {
        console.error("GemmaTranslator: Contenitore '" + STORY_CONTAINER_SELECTOR + "' non trovato per inserire la traduzione.");
    }
}