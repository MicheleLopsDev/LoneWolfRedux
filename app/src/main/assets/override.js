// File: src/main/assets/override.js

// ############# BLOCCO COMUNICAZIONE NATIVA #############
// (Invariato)
window.nativeCallbacks = {};
window.nativeCallbackId = 0;
function callNative(funcName, ...args) {
    return new Promise((resolve) => {
        const callbackId = window.nativeCallbackId++;
        window.nativeCallbacks[callbackId] = resolve;
        if (window.Android && typeof window.Android[funcName] === 'function') {
            window.Android[funcName](String(callbackId), ...args);
        } else {
            console.error("Funzione nativa (Android) non trovata: " + funcName);
            resolve(null);
        }
    });
}
window.nativeCallback = (callbackId, result) => {
    if (window.nativeCallbacks[callbackId]) {
        window.nativeCallbacks[callbackId](result);
        delete window.nativeCallbacks[callbackId];
    }
};

// ############# BLOCCO TRADUZIONE #############
// (Invariato)
window.translationCallbacks = {};
window.translationCallbackId = 0;
function callNativeTranslator(text) {
    return new Promise((resolve) => {
        if (!text || typeof text !== 'string' || text.trim() === '' || text.trim().startsWith('javascript:')) {
            resolve(text);
            return;
        }
        const callbackId = window.translationCallbackId++;
        window.translationCallbacks[callbackId] = resolve;
        if (window.Android && typeof window.Android.translate === 'function') {
            window.Android.translate(text, callbackId);
        } else {
            console.error("Funzione nativa 'Android.translate' non trovata.");
            resolve(text);
        }
    });
}
window.onTranslationResult = (translatedText, callbackId) => {
    if (window.translationCallbacks[callbackId]) {
        window.translationCallbacks[callbackId](translatedText);
        delete window.translationCallbacks[callbackId];
    }
};

// ############# LOGICA DI TRADUZIONE #############
// (Funzioni invariate)
async function translateNode(node) {
    if (!node || node.tagName === 'SCRIPT' || node.tagName === 'STYLE' || node.tagName === 'TEXTAREA') return;
    if (node.tagName === 'INPUT' && node.type === 'button' && node.value.trim().length > 0) {
        node.value = await callNativeTranslator(node.value);
    }
    if (node.nodeType === Node.TEXT_NODE && node.nodeValue.trim().length > 0) {
        node.nodeValue = await callNativeTranslator(node.nodeValue);
    }
    for (const child of node.childNodes) await translateNode(child);
}

async function translateTooltipData() {
    if (typeof window.Text === 'undefined' || !Array.isArray(window.Text)) return;
    for (let i = 0; i < window.Text.length; i++) {
        if (Array.isArray(window.Text[i]) && window.Text[i].length >= 2) {
            const [title, body] = await Promise.all([
                callNativeTranslator(window.Text[i][0]),
                callNativeTranslator(window.Text[i][1])
            ]);
            window.Text[i][0] = title;
            window.Text[i][1] = body;
        }
    }
}

async function startAllTranslations() {
    await Promise.all([translateTooltipData(), translateNode(document.body)]);
    console.log("TRADUZIONI COMPLETATE.");
}

// ############# GESTIONE DATI E AVVIO #############
// (Funzioni di supporto invariate)
function hideUiElements() {
    const optionForm = document.getElementById('optionForm');
    if (optionForm) optionForm.style.display = 'none';
    const backupForm = document.getElementById('backup');
    if (backupForm) backupForm.style.display = 'none';
}

function startApp() {
    hideUiElements();
    // La chiamata a loadAllData ora avverrà qui
    callNative('loadAllSheetData').then(jsonData => {
        if (jsonData && jsonData !== '{}') {
            const data = JSON.parse(jsonData);
            const form = document.actionChart;
            if (form) {
                for(let i=0; i < form.elements.length; i++) {
                    const field = form.elements[i];
                    if (field.name && data[field.name] !== undefined) {
                         if (field.type == 'checkbox') field.checked = (data[field.name] === 'true');
                         else field.value = data[field.name];
                    }
                }
                if (typeof findPercentage === 'function') findPercentage();
            }
        }
        startAllTranslations();
    });
}

// --- SOLUZIONE ROBUSTA: POLLING ---
// Controlla ripetutamente se le risorse della pagina sono pronte.
function initializeWhenReady() {
    const maxAttempts = 50; // Tenta per 5 secondi max (50 * 100ms)
    let attempt = 0;

    const checker = setInterval(() => {
        // La condizione chiave: la funzione loadAllData esiste?
        if (typeof window.loadAllData === 'function' && typeof window.Text !== 'undefined') {
            clearInterval(checker); // Ferma il controllo
            console.log('Dipendenze della pagina caricate. Avvio dello script di override.');
            startApp(); // Esegui la logica principale
        } else {
            attempt++;
            if (attempt > maxAttempts) {
                clearInterval(checker);
                console.error('Timeout: Le dipendenze della pagina (loadAllData) non si sono caricate in tempo.');
            }
        }
    }, 100); // Controlla ogni 100 millisecondi
}

// Avvia il controllore.
initializeWhenReady();