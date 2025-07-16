// Questo script intercetta le chiamate di salvataggio/caricamento della scheda
// e le reindirizza al nostro codice Kotlin.

window.nativeCallbacks = {};
window.nativeCallbackId = 0;

function callNative(funcName, ...args) {
    return new Promise((resolve) => {
        const callbackId = window.nativeCallbackId++;
        window.nativeCallbacks[callbackId] = resolve;
        if (window.Android && typeof window.Android[funcName] === 'function') {
            window.Android[funcName](callbackId.toString(), ...args);
        } else {
            console.error("Native function " + funcName + " not found!");
            resolve('{}');
        }
    });
}

window.nativeCallback = (callbackId, result) => {
    if (window.nativeCallbacks[callbackId]) {
        window.nativeCallbacks[callbackId](result);
        delete window.nativeCallbacks[callbackId];
    }
};

$.jStorage.set = function(key, value) {
    if (window.Android && typeof window.Android.saveData === 'function') {
        window.Android.saveData(key, String(value));
    }
    return value;
};

loadAllData = function() {
    callNative('loadAllChartData').then(jsonData => {
        if (!jsonData || jsonData === '{}') return;
        const data = JSON.parse(jsonData);
        for(let i=0; i < document['actionChart'].elements.length; i++) {
            const field = document['actionChart'].elements[i];
            if (field.name && data[field.name] !== undefined) {
                const savedValue = data[field.name];
                if (field.type == 'checkbox') {
                    field.checked = (savedValue === 'true');
                } else {
                    field.value = savedValue;
                }
            }
        }
        if(typeof findPercentage === 'function') findPercentage();
    });
};

// Eseguiamo subito il caricamento
loadAllData();