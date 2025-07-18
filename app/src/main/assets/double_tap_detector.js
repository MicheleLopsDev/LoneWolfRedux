// Questo script andrebbe in un file .js (es. double_tap_detector.js)
// che poi caricherai via getJsFromAssets
(function() {
    var lastTap = 0;
    var timeout;
    var DBL_TAP_THRESHOLD = 300; // Millisecondi

    document.addEventListener('touchend', function(event) {
        var currentTime = new Date().getTime();
        var tapLength = currentTime - lastTap;

        clearTimeout(timeout); // Cancella qualsiasi tap singolo in sospeso

        if (tapLength < DBL_TAP_THRESHOLD && tapLength > 0) {
            // Doppio tap rilevato
            console.log("Double tap detected!");
            if (window.AndroidTap && typeof window.AndroidTap.onDoubleTapDetected === 'function') {
                window.AndroidTap.onDoubleTapDetected();
            }
            lastTap = 0; // Reset per evitare tripli tap accidentali
        } else {
            // Primo tap, imposta un timer per considerarlo un tap singolo
            timeout = setTimeout(function() {
                // Qui potresti gestire il tap singolo se necessario
            }, DBL_TAP_THRESHOLD);
            lastTap = currentTime;
        }
    }, false);

    // Prevenire il comportamento predefinito del doppio tap del browser (es. zoom)
    document.addEventListener('touchstart', function(event) {
        if (event.touches.length === 2) { // Se due dita, potrebbe essere il pinch-to-zoom
            event.preventDefault(); // Previene il pinch-to-zoom nativo del browser
        }
    }, { passive: false }); // Usa passive: false per permettere preventDefault
})();