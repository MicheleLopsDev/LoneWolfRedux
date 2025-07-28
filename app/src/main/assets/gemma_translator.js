/**
 * gemma_translator.js
 *
 * Script specializzato per la traduzione narrativa con Gemma.
 * Gestisce l'estrazione e la sostituzione di interi blocchi HTML
 * per le sezioni di storia del librogame.
 */

const STORY_CONTAINER_SELECTOR = 'div.numbered';

/**
 * Estrae l'intero contenuto HTML del contenitore della storia.
 * Questo preserva tutti i tag, inclusi i link delle scelte.
 *
 * @returns {string} L'innerHTML del contenitore della storia, o una stringa vuota se non trovato.
 */
function extractStoryHtml() {
    const storyContainer = document.querySelector(STORY_CONTAINER_SELECTOR);
    if (storyContainer) {
        console.log("GemmaTranslator: Contenitore storia trovato. Estrazione HTML in corso...");
        return storyContainer.innerHTML;
    } else {
        console.warn("GemmaTranslator: Contenitore storia '" + STORY_CONTAINER_SELECTOR + "' non trovato nella pagina.");
        return "";
    }
}

/**
 * Sostituisce il contenuto del contenitore della storia con l'HTML tradotto.
 *
 * @param {string} translatedHtml La stringa HTML tradotta ricevuta da Kotlin.
 */
function replaceStoryHtml(translatedHtml) {
    const storyContainer = document.querySelector(STORY_CONTAINER_SELECTOR);
    if (storyContainer) {
        console.log("GemmaTranslator: Contenitore storia trovato. Inserimento HTML tradotto...");
        storyContainer.innerHTML = translatedHtml;
    } else {
        console.error("GemmaTranslator: Impossibile trovare il contenitore '" + STORY_CONTAINER_SELECTOR + "' per inserire la traduzione.");
    }
}