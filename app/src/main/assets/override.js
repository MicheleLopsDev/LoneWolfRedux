// File: src/main/assets/override.js

// Sistema di callback asincrono per comunicare con il codice nativo Kotlin.
window.nativeCallbacks = {};
window.nativeCallbackId = 0;
function callNative(funcName, ...args) {
    return new Promise((resolve) => {
        const callbackId = window.nativeCallbackId++;
        window.nativeCallbacks[callbackId] = resolve;
        if (window.Android && typeof window.Android[funcName] === 'function') {
            window.Android[funcName](String(callbackId), ...args);
        } else {
            console.error("Funzione nativa non trovata: " + funcName);
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

/**
 * Sovrascrive la funzione loadAllData() originale di palwac.js.
 */
function loadAllData() {
    console.log("JS: Richiesta dati al codice nativo...");
    callNative('loadAllSheetData').then(jsonData => {
        if (!jsonData || jsonData === '{}') {
            console.log("JS: Nessun dato ricevuto o dati vuoti.");
            return;
        }
        console.log("JS: Dati ricevuti, popolo la scheda.");
        const data = JSON.parse(jsonData);
        const form = document.actionChart;
        if (!form) return;

        for(let i=0; i < form.elements.length; i++) {
            const field = form.elements[i];
            const savedValue = data[field.name];
            if (savedValue !== undefined) {
                if (field.type == 'checkbox') {
                    field.checked = (savedValue === 'true');
                } else {
                    field.value = savedValue;
                }
            }
        }
        if (typeof findPercentage === 'function') {
            findPercentage();
        }
    });
};

/**
 * Nasconde elementi dell'interfaccia non necessari per semplificare la UI.
 * Cerca gli elementi per ID e imposta il loro stile su 'display: none'.
 * Il controllo `if (element)` previene errori se un elemento non esiste in una data pagina.
 */
function hideUiElements() {
    console.log("JS: Nascondo elementi UI non necessari...");

    const optionForm = document.getElementById('optionForm');
    if (optionForm) {
        optionForm.style.display = 'none';
    }

    const backupForm = document.getElementById('backup');
    if (backupForm) {
        backupForm.style.display = 'none';
    }
}


// Avvia l'intero processo all'avvio della pagina
loadAllData();
hideUiElements();