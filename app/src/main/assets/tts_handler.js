// In: main/assets/tts_handler.js

(function() {
    // Stile per il nostro pulsante "Leggi" fluttuante
    const style = document.createElement('style');
    style.innerHTML = `
        #tts-button {
            position: absolute;
            background-color: #333;
            color: white;
            border: 1px solid #555;
            border-radius: 8px;
            padding: 8px 12px;
            font-family: sans-serif;
            font-size: 14px;
            cursor: pointer;
            z-index: 9999;
            box-shadow: 0 2px 5px rgba(0,0,0,0.5);
        }
    `;
    document.head.appendChild(style);

    let ttsButton = null;

    // Funzione per mostrare il pulsante
    function showTtsButton() {
        const selection = window.getSelection();
        if (selection.isCollapsed || selection.rangeCount === 0) {
            hideTtsButton();
            return;
        }

        if (!ttsButton) {
            ttsButton = document.createElement('button');
            ttsButton.id = 'tts-button';
            ttsButton.innerText = 'Leggi';
            document.body.appendChild(ttsButton);

            ttsButton.addEventListener('click', function() {
                const selectedText = window.getSelection().toString();
                if (selectedText.trim().length > 0) {
                    // Chiama l'interfaccia nativa che abbiamo definito
                    if (window.TtsHandler && typeof window.TtsHandler.speak === 'function') {
                        window.TtsHandler.speak(selectedText);
                    }
                }
                hideTtsButton();
            });
        }

        const range = selection.getRangeAt(0);
        const rect = range.getBoundingClientRect();

        // Posiziona il pulsante sopra la selezione
        ttsButton.style.top = (window.scrollY + rect.top - ttsButton.offsetHeight - 10) + 'px';
        ttsButton.style.left = (window.scrollX + rect.left + (rect.width / 2) - (ttsButton.offsetWidth / 2)) + 'px';
        ttsButton.style.display = 'block';
    }

    // Funzione per nascondere il pulsante
    function hideTtsButton() {
        if (ttsButton) {
            ttsButton.style.display = 'none';
        }
    }

    // Mostra il pulsante quando l'utente finisce di selezionare il testo
    document.addEventListener('touchend', function() {
        // Un piccolo ritardo per assicurarsi che la selezione sia finalizzata
        setTimeout(showTtsButton, 10);
    });

    // Nasconde il pulsante se la selezione cambia o scompare
    document.addEventListener('selectionchange', function() {
         const selection = window.getSelection();
         if (selection.isCollapsed) {
             hideTtsButton();
         }
    });

})();