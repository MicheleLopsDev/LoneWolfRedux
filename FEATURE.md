Bentornato dalle vacanze\! È normale aver bisogno di un riepilogo per riprendere il filo del discorso.

Ecco il piano d'azione che abbiamo definito, spiegato in modo più specifico e dettagliato, esattamente come richiesto. Questo è il nostro progetto tecnico, suddiviso in fasi logiche di implementazione.


### **Fase 2: L'Estrattore Intelligente (Logica JavaScript)**

**Obiettivo**: Delegare al JavaScript tutto il lavoro "sporco" di analisi dell'HTML, lasciando il Kotlin pulito.

* **2.1. Creazione del file gemma\_translator.js**:  
  * Questo nuovo script verrà iniettato e attivato dalla WebView **solo** quando la traduzione avanzata è in funzione.  
* **2.2. Logica di Estrazione del Testo**:  
  * Lo script eseguirà una funzione principale che, per prima cosa, analizza l'URL della pagina corrente.  
  * **Caso A: Pagina di Storia (sect\*.htm)**:  
    1. Lo script individua l'elemento \<div class="numbered"\>.  
    2. Estrae l'**intero contenuto HTML** di questo div in una singola stringa. Questo preserverà tutti i tag (\<p\>, \<a\>, \<i\>, ecc.) e garantirà che i link delle scelte (href="\#sect...") non vengano persi.  
    3. Invia questa stringa HTML completa all'interfaccia Kotlin.  
  * **Caso B: Scheda Personaggio (char\_sheet\*.htm, tssf.htm)**:  
    1. Lo script non estrae un blocco unico. Itera invece su specifici elementi (es. th, td, h2) che contengono testo da tradurre.  
    2. Per ogni elemento, estrae il testo, lo invia a Kotlin per una traduzione **letterale**, riceve la risposta e sostituisce il testo nell'elemento corrispondente.  
  * **Caso C: Pagine Speciali (map.htm, license.htm, ecc.)**:  
    1. Lo script riconosce l'URL e semplicemente non fa nulla.

---

### **Fase 3: Il Motore di Traduzione (Logica Kotlin)**

**Obiettivo**: Gestire la cronologia, costruire il prompt perfetto e orchestrare la traduzione.

* **3.1. Il Custode della Storia (GameViewModel)**:  
  * Il GameViewModel avrà una nuova variabile, ad esempio private var storyContext: String \= "".  
  * Questa variabile conterrà il testo HTML completo della **sezione di storia precedente**.  
* **3.2. L'Intercettore di Navigazione (WebViewClient)**:  
  * All'interno della GameActivity, configureremo un WebViewClient personalizzato per la BookWebView.  
  * Sovrascriveremo il metodo shouldOverrideUrlLoading. Ogni volta che l'utente clicca un link, questo metodo verrà invocato.  
  * **Logica dell'intercettore**:  
    1. Controlla l'URL di destinazione.  
    2. **Se** l'URL è una nuova sezione di storia (contiene sect\*.htm), **prima** di procedere con la navigazione, lo script JavaScript (gemma\_translator.js) verrà eseguito per estrarre il testo HTML della pagina **attuale**. Questo testo verrà passato al GameViewModel, che lo salverà nella sua variabile storyContext.  
    3. Dopodiché, la navigazione verso la nuova pagina viene consentita.  
* **3.3. Il Traduttore Avanzato (GemmaTranslationManager)**:  
  * Il suo metodo principale sarà suspend fun translateNarrative(currentHtml: String, historyHtml: String): String.  
  * **Costruzione del Prompt**: Questo è il cuore del sistema. Il metodo:  
    1. Leggerà il **Tono Narrativo** scelto dall'utente dalle impostazioni.  
    2. Assemblerà un prompt complesso che passerà a Gemma, strutturato così:"Traduci il seguente testo HTML mantenendo la struttura dei tag. Applica uno stile \[TONO SCELTO\]. Assicurati che la traduzione sia coerente con il contesto fornito.  
       \[CONTESTO STORIA PRECEDENTE\]  
       {historyHtml}  
       \[TESTO HTML DA TRADURRE\]  
       {currentHtml}"  
  * Eseguirà l'inferenza e restituirà la stringa HTML tradotta.

---

### **Fase 4: Il Ritorno e la Sostituzione**

**Obiettivo**: Visualizzare il risultato finale senza rompere la pagina.

* **4.1. Callback a JavaScript**:  
  * Il ViewModel, una volta ricevuta la stringa HTML tradotta da Gemma, la invierà indietro alla WebView tramite una funzione di callback JavaScript.  
* **4.2. Sostituzione del Contenuto**:  
  * Il file gemma\_translator.js riceverà l'HTML tradotto.  
  * Il suo ultimo compito sarà quello di sostituire l'intero innerHTML del \<div class="numbered"\> con la nuova stringa HTML ricevuta. Poiché abbiamo preservato i tag, la pagina manterrà la sua formattazione e, soprattutto, i link delle scelte funzioneranno perfettamente.

Questo piano dettagliato ci fornisce una roadmap chiara per ogni componente, assicurando che ogni pezzo del puzzle si incastri nel modo giusto.