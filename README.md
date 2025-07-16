# **Diario di Sviluppo: LoneWolfRedux**

Versione: 8.0 (Versione Definitiva e Consolidata)  
Data: 16 luglio 2025  
Autore: Michele & Gemini

### **1\. Visione del Progetto**

**LoneWolfRedux** è un'applicazione nativa per Android che funge da "contenitore" avanzato per i librogame della serie "Lupo Solitario". Il progetto mira a offrire un'esperienza di lettura e di gioco superiore, offline-first, e altamente personalizzabile, incapsulando i contenuti web originali in un'interfaccia moderna, fluida e reattiva, potenziata da funzionalità avanzate.

* **Nome Progetto**: LoneWolfRedux  
* **Piattaforma Target**: Android  
* **SDK di Riferimento**: targetSdk e compileSdk versione 34 (Android 14).

### **2\. Architettura Generale**

* **UI Toolkit**: **Jetpack Compose**.  
* **Architettura**: **MVVM (Model-View-ViewModel)**.  
* **Navigazione**: L'app utilizzerà un'architettura **Multi-Activity**. La navigazione tra le schermate principali avverrà tramite Intent.  
* **Asincronicità**: **Coroutine Kotlin** e **WorkManager**.  
* **Gestione Dati**: **Jetpack Proto DataStore** sarà l'unica fonte di verità per tutti i dati persistenti.

### **3\. Struttura delle Schermate e Flusso Utente**

L'app sarà suddivisa in quattro Activity principali. Il flusso utente è sequenziale e chiaro.

#### **3.1. MainActivity e MainViewModel**

* **Scopo**: Funge da punto di ingresso e menu principale dell'applicazione.  
* **UI (MainMenuScreen)**: Una schermata componibile con pulsanti di navigazione per:  
  * **Libreria (DownloadManagerActivity)**: L'opzione principale per scaricare e giocare i libri.  
  * **Gestione IA (LlmManagerActivity)**: Per gestire i modelli IA.  
  * **Impostazioni (ConfigurationActivity)**: Per le impostazioni globali.  
* **Logica (MainViewModel)**: Gestisce la navigazione verso le altre Activity. **Il flusso di gioco principale inizia quando l'utente seleziona "Libreria" e avvia la DownloadManagerActivity**.

#### **3.2. DownloadManagerActivity e DownloadManagerViewModel**

* **Scopo**: **È la schermata principale per la gestione e la selezione dei libri.** L'utente sceglie qui quale libro scaricare e quale partita iniziare.  
* **UI (DownloadManagerScreen)**: Una LazyColumn che mostra la lista di tutti i 29 libri, **raggruppati per serie** con icone specifiche. Ogni libro mostrerà il suo stato (Non Scaricato, In Download, Scaricato) con i relativi controlli (Scarica, Gioca, Elimina).  
* **Logica (DownloadManagerViewModel)**: Gestisce la lista dei libri e avvia i DownloadWorker. **Quando l'utente preme "Gioca", questa Activity lancia un Intent per avviare la GameActivity, passando l'ID del libro scelto come extra.**

#### **3.3. GameActivity e GameViewModel**

* **Scopo**: È il cuore dell'esperienza di gioco, **avviata sempre con il contesto di un libro specifico.**  
* **UI (GameScreen)**:  
  * Un ModalNavigationDrawer con il cassetto che si apre da destra.  
  * **Contenuto Principale**: Una WebView (BookWebView), sempre visibile, per la narrazione del libro.  
  * **Contenuto del Cassetto**: Una seconda WebView (SheetWebView) per la Scheda Azione interattiva.  
  * Una TopAppBar nativa con un pulsante per chiudere il libro e un'icona per aprire/chiudere il cassetto della scheda.  
* **Logica (GameViewModel)**:  
  * **Gestione Stato**: **All'avvio, riceve l'ID del libro dall'Intent** inviato dalla DownloadManagerActivity. Usa questo ID per determinare quale char\_sheet\_XXX.htm caricare e per recuperare/creare la sessione di gioco corretta da DataStore.  
  * **Caricamento WebView**: **Carica l'HTML pulito e non parsato**. Usa loadDataWithBaseURL per caricare i contenuti del libro (dalla memoria interna) e della scheda (dagli assets).  
  * **Persistenza**: Fornisce l'oggetto SheetInterface alla SheetWebView per intercettare le chiamate JS e reindirizzarle a DataStore. Salva l'URL di ogni paragrafo visitato per gestire lo storico di navigazione.

#### **3.3.1. Funzionalità di Navigazione Avanzata e Segnalibri**

Per migliorare l'esperienza di gioco, la GameScreen includerà controlli di navigazione avanzati.

* **UI**: La TopAppBar (o una barra di navigazione dedicata) conterrà i seguenti IconButton:  
  * **Home**: Un'icona per tornare istantaneamente al primo paragrafo del libro.  
  * **Indietro (Back)**: Un'icona per tornare al paragrafo visitato immediatamente prima di quello attuale.  
  * **Segnalibro (Star)**: Un'icona a forma di stella che funge da interruttore (argento/oro).  
  * **Vai al Segnalibro**: Un'icona che naviga istantaneamente al paragrafo salvato.  
* **Logica (DataStore)**: Manterrà una **lista di URL** per lo storico di navigazione e un campo dedicato per l'URL del segnalibro.

#### **3.4. LlmManagerActivity e LlmManagerViewModel**

* **Scopo**: Fornire un'interfaccia dedicata per la gestione del modello di linguaggio Gemma 3\.  
* **UI (LlmManagerScreen)**: Interfaccia per inserire il token di Hugging Face, vedere la lista dei modelli IA e monitorare i download.  
* **Logica (LlmManagerViewModel)**: Salva il token in DataStore e gestisce i DownloadWorker per i modelli.

#### **3.5. ConfigurationActivity e ConfigurationViewModel**

* **Scopo**: Gestire tutte le impostazioni globali dell'app.  
* **UI (ConfigurationScreen)**: Schermata di impostazioni standard.  
* **Funzionalità**:  
  * **Selezione Motore di Traduzione**: Scelta tra "Veloce (Offline)" e "Avanzata (Locale)". **L'opzione avanzata sarà disabilitata di default**.  
  * **Selezione Tono Narrativo**: Menu per scegliere lo stile dell'IA (per Gemma 3).  
  * **Dimensione Font**: Controlla il textZoom delle WebView.  
  * **Impostazioni TTS**: Configurazione della sintesi vocale.  
  * **Backup/Ripristino**: Pulsanti per esportare/importare lo stato del gioco.  
  * **Azioni Partita**: Pulsanti per "Concludi Libro Corrente" e "Reset Totale".  
* **Logica (ConfigurationViewModel)**: Legge e scrive tutte le preferenze nel DataStore.

### **4\. Motori di Servizio e Logica di Business**

#### **4.1. Motori di Traduzione**

Regola Fondamentale per la Traduzione:  
Il GameViewModel sceglierà il motore appropriato in base alle impostazioni dell'utente e al tipo di pagina.

* **Se** l'opzione "Traduzione Avanzata" è attiva **E** il nome del file è sectxxx.htm o tssf.htm, verrà usato il motore **Gemma 3**.  
* **In tutti gli altri casi**, verrà usato il motore di default **ML Kit**.

**Opzione 1: Traduzione Veloce (Default \- Offline con ML Kit)**

* **Meccanismo**: Usato per default e per tutte le pagine non narrative (es. char\_sheet\_xxx.htm). Simula una traduzione completa della pagina.  
  1. La pagina viene caricata in inglese nella WebView.  
  2. Uno **script JavaScript**, iniettato dall'app, attraversa l'intero DOM della pagina, identifica tutti i nodi di testo visibili e ne invia il contenuto al TranslationEngine nativo.  
  3. Il motore nativo traduce le stringhe e le restituisce al JavaScript.  
  4. Lo script JS aggiorna il contenuto dei singoli nodi di testo con le traduzioni, **preservando l'intera struttura HTML e la funzionalità della pagina**.  
* **Risultato**: L'utente vede l'intera pagina tradotta. **Nessun parsing dell'HTML avviene in Kotlin**.

**Opzione 2: Traduzione Avanzata (Opzionale \- Locale con Gemma 3\)**

* **Meccanismo**: Usato **solo per le pagine narrative**. **Il parsing è demandato al JavaScript**.  
  1. La pagina viene caricata in inglese.  
  2. Uno **script JS** estrae l'**intero blocco HTML** del contenuto narrativo.  
  3. Lo script passa questo blocco HTML al GameViewModel.  
  4. Il GameViewModel invoca il GemmaEngine locale con un prompt specifico: "Traduci in italiano solo il testo visibile all'interno di questo blocco HTML. Non modificare in alcun modo i tag, gli attributi come 'href' o 'class', o la struttura generale. Restituisci l'intero blocco HTML con il solo testo tradotto."  
  5. Il GemmaEngine restituisce l'HTML tradotto, che viene usato dallo script JS per sostituire il contenuto del div originale.

#### **4.2. Logica della SheetWebView (Interception Layer)**

Questa è la strategia chiave per riutilizzare il codice JavaScript della Scheda Azione **senza modificarlo**.

1. **SheetInterface.kt (Kotlin)**: Il ponte tra JS e Kotlin.  
   class SheetInterface(private val viewModel: GameViewModel) {  
       @JavascriptInterface  
       fun saveData(key: String, value: String) {  
           viewModel.saveSheetData(key, value) // Salva su DataStore  
       }

       @JavascriptInterface  
       fun loadAllChartData(callbackId: String) {  
           viewModel.viewModelScope.launch {  
               val allData: Map\<String, String\> \= viewModel.getAllSheetData() // Legge da DataStore  
               val jsonString \= Gson().toJson(allData)  
               val script \= "window.nativeCallback($callbackId, '$jsonString');"  
               viewModel.runJsInSheetView(script) // Esegue la callback in JS  
           }  
       }  
   }

2. **override.js (JavaScript)**: Lo script completo da iniettare nella SheetWebView.  
   // Sistema di Promise e Callback per la comunicazione asincrona  
   window.nativeCallbacks \= {};  
   window.nativeCallbackId \= 0;  
   function callNative(funcName, ...args) {  
       return new Promise((resolve) \=\> {  
           const callbackId \= window.nativeCallbackId++;  
           window.nativeCallbacks\[callbackId\] \= resolve;  
           if (window.Android && typeof window.Android\[funcName\] \=== 'function') {  
               window.Android\[funcName\](callbackId, ...args);  
           }  
       });  
   }  
   window.nativeCallback \= (callbackId, result) \=\> {  
       if (window.nativeCallbacks\[callbackId\]) {  
           window.nativeCallbacks\[callbackId\](result);  
           delete window.nativeCallbacks\[callbackId\];  
       }  
   };

   // Override delle funzioni di jStorage e palwac  
   $.jStorage.set \= function(key, value) {  
       if (window.Android && typeof window.Android.saveData \=== 'function') {  
           window.Android.saveData(key, String(value));  
       }  
       return value;  
   };

   loadAllData \= function() {  
       callNative('loadAllChartData').then(jsonData \=\> {  
           if (\!jsonData || jsonData \=== '{}') return;  
           const data \= JSON.parse(jsonData);  
           for(let i=0; i \< document\['actionChart'\].elements.length; i++) {  
               const field \= document\['actionChart'\].elements\[i\];  
               const savedValue \= data\[field.name\];  
               if (savedValue \!== undefined) {  
                   if (field.type \== 'checkbox') {  
                       field.checked \= (savedValue \=== 'true');  
                   } else {  
                       field.value \= savedValue;  
                   }  
               }  
           }  
           findPercentage();  
       });  
   };

   loadAllData();

### **5\. Gestione dei Dati (Proto DataStore)**

* **Fonte di Verità Unica**: Utilizzeremo **Proto DataStore** per la sua type safety.  
* **Schema Dati**: Verrà definito un file .proto per strutturare la Sessione di gioco. Questo includerà lo stato del personaggio, l'inventario, le impostazioni e i nuovi campi per la navigazione: repeated string navigation\_history e string bookmarked\_paragraph\_url.  
* **Backup e Ripristino**: Le funzioni di backup serializzeranno l'oggetto Sessione dal DataStore in un file esportabile.

### **Appendice A: Link per il Download dei Libri**

#### **Kai**

* [Flight from the Dark](https://www.projectaon.org/en/xhtml/lw/01fftd/01fftd.zip)  
* [Fire on the Water](https://www.projectaon.org/en/xhtml/lw/02fotw/02fotw.zip)  
* [The Caverns of Kalte](https://www.projectaon.org/en/xhtml/lw/03tcok/03tcok.zip)  
* [The Chasm of Doom](https://www.projectaon.org/en/xhtml/lw/04tcod/04tcod.zip)  
* [Shadow on the Sand](https://www.projectaon.org/en/xhtml/lw/05sots/05sots.zip)

#### **Magnakai**

* [The Kingdoms of Terror](https://www.projectaon.org/en/xhtml/lw/06tkot/06tkot.zip)  
* [Castle Death](https://www.projectaon.org/en/xhtml/lw/07cd/07cd.zip)  
* [The Jungle of Horrors](https://www.projectaon.org/en/xhtml/lw/08tjoh/08tjoh.zip)  
* [The Cauldron of Fear](https://www.projectaon.org/en/xhtml/lw/09tcof/09tcof.zip)  
* [The Dungeons of Torgar](https://www.projectaon.org/en/xhtml/lw/10tdot/10tdot.zip)  
* [The Prisoners of Time](https://www.projectaon.org/en/xhtml/lw/11tpot/11tpot.zip)  
* [The Masters of Darkness](https://www.projectaon.org/en/xhtml/lw/12tmod/12tmod.zip)

#### **Grand Master**

* [The Plague Lords of Ruel](https://www.projectaon.org/en/xhtml/lw/13tplor/13tplor.zip)  
* [The Captives of Kaag](https://www.projectaon.org/en/xhtml/lw/14tcok/14tcok.zip)  
* [The Darke Crusade](https://www.projectaon.org/en/xhtml/lw/15tdc/15tdc.zip)  
* [The Legacy of Vashna](https://www.projectaon.org/en/xhtml/lw/16tlov/16tlov.zip)  
* [The Deathlord of Ixia](https://www.projectaon.org/en/xhtml/lw/17tdoi/17tdoi.zip)  
* [Dawn of the Dragons](https://www.projectaon.org/en/xhtml/lw/18dotd/18dotd.zip)  
* [Wolf's Bane](https://www.projectaon.org/en/xhtml/lw/19wb/19wb.zip)  
* [The Curse of Naar](https://www.projectaon.org/en/xhtml/lw/20tcon/20tcon.zip)

#### **New Order**

* [Voyage of the Moonstone](https://www.projectaon.org/en/xhtml/lw/21votm/21votm.zip)  
* [The Buccaneers of Shadaki](https://www.projectaon.org/en/xhtml/lw/22tbos/22tbos.zip)  
* [Mydnight's Hero](https://www.projectaon.org/en/xhtml/lw/23mh/23mh.zip)  
* [Rune War](https://www.projectaon.org/en/xhtml/lw/24rw/24rw.zip)  
* [Trail of the Wolf](https://www.projectaon.org/en/xhtml/lw/25totw/25totw.zip)  
* [The Fall of Blood Mountain](https://www.projectaon.org/en/xhtml/lw/26tfobm/26tfobm.zip)  
* [Vampirium](https://www.projectaon.org/en/xhtml/lw/27v/27v.zip)  
* [The Hunger of Sejanoz](https://www.projectaon.org/en/xhtml/lw/28thos/28thos.zip)  
* [The Storms of Chai](https://www.projectaon.org/en/xhtml/lw/29tsoc/29tsoc.zip)

### **Appendice B: Analisi e Riutilizzo del Codice Esistente**

Questa sezione analizza i file Kotlin del precedente progetto "Immunda Noctis" e definisce come verranno riutilizzati o adattati per LoneWolfRedux.

* **MainActivity.kt**  
  * **Scopo Originale**: Menu principale con icone per avviare le varie sezioni dell'app.  
  * **Riutilizzo in LoneWolfRedux**: **Concetto riutilizzato**. La nostra MainActivity fungerà da punto di ingresso e menu principale, lanciando le altre Activity (DownloadManagerActivity, LlmManagerActivity, ConfigurationActivity) tramite Intent, proprio come nel progetto originale.  
* **ModelActivity.kt**  
  * **Scopo Originale**: Gestione completa dei modelli LLM (download, selezione, configurazione parametri).  
  * **Riutilizzo in LoneWolfRedux**: **Altamente riutilizzabile come base per la LlmManagerActivity**. La logica per mostrare una lista di elementi scaricabili, interagire con WorkManager e salvare le preferenze è un modello perfetto da seguire.  
* **DownloadWorker.kt**  
  * **Scopo Originale**: Worker in background per scaricare file pesanti (modelli AI).  
  * **Riutilizzo in LoneWolfRedux**: **Direttamente riutilizzabile**. Verrà adattato per gestire non solo i modelli AI, ma anche i file ZIP dei libri. Aggiungeremo una fase di decompressione al termine del download. Sarà il cavallo di battaglia sia per il DownloadManagerViewModel che per il LlmManagerViewModel.  
* **AdventureActivity.kt e MainViewModel.kt**  
  * **Scopo Originale**: AdventureActivity era il cuore del gioco, con una UI nativa in Compose. MainViewModel ne gestiva tutta la complessa logica.  
  * **Riutilizzo in LoneWolfRedux**: **Fonte di ispirazione architetturale**. Sebbene la GameActivity di LoneWolfRedux utilizzi WebView invece di una UI nativa per il gioco, la **struttura** di questi file è preziosa. Il modo in cui MainViewModel gestisce lo stato con StateFlow, comunica con i motori, e orchestra la logica di gioco è il modello che seguiremo per i nostri GameViewModel, DownloadManagerViewModel, ecc.  
* **GemmaEngine.kt e TranslationEngine.kt**  
  * **Scopo Originale**: Motori per l'inferenza con Gemma e la traduzione con ML Kit.  
  * **Riutilizzo in LoneWolfRedux**: **Direttamente riutilizzabili**. Corrispondono perfettamente ai motori "Avanzato" e "Veloce" definiti nelle nostre specifiche. Verranno integrati nel GameViewModel e utilizzati secondo le regole definite nella sezione 4.1.  
* **TtsService.kt e TtsPreferences.kt**  
  * **Scopo Originale**: Servizio incapsulato per la gestione del Text-to-Speech.  
  * **Riutilizzo in LoneWolfRedux**: **Direttamente riutilizzabili**. Verranno invocati dalla GameActivity quando l'utente seleziona del testo nella BookWebView e sceglie l'opzione "Leggi" dal menu contestuale.
 
Licenza
Distribuito con licenza MIT.
