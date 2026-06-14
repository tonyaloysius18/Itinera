package com.itinera.app.i18n

import com.itinera.app.deviceLanguageCode

/**
 * Languages offered in the app. SYSTEM follows the device locale; the rest force
 * a specific language. Add more by adding an entry here and a Strings map below.
 */
enum class Language(val englishName: String, val nativeName: String) {
    SYSTEM("System default", "System default"),
    ENGLISH("English", "English"),
    FRENCH("French", "Français"),
    SPANISH("Spanish", "Español"),
    GERMAN("German", "Deutsch"),
    ITALIAN("Italian", "Italiano"),
    PORTUGUESE("Portuguese", "Português"),
    DUTCH("Dutch", "Nederlands"),
    POLISH("Polish", "Polski"),
    
    CHINESE("Chinese", "中文"),
    
    JAPANESE("Japanese", "日本語"),
    
    KOREAN("Korean", "한국어"),
    
    RUSSIAN("Russian", "Русский"),
    
    TAMIL("Tamil", "தமிழ்"),
    
    HINDI("Hindi", "हिंदी"),

    UKRAINIAN("Ukrainian", "Українська"),

    HUNGARIAN("Hungarian", "Magyar"),

    ROMANIAN("Romanian", "Română"),
    
    GREEK("Greek", "Ελληνικά"),
    
    GEORGIAN("Georgian", "ქართული"),
    
    BULGARIAN("Bulgarian", "български"),
    
    CZECH("Czech", "Čeština"),
    
    LATVIAN("Latvian", "Latviešu"),
    
    LITHUANIAN("Lithuanian", "Lietuvių"),
    
    SLOVAK("Slovak", "Slovenčina"),
    
    SLOVENIAN("Slovenian", "Slovenščina"),
    
    SERBIAN("Serbian", "Српски"),
    
    ALBANIAN("Albanian", "Shqip"),
    
    MACEDONIAN("Macedonian", "Македонски"),
    
    MOLDAVIAN("Moldovan", "Молдавски"),
    
    NORWEGIAN("Norwegian", "Norsk"),
    
    FINNISH("Finnish", "Suomi"),
    
    SWEDISH("Swedish", "Svenska"),
    
    DANISH("Danish", "Dansk"),
    
    ESTONIAN("Estonian", "Eesti"),
    
    ARABIC("Arabic", "العربية"),
    TURKISH("Turkish", "Türkçe"),
    VIETNAMESE("Vietnamese", "Tiếng Việt"),
    INDONESIAN("Indonesian", "Bahasa Indonesia"),
    THAI("Thai", "ไทย"),
    HEBREW("Hebrew", "עברית"),
    BENGALI("Bengali", "বাংলা"),
    TAGALOG("Tagalog", "Tagalog"),

}

/**
 * Every piece of UI text the app owns. User-entered content (trip titles, city
 * names) is NOT in here on purpose — only the app's own chrome gets translated.
 *
 * In production this would be Compose Multiplatform string resources
 * (composeResources/values-fr/strings.xml etc.). A plain data class is used here
 * so language switching works at runtime with zero extra build setup.
 */
data class Strings(
    val name: String, val surname: String, val dob: String, val street: String, val city: String,
    val postelCode: String, val appTagline: String, val myTrips: String, val countries: String, val legs: String,
    val done: String, val addLeg: String, val newLeg: String, val from: String, val to: String,
    val transport: String, val date: String, val time: String, val bookingRef: String, val optional: String,
    val saveLeg: String, val tickets: String, val event: String, val beforeYouGo: String, val addItem: String,
    val settings: String, val backupSync: String, val appearance: String, val language: String, val currencyUnits: String,
    val exportTrips: String, val about: String, val logOut: String, val welcomeBack: String, val logInToTrips: String,
    val email: String, val password: String, val forgotPassword: String, val logIn: String, val orDivider: String,
    val continueGoogle: String, val continueApple: String, val newHere: String, val createAccount: String, val calendar: String,
    val documents: String, val nextUp: String, val legsTravelled: String, val syncWholeTrip: String, val addToPhoneCalendar: String,
    val remindMe: String, val chooseLanguage: String, val followPhone: String, val ok: String, val cancel: String, val address: String,
    val editProfile: String, val newPassword: String, val saveChanges: String, val systemDefault: String, val light: String,
    val dark: String, val matchYourPhone: String, val currency: String, val convert: String, 
    val bookings: String, val packing: String, val money: String, val gadget: String, val other: String, val needToAdd: String, val section: String,
    val amount: String, val validAmount: String, val couldntFetch: String, val fillAllFields: String, val loginFailed: String,
    val passwordTooShort: String, val signupFailed: String,


    val add: String, val addToItinerary: String, val travel: String, val place: String,
    val newPlace: String, val editPlace: String, val landmark: String, val savePlace: String, val searchTrips: String,
    val newTrip: String, val renameTrip: String, val create: String, val save: String,
    val pin: String, val unpin: String, val edit: String, val archive: String, val unarchive: String,
    val delete: String, val deleteTripQ: String, val deletePlaceQ: String, val deleteLegQ: String,
    val cantBeUndone: String, val archivedTrips: String, val noArchivedTrips: String,
    val noDatesYet: String, val tripName: String, val aLegBetween: String, val somewhereToVisit: String,
)

private val EN = Strings(
    name = "Name", surname = "Surname", dob = "Date of birth", street = "Street", city = "City",
    postelCode = "Postal code", appTagline = "Where every journey begins!", myTrips = "My trips", countries = "countries", legs = "legs",
    done = "done", addLeg = "Add leg", newLeg = "New leg", from = "From", to = "To",
    transport = "Transport", date = "Date", time = "Time", bookingRef = "Booking ref", optional = "optional",
    saveLeg = "Save leg", tickets = "Tickets", event = "Event", beforeYouGo = "Before you go", addItem = "Add item",
    settings = "Settings", backupSync = "Backup & sync", appearance = "Appearance", language = "Language", currencyUnits = "Currency & units",
    exportTrips = "Export trips", about = "About", logOut = "Log out", welcomeBack = "Welcome back", logInToTrips = "Log in to your trips",
    email = "E-mail", password = "Password", forgotPassword = "Forgot password?", logIn = "Log in", orDivider = "or",
    continueGoogle = "Continue with Google", continueApple = "Continue with Apple", newHere = "New here?", createAccount = "Create account", calendar = "Calendar",
    documents = "Documents", nextUp = "next up", legsTravelled = "legs travelled", syncWholeTrip = "Sync whole trip to calendar", addToPhoneCalendar = "Add to phone calendar",
    remindMe = "Remind me", chooseLanguage = "Choose a language", followPhone = "Follow phone language", ok = "OK", cancel = "Cancel", address = "Address", editProfile = "Edit profile",
    newPassword = "New password", saveChanges = "Save changes", systemDefault = "System default", light = "Light", dark = "Dark", matchYourPhone = "Match your phone", currency = "Currency",
    convert = "Convert", bookings = "Bookings", packing = "Packing", money = "Money", gadget = "Gadget", other = "Other", needToAdd = "What do you need to add?", section = "Section",
    amount = "Amount", validAmount = "Enter a valid amount", couldntFetch = "Couldn't fetch rate. Check your connection.",
    fillAllFields = "Please fill in all fields", loginFailed = "Login failed. Check your email and password.",
    passwordTooShort = "Password must be at least 6 characters", signupFailed = "Could not create account. The email may already be in use.",



    add = "Add", addToItinerary = "Add to itinerary", travel = "Travel", place = "Place",
    newPlace = "New place", editPlace = "Edit Place", landmark = "Landmark (optional)", savePlace = "Save place", searchTrips = "Search trips",
    newTrip = "New trip", renameTrip = "Rename trip", create = "Create", save = "Save",
    pin = "Pin", unpin = "Unpin", edit = "Edit", archive = "Archive", unarchive = "Unarchive",
    delete = "Delete", deleteTripQ = "Delete trip?", deletePlaceQ = "Delete place?", deleteLegQ = "Delete leg?",
    cantBeUndone = "This cannot be undone.", archivedTrips = "Archived trips", noArchivedTrips = "No archived trips",
    noDatesYet = "No dates yet", tripName = "Trip name", aLegBetween = "A leg between", somewhereToVisit = "Somewhere to visit",
)

private val FR = EN.copy(
    name = "Prénom", surname = "Nom", dob = "Date de naissance", street = "Rue", city = "Ville",
    postelCode = "Code postal", appTagline = "Là où commence chaque voyage!", myTrips = "Mes voyages", countries = "pays", legs = "trajets",
    done = "effectués", addLeg = "Ajouter un trajet", newLeg = "Nouveau trajet", from = "De", to = "À",
    transport = "Transport", date = "Date", time = "Heure", bookingRef = "Référence", optional = "facultatif",
    saveLeg = "Enregistrer", tickets = "Billets", event = "Événement", beforeYouGo = "Avant le départ", addItem = "Ajouter",
    settings = "Réglages", backupSync = "Sauvegarde et sync", appearance = "Apparence", language = "Langue", currencyUnits = "Devise et unités",
    exportTrips = "Exporter les voyages", about = "À propos", logOut = "Se déconnecter", welcomeBack = "Bon retour", logInToTrips = "Connectez-vous à vos voyages",
    email = "E-mail", password = "Mot de passe", forgotPassword = "Mot de passe oublié ?", logIn = "Se connecter", continueGoogle = "Continuer avec Google",
    continueApple = "Continuer avec Apple", newHere = "Nouveau ?", createAccount = "Créer un compte", calendar = "Calendrier", documents = "Documents",
    nextUp = "à venir", legsTravelled = "trajets effectués", syncWholeTrip = "Synchroniser tout le voyage", addToPhoneCalendar = "Ajouter au calendrier", remindMe = "Me rappeler",
    chooseLanguage = "Choisir une langue", followPhone = "Suivre la langue du téléphone", ok = "OK", cancel = "Annuler", address = "Adresse", editProfile = "Modifier le profil",
    newPassword = "Nouveau mot de passe", saveChanges = "Enregistrer les modifications", systemDefault = "Par défaut du système", light = "Clair", dark = "Sombre",
    matchYourPhone = "Match votre téléphone", currency = "Devise", orDivider = "ou", convert = "Convertir", bookings = "Réservations", packing = "Emballage", money = "Argent",
    gadget = "Appareil", other = "Autre", needToAdd = "Que voulez-vous ajouter ?", section = "Section",
    amount = "Montant", validAmount = "Entrez un montant valide", couldntFetch = "Impossible de récupérer le taux. Vérifiez votre connexion.",
    fillAllFields = "Veuillez remplir tous les champs", loginFailed = "Connexion échouée. Vérifiez votre adresse e-mail et mot de passe.",
    
    add = "Ajouter", addToItinerary = "Ajouter à l'itinéraire", travel = "Voyage", place = "Lieu", 
    newPlace = "Nouveau lieu", editPlace = "Modifier le lieu", landmark = "Point de repère (facultatif)", savePlace = "Enregistrer le lieu", searchTrips = "Rechercher des voyages", 
    newTrip = "Nouveau voyage", renameTrip = "Renommer le voyage", create = "Créer", save = "Enregistrer", 
    pin = "Épingler", unpin = "Désépingler", edit = "Modifier", archive = "Archiver", unarchive = "Déarchiver", 
    delete = "Supprimer", deleteTripQ = "Supprimer le voyage?", deletePlaceQ = "Supprimer le lieu?", deleteLegQ = "Supprimer le trajet?", 
    cantBeUndone = "Cela ne peut pas être annulé.", archivedTrips = "Voyages archivés", noArchivedTrips = "Aucun voyage archivé", 
    noDatesYet = "Aucune date trouvée", tripName = "Nom du voyage", aLegBetween = "Un trajet entre", somewhereToVisit = "Où visiter", 
)

private val ES = EN.copy(
    name = "Nombre", surname = "Apellido", dob = "Fecha de nacimiento", street = "Calle", city = "Ciudad",
    postelCode = "Código postal", appTagline = "Donde comienza cada viaje!", myTrips = "Mis viajes", countries = "países", legs = "tramos",
    done = "hechos", addLeg = "Añadir tramo", newLeg = "Nuevo tramo", from = "Desde", to = "Hasta",
    date = "Fecha", time = "Hora", bookingRef = "Reserva", optional = "opcional", saveLeg = "Guardar",
    tickets = "Billetes", event = "Evento", beforeYouGo = "Antes de salir", addItem = "Añadir", settings = "Ajustes",
    backupSync = "Copia y sync", appearance = "Apariencia", language = "Idioma", currencyUnits = "Moneda y unidades", exportTrips = "Exportar viajes",
    about = "Acerca de", logOut = "Cerrar sesión", welcomeBack = "Bienvenido", logInToTrips = "Inicia sesión en tus viajes", email = "Correo",
    password = "Contraseña", forgotPassword = "¿Olvidaste la contraseña?", logIn = "Iniciar sesión", continueGoogle = "Continuar con Google", continueApple = "Continuar con Apple",
    newHere = "¿Nuevo aquí?", createAccount = "Crear cuenta", calendar = "Calendario", documents = "Documentos", nextUp = "siguiente",
    legsTravelled = "tramos hechos", syncWholeTrip = "Sincronizar todo el viaje", addToPhoneCalendar = "Añadir al calendario", remindMe = "Recordarme", chooseLanguage = "Elige un idioma",
    followPhone = "Seguir idioma del teléfono", ok = "OK", cancel = "Cancelar", address = "Dirección", editProfile = "Editar perfil", newPassword = "Nueva contraseña",
    saveChanges = "Guardar cambios", systemDefault = "Por defecto del sistema", light = "Claro", dark = "Oscuro", matchYourPhone = "Coincidir con tu teléfono", currency = "Moneda",
    transport = "Transporte", orDivider = "o", convert = "Convertir",
    bookings = "Reservas", packing = "Envase", money = "Dinero", gadget = "Dispositivo", other = "Otro", needToAdd = "¿Qué necesitas agregar?", section = "Sección",
    amount = "Monto", validAmount = "Ingrese un monto válido", couldntFetch = "No se pudo obtener la tasa. Verifique su conexión.",
    fillAllFields = "Por favor complete todos los campos", loginFailed = "Inicio de sesión fallido. Verifique su dirección de correo electrónico y contraseña.",


    add = "Añadir", addToItinerary = "Añadir al itinerario", travel = "Viaje", place = "Lugar",
    newPlace = "Nuevo lugar", editPlace = "Editar lugar", landmark = "Punto de referencia (opcional)", savePlace = "Guardar lugar", searchTrips = "Buscar viajes",
    newTrip = "Nuevo viaje", renameTrip = "Renombrar viaje", create = "Crear", save = "Guardar",
    pin = "Fijar", unpin = "Desfijar", edit = "Editar", archive = "Archivar", unarchive = "Desarchivar", 
    delete = "Eliminar", deleteTripQ = "¿Eliminar viaje?", deletePlaceQ = "¿Eliminar lugar?", deleteLegQ = "¿Eliminar tramo?", 
    cantBeUndone = "Esto no se puede deshacer.", archivedTrips = "Viajes archivados", noArchivedTrips = "Sin viajes archivados", 
    noDatesYet = "Sin fechas aún", tripName = "Nombre del viaje", aLegBetween = "Un tramo entre", somewhereToVisit = "Algún lugar para visitar", 
)

private val DE = EN.copy(
    name = "Vorname", surname = "Nachname", dob = "Geburtsdatum", street = "Straße", city = "Stadt",
    postelCode = "Postleitzahl", appTagline = "Wo jede Reise beginnt!", myTrips = "Meine Reisen", countries = "Länder", legs = "Etappen",
    done = "erledigt", addLeg = "Etappe hinzufügen", newLeg = "Neue Etappe", from = "Von", to = "Nach",
    date = "Datum", time = "Zeit", bookingRef = "Buchung", optional = "optional", saveLeg = "Speichern",
    tickets = "Tickets", event = "Termin", beforeYouGo = "Vor der Abreise", addItem = "Hinzufügen", settings = "Einstellungen",
    backupSync = "Backup & Sync", appearance = "Darstellung", language = "Sprache", currencyUnits = "Währung & Einheiten", exportTrips = "Reisen exportieren",
    about = "Über", logOut = "Abmelden", welcomeBack = "Willkommen zurück", logInToTrips = "Bei deinen Reisen anmelden", email = "E-Mail",
    password = "Passwort", forgotPassword = "Passwort vergessen?", logIn = "Anmelden", continueGoogle = "Mit Google fortfahren", continueApple = "Mit Apple fortfahren",
    newHere = "Neu hier?", createAccount = "Konto erstellen", calendar = "Kalender", documents = "Dokumente", nextUp = "als Nächstes",
    legsTravelled = "Etappen gereist", syncWholeTrip = "Ganze Reise synchronisieren", addToPhoneCalendar = "Zum Kalender hinzufügen", remindMe = "Erinnern", chooseLanguage = "Sprache wählen",
    followPhone = "Telefonsprache folgen", ok = "OK", cancel = "Abbrechen", address = "Adresse", editProfile = "Profil bearbeiten", newPassword = "Neues Passwort",
    saveChanges = "Änderungen speichern", systemDefault = "System-Standard", light = "Hell", dark = "Dunkel", matchYourPhone = "Mit Ihrem Telefon", currency = "Währung",
    transport = "Verkehrsmittel", orDivider = "oder", convert = "Umwandeln", 
    bookings = "Reservierungen", packing = "Packung", money = "Geld", gadget = "Gerät", other = "Andere", needToAdd = "Was möchten Sie hinzufügen?", section = "Abschnitt",
    amount = "Betrag", validAmount = "Geben Sie eine gültige Menge ein", couldntFetch = "Konnte den Kurs nicht abrufen. Überprüfen Sie Ihre Verbindung.",
    fillAllFields = "Bitte füllen Sie alle Felder aus", loginFailed = "Anmeldung fehlgeschlagen. Überprüfen Sie Ihre E-Mail-Adresse und Ihr Passwort.",

    add = "Hinzufügen", addToItinerary = "Zum Reiseplan hinzufügen", travel = "Reise", place = "Ort",
    newPlace = "Neuer Ort", editPlace = "Ort bearbeiten", landmark = "Wahrzeichen (optional)", savePlace = "Ort speichern", searchTrips = "Reisen suchen",
    newTrip = "Neue Reise", renameTrip = "Reise umbenennen", create = "Erstellen", save = "Speichern",
    pin = "Anheften", unpin = "Lösen", edit = "Bearbeiten", archive = "Archivieren", unarchive = "Dearchivieren",
    delete = "Löschen", deleteTripQ = "Reise löschen?", deletePlaceQ = "Ort löschen?", deleteLegQ = "Etappe löschen?",
    cantBeUndone = "Dies kann nicht rückgängig gemacht werden.", archivedTrips = "Archivierte Reisen", noArchivedTrips = "Keine archivierten Reisen",
    noDatesYet = "Noch keine Termine", tripName = "Name der Reise", aLegBetween = "Eine Etappe zwischen", somewhereToVisit = "Irgendwo zu besuchen",
)

private val IT = EN.copy(
    name = "Nome", surname = "Cognome", dob = "Data di nascita", street = "Via", city = "Città",
    postelCode = "Codice postale", appTagline = "Dove inizia ogni viaggio!", myTrips = "I miei viaggi", countries = "paesi", legs = "tratte",
    done = "completati", addLeg = "Aggiungi tratta", newLeg = "Nuova tratta", from = "Da", to = "A",
    date = "Data", time = "Ora", bookingRef = "Riferimento", optional = "opzionale", saveLeg = "Salva",
    tickets = "Biglietti", event = "Evento", beforeYouGo = "Prima di partire", addItem = "Aggiungi elemento", settings = "Impostazioni",
    backupSync = "Backup e sincronizzazione", appearance = "Aspetto", language = "Lingua", currencyUnits = "Valuta e unità", exportTrips = "Esporta viaggi",
    about = "Informazioni", logOut = "Esci", welcomeBack = "Bentornato", logInToTrips = "Accedi ai tuoi viaggi", email = "E-mail",
    password = "Password", forgotPassword = "Password dimenticata?", logIn = "Accedi", continueGoogle = "Continua con Google", continueApple = "Continua con Apple",
    newHere = "Nuovo qui?", createAccount = "Crea account", calendar = "Calendario", documents = "Documentos", nextUp = "prossimo",
    legsTravelled = "tratte percorse", syncWholeTrip = "Sincronizza tutto il viaggio", addToPhoneCalendar = "Aggiungi al calendario", remindMe = "Ricordami", chooseLanguage = "Scegli una lingua",
    followPhone = "Segui lingua del telefono", ok = "OK", cancel = "Annulla", address = "Indirizzo", editProfile = "Modifica profilo", newPassword = "Nuova password",
    saveChanges = "Salva modifiche", systemDefault = "Predefinito del sistema", light = "Chiaro", dark = "Scuro", matchYourPhone = "Coincide con il tuo telefono", currency = "Valuta",
    transport = "Trasporto", orDivider = "o", convert = "Convertire",
    bookings = "Prenotazioni", packing = "Pacco", money = "Monero", gadget = "Gadget", other = "Altro", needToAdd = "Cosa vuoi aggiungere?", section = "Sezione",
    amount = "Importo", validAmount = "Inserisci un importo valido", couldntFetch = "Impossibile ottenere il tasso. Controllare la connessione.",
    fillAllFields = "Per favore compila tutti i campi", loginFailed = "Login fallito. Controllare la tua email e password.",



    add = "Aggiungi", addToItinerary = "Aggiungi all'itinerario", travel = "Viaggio", place = "Luogo",
    newPlace = "Nuovo luogo", editPlace = "Modifica luogo", landmark = "Punto di riferimento (opzionale)", savePlace = "Salva luogo", searchTrips = "Cerca viaggi",
    newTrip = "Nuovo viaggio", renameTrip = "Rinomina viaggio", create = "Crea", save = "Salva",
    pin = "Fissa", unpin = "Rimuovi fissa", edit = "Modifica", archive = "Archivia", unarchive = "Ripristina", 
    delete = "Elimina", deleteTripQ = "Elimina viaggio?", deletePlaceQ = "Elimina luogo?", deleteLegQ = "Elimina tratta?", 
    cantBeUndone = "Questa azione non può essere annullata.", archivedTrips = "Viaggi archiviati", noArchivedTrips = "Nessun viaggio archiviato", 
    noDatesYet = "Ancora nessuna data", tripName = "Nome del viaggio", aLegBetween = "Una tratta tra", somewhereToVisit = "Qualche posto da visitare", 
)

private val PT = EN.copy(
    name = "Nome", surname = "Sobrenome", dob = "Data de nascimento", street = "Rua", city = "Cidade",
    postelCode = "Código postal", appTagline = "Onde começa cada viagem!", myTrips = "Minhas viagens",
    countries = "países", legs = "legs", done = "feito", addLeg = "Adicionar leg", newLeg = "Nova leg", from = "De",
    to = "Para", date = "Data", time = "Hora", bookingRef = "Reserva", optional = "opcional", saveLeg = "Salvar",
    tickets = "Bilhetes", event = "Evento", beforeYouGo = "Antes de sair", addItem = "Adicionar", settings = "Configurações",
    backupSync = "Backup & sincronização", appearance = "Aparência", language = "Idioma", currencyUnits = "Moeda e unidades",
    exportTrips = "Exportar viagens", about = "Sobre", logOut = "Sair", welcomeBack = "Bem-vindo de volta",
    logInToTrips = "Entrar em suas viagens", email = "E-mail", password = "Senha", forgotPassword = "Esqueceu a senha?",
    logIn = "Entrar", continueGoogle = "Continuar com Google", continueApple = "Continuar com Apple",
    newHere = "Novo aqui?", createAccount = "Criar conta", calendar = "Calendário", documents = "Documentos", nextUp = "próximo",
    legsTravelled = "legs percorridos", syncWholeTrip = "Sincronizar toda a viagem", addToPhoneCalendar = "Adicionar ao calendário",
    remindMe = "Lembrar", chooseLanguage = "Escolher idioma", followPhone = "Seguir idioma do telefone",
    ok = "OK", cancel = "Cancelar", address = "Endereço", editProfile = "Editar perfil", newPassword = "Nova senha",
    saveChanges = "Salvar alterações", systemDefault = "Padrão do sistema", light = "Claro", dark = "Escuro",
    matchYourPhone = "Coincidir com o seu telefone", currency = "Moeda", transport = "Transporte", orDivider = "ou", convert = "Converter",
    bookings = "Reservas", packing = "Embalagem", money = "Dinheiro", gadget = "Dispositivo", other = "Outro", needToAdd = "O que você precisa adicionar?", section = "Seção",
    amount = "Montante", validAmount = "Insira um montante válido", couldntFetch = "Não foi possível obter a taxa. Verifique a sua conexão.",
    fillAllFields = "Por favor preencha todos os campos", loginFailed = "Login falhou. Verifique a sua email e senha.",


    add = "Adicionar", addToItinerary = "Adicionar ao itinerário", travel = "Viagem", place = "Lugar",
    newPlace = "Novo lugar", editPlace = "Editar lugar", landmark = "Ponto de referência (opcional)", savePlace = "Salvar lugar", searchTrips = "Pesquisar viagens",
    newTrip = "Nova viagem", renameTrip = "Renomear viagem", create = "Criar", save = "Salvar",
    pin = "Fixar", unpin = "Desafixar", edit = "Editar", archive = "Arquivar", unarchive = "Desarquivar",
    delete = "Excluir", deleteTripQ = "Excluir viagem?", deletePlaceQ = "Excluir lugar?", deleteLegQ = "Excluir leg?",
    cantBeUndone = "Isso não pode ser desfeito.", archivedTrips = "Viagens arquivadas", noArchivedTrips = "Nenhuma viagem arquivada",
    noDatesYet = "Sem datas ainda", tripName = "Nome da viagem", aLegBetween = "Uma leg entre", somewhereToVisit = "Algum lugar para visitar",
)
private val NL = EN.copy(
    name = "Naam", surname = "Achternaam", dob = "Geboortedatum", street = "Straat", city = "Stad", postelCode = "Postcode",
    appTagline = "Waar elke reis begint!", myTrips = "Mijn reizen", countries = "countries", legs = "legs",
    done = "afgerond", addLeg = "Leg toevoegen", newLeg = "Nieuwe leg", from = "Van", to = "Naar",
    date = "Datum", time = "Tijd", bookingRef = "Reserveringsnummer", optional = "optioneel", saveLeg = "Opslaan",
    tickets = "Billets", event = "Event", beforeYouGo = "Voordat je gaat", addItem = "Item toevoegen", settings = "Instellingen",
    backupSync = "Backup & synchronisatie", appearance = "Apparaat", language = "Taal", currencyUnits = "Woonheid",
    exportTrips = "Reizen exporteren", about = "Over", logOut = "Uitloggen", welcomeBack = "Welkom terug",
    logInToTrips = "Inloggen naar je reizen", email = "E-mail", password = "Wachtwoord", forgotPassword = "Wachtwoord vergeten?",
    logIn = "Inloggen", continueGoogle = "Doorgaan met Google", continueApple = "Doorgaan met Apple",
    newHere = "Nieuw hier?", createAccount = "Account creëren", calendar = "Kalender", documents = "Documents", nextUp = "naast",
    legsTravelled = "legs doorlopen", syncWholeTrip = "Alle rit synchroniseren", addToPhoneCalendar = "Toevoegen aan agenda", remindMe = "Opgelet",
    chooseLanguage = "Taal kiezen", followPhone = "Volg telefoontaal", ok = "OK", cancel = "Annuleer", address = "Adres", editProfile = "Profiel wijzigen", newPassword = "Nieuw wachtwoord",
    saveChanges = "Opslaan wijzigingen", systemDefault = "Systeemstandaard", light = "Licht", dark = "Donker", matchYourPhone = "Match je telefoon", currency = "Woonheid",
    transport = "Vervoer", orDivider = "of", convert = "Convert", 
    bookings = "Reservaties", packing = "Pakken", money = "Geld", gadget = "Gadget", other = "Overige", needToAdd = "Wat wilt u toevoegen?", section = "Sectie",
    amount = "Bedrag", validAmount = "Geef een geldig bedrag", couldntFetch = "Kon de kurs niet ophalen. Controleer uw verbinding.",
    fillAllFields = "Vul alle velden in", loginFailed = "Inloggen mislukt. Controleer uw e-mailadres en wachtwoord.",

    add = "Toevoegen", addToItinerary = "Toevoegen aan reisplan", travel = "Reis", place = "Plaats",
    newPlace = "Nieuwe plaats", editPlace = "Plaats bewerken", landmark = "Bezienswaardigheid (optioneel)", savePlace = "Plaats opslaan", searchTrips = "Reizen zoeken",
    newTrip = "Nieuwe reis", renameTrip = "Reis hernoemen", create = "Maken", save = "Opslaan",
    pin = "Vastzetten", unpin = "Losmaken", edit = "Bewerken", archive = "Archiveren", unarchive = "Dearchiveren",
    delete = "Verwijderen", deleteTripQ = "Reis verwijderen?", deletePlaceQ = "Plaats verwijderen?", deleteLegQ = "Leg verwijderen?",
    cantBeUndone = "Dit kan niet ongedaan worden gemaakt.", archivedTrips = "Gearchiveerde reizen", noArchivedTrips = "Geen gearchiveerde reizen",
    noDatesYet = "Nog geen data", tripName = "Naam van de reis", aLegBetween = "Een leg tussen", somewhereToVisit = "Iets om te bezoeken",
)

private val PL = EN.copy(
    name = "Imię", surname = "Nazwisko", dob = "Data urodzenia", street = "Ulica", city = "Miasto",
    postelCode = "Kod pocztowy", appTagline = "Tam, gdzie zaczyna się każda podróż!", myTrips = "Moje podróże", countries = "kraje", legs = "odcinki",
    done = "ukończone", addLeg = "Dodaj odcinek", newLeg = "Nowy odcinek", from = "Z", to = "Do",
    date = "Data", time = "Czas", bookingRef = "Rezerwacja", optional = "opcjonalne", saveLeg = "Zapisz",
    tickets = "Bilety", event = "Wydarzenie", beforeYouGo = "Przed wyjazdem", addItem = "Dodaj element", settings = "Ustawienia",
    backupSync = "Kopia i synchronizacja", appearance = "Wygląd", language = "Język", currencyUnits = "Waluta i jednostki", exportTrips = "Eksportuj podróże",
    about = "O aplikacji", logOut = "Wyloguj się", welcomeBack = "Witaj ponownie", logInToTrips = "Zaloguj się do podróży", email = "E-mail",
    password = "Hasło", forgotPassword = "Zapomniałeś hasła?", logIn = "Zaloguj się", continueGoogle = "Kontynuuj z Google", continueApple = "Kontynuuj z Apple",
    newHere = "Nowy tutaj?", createAccount = "Utworz konto", calendar = "Kalendarz", documents = "Dokumenty", nextUp = "następne",
    legsTravelled = "przebyte odcinki", syncWholeTrip = "Synchronizuj całą podróż", addToPhoneCalendar = "Dodaj do kalendarza", remindMe = "Przypomnij mi", chooseLanguage = "Wybierz język",
    followPhone = "Użyj języka telefonu", ok = "OK", cancel = "Anuluj", address = "Adres", editProfile = "Edytuj profil",
    newPassword = "Nowe hasło", saveChanges = "Zapisz zmiany", systemDefault = "Domyślny system", light = "Jasny", dark = "Ciemny", matchYourPhone = "Zgadnij telefon", currency = "Waluta",
    transport = "Transport", orDivider = "lub", convert = "Przelicz",
    bookings = "Rezerwacje", packing = "Pakowanie", money = "Pieniądze", gadget = "Gadżet", other = "Inne", needToAdd = "Co chcesz dodać?", section = "Sekcja",
    amount = "Kwota", validAmount = "Wprowadź prawidłową kwotę", couldntFetch = "Nie udało się pobrać kursu. Sprawdź połączenie.",
    fillAllFields = "Proszę uzupełnić wszystkie pola", loginFailed = "Logowanie nieudane. Sprawdź poprawność adresu e-mail i hasła.",



    add = "Dodaj", addToItinerary = "Dodaj do planu podróży", travel = "Podróż", place = "Miejsce",
    newPlace = "Nowe miejsce", editPlace = "Edytuj miejsce", landmark = "Punkt orientacyjny (opcjonalnie)", savePlace = "Zapisz miejsce", searchTrips = "Szukaj podróży",
    newTrip = "Nowa podróż", renameTrip = "Zmień nazwę podróży", create = "Utwórz", save = "Zapisz",
    pin = "Przypnij", unpin = "Odepnij", edit = "Edytuj", archive = "Archiwizuj", unarchive = "Przywróć z archiwum", 
    delete = "Usuń", deleteTripQ = "Usunąć podróż?", deletePlaceQ = "Usunąć miejsce?", deleteLegQ = "Usunąć odcinek?", 
    cantBeUndone = "Tego nie można cofnąć.", archivedTrips = "Zarchiwizowane podróże", noArchivedTrips = "Brak zarchiwizowanych podróży", 
    noDatesYet = "Brak dat", tripName = "Nazwa podróży", aLegBetween = "Odcinek między", somewhereToVisit = "Miejsce do odwiedzenia", 
)

private val ZH = EN.copy(
    name = "名字", surname = "姓氏", dob = "出生日期", street = "街道", city = "城市", postelCode = "邮政编码", appTagline = "从每趟旅程开始！", myTrips = "我的旅程",
    countries = "国家", legs = "行程", done = "完成", addLeg = "添加行程", newLeg = "新的行程", from = "从", to = "到",
    date = "日期", time = "时间", bookingRef = "预订参考", optional = "可选的", saveLeg = "保存",
    tickets = "票", event = "事件", beforeYouGo = "准备离开", addItem = "添加项目", settings = "设置", backupSync = "备份与同步", appearance = "外观",
    language = "语言", currencyUnits = "货币与单位", exportTrips = "导出行程", about = "关于", logOut = "退出", welcomeBack = "欢迎回来",
    logInToTrips = "登录到你的旅程", email = "电子邮件", password = "密码", forgotPassword = "忘记密码？", logIn = "登录", continueGoogle = "继续使用 Google", continueApple = "继续使用 Apple", newHere = "新在这里？",
    createAccount = "创建账号", calendar = "日历", documents = "文档", nextUp = "下一步", legsTravelled = "已走过的行程", syncWholeTrip = "同步整个行程", addToPhoneCalendar = "添加到手机日历", remindMe = "提醒我", chooseLanguage = "选择语言",
    followPhone = "跟随手机语言", ok = "OK", cancel = "取消", address = "地址", editProfile = "编辑资料", newPassword = "新密码", saveChanges = "保存修改", systemDefault = "系统默认",
    light = "亮色", dark = "暗色", matchYourPhone = "匹配你的手机", currency = "货币",transport = "交通", orDivider = "或", convert = "转换",
    bookings = "预订", packing = "装箱", money = "钱", gadget = "设备", other = "其他", needToAdd = "你需要添加什么？", section = "章节",
    amount = "金额", validAmount = "请输入有效的金额", couldntFetch = "无法获取汇率。请检查您的连接。",
    fillAllFields = "请填写所有字段", loginFailed = "登录失败。请检查您的电子邮件地址和密码。",



    add = "添加", addToItinerary = "添加到行程", travel = "旅行", place = "地点",
    newPlace = "新地点", editPlace = "编辑地点", landmark = "地标（可选）", savePlace = "保存地点", searchTrips = "搜索行程",
    newTrip = "新行程", renameTrip = "重命名行程", create = "创建", save = "保存",
    pin = "固定", unpin = "取消固定", edit = "编辑", archive = "归档", unarchive = "取消归档", 
    delete = "删除", deleteTripQ = "删除行程？", deletePlaceQ = "删除地点？", deleteLegQ = "删除行程段？", 
    cantBeUndone = "此操作无法撤销。", archivedTrips = "已归档行程", noArchivedTrips = "没有已归档行程", 
    noDatesYet = "尚无日期", tripName = "行程名称", aLegBetween = "之间的行程", somewhereToVisit = "值得一游的地方", 
)

private val JP = EN.copy(
    name = "名前", surname = "苗字", dob = "誕生日", street = "住所", city = "都市", postelCode = "郵便番号", appTagline = "旅の始まり！", myTrips = "私の旅",
    countries = "国", legs = "経路", done = "完了", addLeg = "経路を追加", newLeg = "新しい経路", from = "から", to = "へ", transport = "交通手段",
    date = "日付", time = "時間", bookingRef = "予約番号", optional = "オプション", saveLeg = "保存", tickets = "チケット", event = "イベント",
    beforeYouGo = "出発前に", addItem = "項目を追加", settings = "設定", backupSync = "バックアップと同期", appearance = "外観", language = "言語",
    currencyUnits = "通貨と単位", exportTrips = "旅をエクスポート", about = "このアプリについて", logOut = "ログアウト", welcomeBack = "おかえり",
    logInToTrips = "旅にログイン", email = "メールアドレス", password = "パスワード", forgotPassword = "パスワードをお忘れですか？", logIn = "ログイン",
    orDivider = "または", continueGoogle = "Googleで続行", continueApple = "Appleで続行", newHere = "新規？", createAccount = "アカウントを作成",
    calendar = "カレンダー", documents = "文書", nextUp = "次の予定", legsTravelled = "完了した経路", syncWholeTrip = "旅行全体を同期",
    addToPhoneCalendar = "カレンダーに追加", remindMe = "リマインド", chooseLanguage = "言語を選択", followPhone = "システム設定に従う", ok = "OK",
    cancel = "キャンセル", address = "住所", editProfile = "プロフィール編集", newPassword = "新しいパスワード", saveChanges = "変更を保存",
    systemDefault = "系统默认", light = "ライト", dark = "ダーク", matchYourPhone = "電話と一致", currency = "通貨", convert = "変換",
    bookings = "予約", packing = "パック", money = "お金", gadget = " gadget", other = "その他", needToAdd = "追加したいものがあれば？", section = "セクション",
    amount = "金額", validAmount = "正しい金額を入力してください", couldntFetch = "レートを取得できませんでした。接続を確認してください。",
    fillAllFields = "すべての項目を入力してください", loginFailed = "ログインに失敗しました。メールアドレスとパスワードを確認してください。",
    

    add = "追加", addToItinerary = "旅程に追加", travel = "旅行", place = "場所",
    newPlace = "新しい場所", editPlace = "場所を編集", landmark = "目印（オプション）", savePlace = "場所を保存", searchTrips = "旅を検索",
    newTrip = "新しい旅", renameTrip = "旅の名前を変更", create = "作成", save = "保存",
    pin = "ピン留め", unpin = "ピン留めを解除", edit = "編集", archive = "アーカイブ", unarchive = "アーカイブを解除", 
    delete = "削除", deleteTripQ = "旅を削除しますか？", deletePlaceQ = "場所を削除しますか？", deleteLegQ = "経路を削除しますか？", 
    cantBeUndone = "この操作は取り消せません。", archivedTrips = "アーカイブ済みの旅", noArchivedTrips = "アーカイブ済みの旅はありません", 
    noDatesYet = "日付が未設定です", tripName = "旅の名前", aLegBetween = "の間の経路", somewhereToVisit = "訪れる場所", 
)

private val KR = EN.copy(
    name = "이름", surname = "성", dob = "생년월일", street = "거리", city = "도시", postelCode = "우편번호", appTagline = "여행의 시작!", myTrips = "내 여행",
    countries = "국가", legs = "경로", done = "완료", addLeg = "경로 추가", newLeg = "새로운 경로", from = "출발", to = "도착",
    transport = "교통수단", date = "날짜", time = "시간", bookingRef = "예약 번호", optional = "선택 사항",
    saveLeg = "경로 저장", tickets = "티켓", event = "이벤트", beforeYouGo = "출발 전", addItem = "항목 추가",
    settings = "설정", backupSync = "백업 및 동기화", appearance = "테마", language = "언어", currencyUnits = "통화 및 단위",
    exportTrips = "여행 내보내기", about = "정보", logOut = "로그아웃", welcomeBack = "다시 오신 것을 환영합니다", logInToTrips = "여행 정보를 확인하려면 로그인하세요",
    email = "이메일", password = "비밀번호", forgotPassword = "비밀번호를 잊으셨나요?", logIn = "로그인", orDivider = "또는",
    continueGoogle = "Google 계정으로 계속하기", continueApple = "Apple 계정으로 계속하기", newHere = "처음이신가요?", createAccount = "계정 만들기", calendar = "캘린더",
    documents = "문서", nextUp = "다음 일정", legsTravelled = "여행한 경로", syncWholeTrip = "전체 여행 일정 동기화", addToPhoneCalendar = "휴대폰 캘린더에 추가",
    remindMe = "미리 알림", chooseLanguage = "언어 선택", followPhone = "시스템 설정 언어 사용", ok = "확인", cancel = "취소", address = "주소",
    editProfile = "프로필 수정", newPassword = "새 비밀번호", saveChanges = "변경사항 저장", systemDefault = "시스템 기본값", light = "밝은", dark = "어두운", matchYourPhone = "전화와 일치",
    currency = "통화", convert = "변환", 
    bookings = "예약", packing = "포장", money = "돈", gadget = "기기", other = "기타", needToAdd = "추가할 것이 있나요?", section = "섹션",
    amount = "금액", validAmount = "유효한 금액을 입력하세요", couldntFetch = "환율을 가져올 수 없습니다. 연결을 확인하세요.",
    fillAllFields = "모든 필드를 채워주세요", loginFailed = "로그인에 실패했습니다. 이메일과 비밀번호를 확인하세요.",


    add = "추가", addToItinerary = "일정에 추가", travel = "여행", place = "장소",
    newPlace = "새 장소", editPlace = "장소 편집", landmark = "랜드마크 (선택 사항)", savePlace = "장소 저장", searchTrips = "여행 검색",
    newTrip = "새 여행", renameTrip = "여행 이름 바꾸기", create = "만들기", save = "저장",
    pin = "고정", unpin = "고정 해제", edit = "편집", archive = "보관", unarchive = "보관 취소", 
    delete = "삭제", deleteTripQ = "여행을 삭제할까요?", deletePlaceQ = "장소를 삭제할까요?", deleteLegQ = "경로를 삭제할까요?", 
    cantBeUndone = "이 작업은 되돌릴 수 없습니다.", archivedTrips = "보관된 여행", noArchivedTrips = "보관된 여행이 없습니다", 
    noDatesYet = "날짜 없음", tripName = "여행 이름", aLegBetween = "사이의 경로", somewhereToVisit = "방문할 곳", 
)

private val RU = EN.copy(
    name = "Имя", surname = "Фамилия", dob = "Дата рождения", street = "Улица", city = "Город", 
    postelCode = "Почтовый индекс", appTagline = "Начало каждого путешествия!", 
    myTrips = "Мои путешествия", countries = "страны", legs = "пути", done = "выполнено",
    addLeg = "Добавить путь", newLeg = "Новый путь", from = "От", to = "До", date = "Дата", time = "Время", 
    bookingRef = "Бронирование", optional = "необязательный", saveLeg = "Сохранить", tickets = "Билеты", event = "Событие", 
    beforeYouGo = "До вылета", addItem = "Добавить элемент", settings = "Настройки", backupSync = "Backup & Sync", 
    appearance = "Внешний вид", language = "Язык", currencyUnits = "Валюта и единицы", exportTrips = "Экспорт путешествий", about = "О приложении",
    logOut = "Выйти", welcomeBack = "Добро пожаловать", logInToTrips = "Войдите в ваши путешествия", email = "E-mail", password = "Пароль", 
    forgotPassword = "Забыли пароль?", logIn = "Войти", continueGoogle = "Продолжить с Google", continueApple = "Продолжить с Apple", 
    newHere = "Новый?", createAccount = "Создать аккаунт", calendar = "Календарь", documents = "Документы", nextUp = "Следующий", 
    legsTravelled = "пути пройдено", syncWholeTrip = "Синхронизировать всю поездку", addToPhoneCalendar = "Добавить в календарь", 
    remindMe = "Напомнить", chooseLanguage = "Выбрать язык", followPhone = "Следовать языку телефона", ok = "OK", cancel = "Отмена", 
    address = "Адрес", editProfile = "Редактировать профиль", newPassword = "Новый пароль", saveChanges = "Сохранить изменения",
    systemDefault = "Системный по умолчанию", light = "Светлый", dark = "Тёмный", matchYourPhone = "Совместить с телефоном", currency = "Валюта",
    transport = "Транспорт", orDivider = "или", convert = "Перевести",
    bookings = "Бронирования", packing = "Упаковка", money = "Деньги", gadget = "Гаджет", other = "Другое", needToAdd = "Что нужно добавить?", section = "Секция",
    amount = "Сумма", validAmount = "Введите действительную сумму", couldntFetch = "Не удалось получить курс. Проверьте соединение.",
    fillAllFields = "Заполните все поля", loginFailed = "Вход не удался. Проверьте адрес электронной почты и пароль.",

    add = "Добавить", addToItinerary = "Добавить в маршрут", travel = "Путешествие", place = "Место",
    newPlace = "Новое место", editPlace = "Редактировать место", landmark = "Достопримечательность (необязательно)", savePlace = "Сохранить место", searchTrips = "Поиск путешествий",
    newTrip = "Новое путешествие", renameTrip = "Переименовать путешествие", create = "Создать", save = "Сохранить",
    pin = "Закрепить", unpin = "Открепить", edit = "Редактировать", archive = "Архивировать", unarchive = "Разархивировать", 
    delete = "Удалить", deleteTripQ = "Удалить путешествие?", deletePlaceQ = "Удалить место?", deleteLegQ = "Удалить путь?", 
    cantBeUndone = "Это действие нельзя отменить.", archivedTrips = "Архивные путешествия", noArchivedTrips = "Нет архивных путешествий", 
    noDatesYet = "Нет дат", tripName = "Название путешествия", aLegBetween = "Путь между", somewhereToVisit = "Место для посещения",
)

private val TAM = EN.copy(
    name = "பெயர்", surname = "பின்னொட்டு", dob = "பிறந்த தேதி", street = "தெரு", city = "நகரம்",
    postelCode = "அஞ்சல் குறியீடு", appTagline = "ஒவ்வொரு பயணமும் இங்கே தொடங்குகிறது!", myTrips = "எனது பயணங்கள்",
    countries = "நாடுகள்", legs = "பயணங்கள்", done = "முடிந்தது", addLeg = "பயணத்தைச் சேர்", newLeg = "புதிய பயணம்",
    from = "இருந்து", to = "வரை", transport = "போக்குவரத்து", date = "தேதி", time = "நேரம்",
    bookingRef = "முன்பதிவு எண்", optional = "விருப்பத்திற்குரியது", saveLeg = "பயணத்தைச் சேமி",
    tickets = "டிக்கெட்டுகள்", event = "நிகழ்வு", beforeYouGo = "செல்வதற்கு முன்", addItem = "பொருளைச் சேர்",
    settings = "அமைப்புகள்", backupSync = "காப்புப்பிரதி மற்றும் ஒத்திசைவு", appearance = "தோற்றம்",
    language = "மொழி", currencyUnits = "நாணயம் மற்றும் அலகுகள்", exportTrips = "பயணங்களை ஏற்றுமதி செய்",
    about = "பற்றி", logOut = "வெளியேறு", welcomeBack = "நல்வரவு", logInToTrips = "பயணங்களுக்கு உள்நுழையவும்",
    email = "மின்னஞ்சல்", password = "கடவுச்சொல்", forgotPassword = "கடவுச்சொல்லை மறந்துவிட்டீர்களா?",
    logIn = "உள்நுழை", orDivider = "அல்லது", continueGoogle = "Google மூலம் தொடரவும்",
    continueApple = "Apple மூலம் தொடரவும்", newHere = "புதியவரா?", createAccount = "கணக்கை உருவாக்கு",
    calendar = "நாட்காட்டி", documents = "ஆவணங்கள்", nextUp = "அடுத்தது", legsTravelled = "பயணம் செய்த தூரம்",
    syncWholeTrip = "முழு பயணத்தையும் நாட்காட்டியில் ஒத்திசைக்கவும்", addToPhoneCalendar = "போன் நாட்காட்டியில் சேர்",
    remindMe = "நினைவூட்டு", chooseLanguage = "மொழியைத் தேர்ந்தெடுக்கவும்", followPhone = "போன் மொழியைப் பின்பற்றவும்",
    ok = "சரி", cancel = "ரத்துசெய்", address = "முகவரி", editProfile = "சுயவிவரத்தைத் திருத்து",
    newPassword = "புதிய கடவுச்சொல்", saveChanges = "மாற்றங்களைச் சேமி", systemDefault = "கணினி இயல்புநிலை",
    light = "ஒளி", dark = "இருள்", matchYourPhone = "உங்கள் தொலைபேசியுடன் பொருந்தவும்", currency = "நாணயம்", convert = "மாற்று",
    bookings = "முன்பதிவுகள்", packing = "பேக்கிங்", money = "பணம்", gadget = "கேஜெட்", other = "மற்றவை", needToAdd = "நீங்கள் என்ன சேர்க்க வேண்டும்?", section = "பிரிவு",
    amount = "தொகை", validAmount = "செல்லுபடியாகும் தொகையை உள்ளிடவும்", couldntFetch = "விகிதத்தைப் பெற முடியவில்லை. உங்கள் இணைப்பைச் சரிபார்க்கவும்.",

    add = "சேர்", addToItinerary = "பயணத்திட்டத்தில் சேர்", travel = "பயணம்", place = "இடம்",
    newPlace = "புதிய இடம்", editPlace = "இடத்தைத் திருத்து", landmark = "அடையாளம் (விருப்பத்திற்குரியது)", savePlace = "இடத்தைச் சேமி", searchTrips = "பயணங்களைத் தேடு",
    newTrip = "புதிய பயணம்", renameTrip = "பயணப் பெயரை மாற்று", create = "உருவாக்கு", save = "சேமி",
    pin = "பின் செய்", unpin = "பின்னை நீக்கு", edit = "திருத்து", archive = "காப்பகப்படுத்து", unarchive = "காப்பகத்திலிருந்து எடு",
    delete = "நீக்கு", deleteTripQ = "பயணத்தை நீக்கவா?", deletePlaceQ = "இடத்தை நீக்கவா?", deleteLegQ = "பயணத்தை நீக்கவா?",
    cantBeUndone = "இதை மாற்ற முடியாது.", archivedTrips = "காப்பகப்படுத்தப்பட்ட பயணங்கள்", noArchivedTrips = "காப்பகப்படுத்தப்பட்ட பயணங்கள் இல்லை",
    noDatesYet = "தேதிகள் இன்னும் இல்லை", tripName = "பயணப் பெயர்", aLegBetween = "இடையிலான பயணம்", somewhereToVisit = "சந்திக்க வேண்டிய இடம்",
)

private val HI = EN.copy(
    name = "नाम", surname = "सरनेम", dob = "जन्म तिथि", street = "गली/सड़क", city = "शहर",
    postelCode = "पिन कोड", appTagline = "जहाँ हर यात्रा शुरू होती है!", myTrips = "मेरी यात्राएँ",
    countries = "देश", legs = "पड़ाव", done = "पूरा हुआ", addLeg = "पड़ाव जोड़ें", newLeg = "नया पड़ाव",
    from = "से", to = "तक", transport = "परिवहन", date = "तारीख", time = "समय",
    bookingRef = "बुकिंग संदर्भ", optional = "वैकल्पिक", saveLeg = "पड़ाव सहेजें",
    tickets = "टिकट", event = "ईवेंट", beforeYouGo = "जाने से पहले", addItem = "आइटम जोड़ें",
    settings = "सेटिंग्स", backupSync = "बैकअप और सिंक", appearance = "प्रकटन",
    language = "भाषा", currencyUnits = "मुद्रा और इकाइयाँ", exportTrips = "यात्राएँ निर्यात करें",
    about = "ऐप के बारे में", logOut = "लॉग आउट", welcomeBack = "आपका स्वागत है",
    logInToTrips = "अपनी यात्राओं में लॉग इन करें", email = "ई-मेल", password = "पासवर्ड",
    forgotPassword = "पासवर्ड भूल गए?", logIn = "लॉग इन", orDivider = "या",
    continueGoogle = "Google के साथ जारी रखें", continueApple = "Apple के साथ जारी रखें",
    newHere = "यहाँ नए हैं?", createAccount = "खाता बनाएँ", calendar = "कैलेंडर",
    documents = "दस्तावेज़", nextUp = "अगला", legsTravelled = "तय किए गए पड़ाव",
    syncWholeTrip = "पूरी यात्रा कैलेंडर में सिंक करें", addToPhoneCalendar = "फोन कैलेंडर में जोड़ें",
    remindMe = "मुझे याद दिलाएं", chooseLanguage = "भाषा चुनें", followPhone = "फोन की भाषा का पालन करें",
    ok = "ठीक है", cancel = "रद्द करें", address = "पता", editProfile = "प्रोफ़ाइल संपादित करें",
    newPassword = "नया पासवर्ड", saveChanges = "परिवर्तन सहेजें", systemDefault = "सिस्टम डिफ़ॉल्ट",
    light = "लाइट", dark = "डार्क", matchYourPhone = "अपने फ़ोन से मिलान करें", currency = "मुप्रा", convert = "बदलें",
    bookings = "बुकिंग", packing = "पैकिंग", money = "मु", gadget = "गेट", other = "अन्य", needToAdd = "जान", section = "section",
    amount = "राशि", validAmount = "एक वैध राशि दर्ज करें", couldntFetch = "दर प्राप्त नहीं हो सकी। अपना कनेक्शन जांचें।",


    add = "जोड़ें", addToItinerary = "यात्रा कार्यक्रम में जोड़ें", travel = "यात्रा", place = "स्थान",
    newPlace = "नया स्थान", editPlace = "स्थान संपादित करें", landmark = "लैंडमार्क (वैकल्पिक)", savePlace = "स्थान सहेजें", searchTrips = "यात्राएं खोजें",
    newTrip = "नई यात्रा", renameTrip = "यात्रा का नाम बदलें", create = "बनाएं", save = "सहेजें",
    pin = "पिन करें", unpin = "अनपिन करें", edit = "संपादित करें", archive = "संग्रह करें", unarchive = "संग्रह से निकालें",
    delete = "मिटाएं", deleteTripQ = "यात्रा मिटाएं?", deletePlaceQ = "स्थान मिटाएं?", deleteLegQ = "पड़ाव मिटाएं?",
    cantBeUndone = "इसे वापस नहीं लिया जा सकता।", archivedTrips = "संग्रहीत यात्राएं", noArchivedTrips = "कोई संग्रहीत यात्रा नहीं",
    noDatesYet = "अभी कोई तारीख नहीं", tripName = "यात्रा का नाम", aLegBetween = "के बीच एक पड़ाव", somewhereToVisit = "घूमने की जगह",
)

private val UKR = EN.copy(
    name = "Ім'я", surname = "Прізвище", dob = "Дата народження", street = "Вулиця", city = "Місто",
    postelCode = "Поштовий індекс", appTagline = "Початок кожного відпочинку!", myTrips = "Мої відпочинки",
    countries = "країни", legs = "лінії", done = "виконано", addLeg = "Додати лінію", newLeg = "Нова лінія", from = "Від",
    to = "До", transport = "Транспорт", date = "Дата", time = "Час", bookingRef = "Резерваційний номер",
    optional = "необов'язковий", saveLeg = "Зберегти лінію", tickets = "Билеты", event = "Подія", beforeYouGo = "До відправлення",
    addItem = "Додати елемент", settings = "Налаштування", backupSync = "Backup & Sync", appearance = "Внешний вид",
    language = "Мова", currencyUnits = "Валюта та одиниці", exportTrips = "Експорт відпочинок", about = "Про додаток", logOut = "Вихід",
    welcomeBack = "Ласкаво просимо", logInToTrips = "Увійти у ваші відпочинки", email = "Електронна пошта", password = "Пароль",
    forgotPassword = "Забули пароль?", logIn = "Увійти", orDivider = "або", continueGoogle = "Продовжити з Google",
    continueApple = "Продовжити з Apple", newHere = "Новий?", createAccount = "Створити акаунт", calendar = "Календар", documents = "Документи",
    nextUp = "наступний", legsTravelled = "лінії відправлено", syncWholeTrip = "Синхронізувати весь відпочинок", addToPhoneCalendar = "Додати до календаря телефону",
    remindMe = "Нагадати", chooseLanguage = "Вибрати мову", followPhone = "Підтримувати мову телефону", ok = "OK", cancel = "Відміна", address = "Адреса", editProfile = "Редагувати профіль",
    newPassword = "Новий пароль", saveChanges = "Зберегти зміни", systemDefault = "Системний замовчування",    light = "Світловий", dark = "Темний", matchYourPhone = "Збігається з телефоном",
    currency = "Валюта", convert = "Конвертувати", bookings = "Бронювання", packing = "Упаковка", money = "Гроші", gadget = "Гаджет", other = "Інше", needToAdd = "Що вам потрібно додати?", section = "Розділ", 
    amount = "Сума", validAmount = "Введіть коректну суму", couldntFetch = "Не вдалося отримати курс. Перевірте з'єднання.", 

    add = "Додати", addToItinerary = "Додати до маршруту", travel = "Подорож", place = "Місце",
    newPlace = "Нове місце", editPlace = "Редагувати місце", landmark = "Орієнтир (необов'язково)", savePlace = "Зберегти місце", searchTrips = "Пошук подорожей",
    newTrip = "Нова подорож", renameTrip = "Перейменувати подорож", create = "Створити", save = "Зберегти",
    pin = "Закріпити", unpin = "Відкріпити", edit = "Редагувати", archive = "Архівувати", unarchive = "Розархівувати",
    delete = "Видалити", deleteTripQ = "Видалити подорож?", deletePlaceQ = "Видалити місце?", deleteLegQ = "Видалити лінію?",
    cantBeUndone = "Цю дію неможливо скасувати.", archivedTrips = "Архівні подорожі", noArchivedTrips = "Немає архівних подорожей",
    noDatesYet = "Немає дат", tripName = "Назва подорожі", aLegBetween = "Лінія між", somewhereToVisit = "Місце для відвідування",
)

private val HUN = EN.copy(
    name = "Név", surname = "Vezetéknév", dob = "Születési dátum", street = "Utca", city = "Város", postelCode = "Irányítószám",
    appTagline = "Minden utazási kezdése!", myTrips = "Saját utazások", countries = "ország", legs = "utazások", done = "kész",
    addLeg = "Útvonal hozzáadása", newLeg = "Új útvonal", from = "Kezdőpont", to = "Célpont", transport = "Transport", date = "Dátum", time = "Idő",
    bookingRef = "Foglalási azonosító", optional = "opcionális", saveLeg = "Útvonal mentése", tickets = "Tiketek", event = "Esemény", beforeYouGo = "Először az útmutató",
    addItem = "Elem hozzáadása", settings = "Beállítások", backupSync = "Backup & Sync", appearance = "Kiíró",
    language = "Nyelv", currencyUnits = "Valuta és mértékegység", exportTrips = "Utázek exportálása", about = "Rólunk", logOut = "Kijelentkezés",
    welcomeBack = "Üdvözlünk újra", logInToTrips = "Bejelentkezés az utazásokhoz", email = "E-mail", password = "Jelszó",
    forgotPassword = "Elfelejtettem a jelszavam?", logIn = "Bejelentkezés", orDivider = "vagy", continueGoogle = "Google-lel folytatás",
    continueApple = "Apple-lel folytatás", newHere = "Új?", createAccount = "Új fiók létrehozása", calendar = "Kalendar",
    documents = "Dokumentumok", nextUp = "következő", legsTravelled = "utazások hossza", syncWholeTrip = "Teljes utazási synchronizáció",
    addToPhoneCalendar = "Családi dátumhozzáadás", remindMe = "Rengeteg értesítés", chooseLanguage = "Nyelv választása",
    followPhone = "Teljesítjük a telefon nyelvét", ok = "OK", cancel = "Mégsem", address = "Cím",
    editProfile = "Profil szerkesztése", newPassword = "Új jelszó", saveChanges = "Mentés", systemDefault = "Rendszeri alapértelmezés",
    light = "Sötét", dark = "Sötét", matchYourPhone = "Teljesítjük a telefonunkat", currency = "Valuta", convert = "Konvertálás", bookings = "Foglalások", packing = "Csomagolás", money = "Pénz", gadget = "Kütyü", other = "Egyéb", needToAdd = "Mit kell hozzáadnia?", section = "Szakasz",
    amount = "Összeg", validAmount = "Adjon meg egy érvényes összeget", couldntFetch = "Nem sikerült lekérni az árfolyamot. Ellenőrizze a kapcsolatot.", 

    add = "Hozzáadás", addToItinerary = "Hozzáadás az útitervhez", travel = "Utazás", place = "Hely",
    newPlace = "Új hely", editPlace = "Hely szerkesztése", landmark = "Nevezetesség (opcionális)", savePlace = "Hely mentése", searchTrips = "Utazások keresése",
    newTrip = "Új utazás", renameTrip = "Utazás átnevezése", create = "Létrehozás", save = "Mentés",
    pin = "Rögzítés", unpin = "Rögzítés feloldása", edit = "Szerkesztés", archive = "Archiválás", unarchive = "Visszaállítás",
    delete = "Törlés", deleteTripQ = "Törli az utazást?", deletePlaceQ = "Törli a helyet?", deleteLegQ = "Törli az utat?",
    cantBeUndone = "Ez a művelet nem vonható vissza.", archivedTrips = "Archivált utazások", noArchivedTrips = "Nincsenek archivált utazások",
    noDatesYet = "Nincsenek dátumok", tripName = "Utazás neve", aLegBetween = "Egy út a következők között:", somewhereToVisit = "Valahol, amit érdemies meglátogatni",
)

private val RO = EN.copy(
    name = "Prenume", surname = "Nume", dob = "Data nașterii", street = "Stradă", city = "Oraș", postelCode = "Cod poștal",
    appTagline = "Unde începe fiecare călătorie!", myTrips = "Călătoriile mele", countries = "țări", legs = "etape", done = "finalizat",
    addLeg = "Adaugă etapă", newLeg = "Etapă nouă", from = "De la", to = "Până la", transport = "Transport", date = "Dată", time = "Oră",
    bookingRef = "Ref. rezervare", optional = "opțional", saveLeg = "Salvează etapa", tickets = "Bilete", event = "Eveniment", beforeYouGo = "Înainte de plecare",
    addItem = "Adaugă element", settings = "Setări", backupSync = "Backup și sincronizare", appearance = "Aspect",
    language = "Limbă", currencyUnits = "Monedă și unități", exportTrips = "Exportă călătoriile", about = "Despre", logOut = "Deconectare",
    welcomeBack = "Bine ai revenit", logInToTrips = "Conectează-te la călătorii", email = "E-mail", password = "Parolă",
    forgotPassword = "Ai uitat parola?", logIn = "Conectare", orDivider = "sau", continueGoogle = "Continuă cu Google",
    continueApple = "Continuă cu Apple", newHere = "Nou aici?", createAccount = "Creează cont", calendar = "Calendar",
    documents = "Documente", nextUp = "următoarea", legsTravelled = "etape parcurse", syncWholeTrip = "Sincronizează călătoria",
    addToPhoneCalendar = "Adaugă în calendar", remindMe = "Amintește-mi", chooseLanguage = "Alege o limbă",
    followPhone = "Urmează limba telefonului", ok = "OK", cancel = "Anulează", address = "Adresă",
    editProfile = "Editează profilul", newPassword = "Parolă nouă", saveChanges = "Salvează modificările", systemDefault = "Sistem default",
    light = "Luz", dark = "Umid", matchYourPhone = "Matchează cu telefonul", currency = "Monedă", convert = "Convertiți", bookings = "Rezervări", packing = "Ambalare", money = "Bani", gadget = "Gadget", other = "Altele", needToAdd = "Ce trebuie să adăugați?", section = "Secțiune",
    amount = "Sumă", validAmount = "Introduceți o sumă validă", couldntFetch = "Nu s-a putut obține cursul. Verificați conexiunea.",

    add = "Adaugă", addToItinerary = "Adaugă la itinerar", travel = "Călătorie", place = "Loc",
    newPlace = "Loc nou", editPlace = "Editează locul", landmark = "Punct de reper (opțional)", savePlace = "Salvează locul", searchTrips = "Caută călătorii",
    newTrip = "Călătorie nouă", renameTrip = "Redenumește călătoria", create = "Creează", save = "Salvează",
    pin = "Fixează", unpin = "Anulează fixarea", edit = "Editează", archive = "Arhivează", unarchive = "Dezarhivează",
    delete = "Șterge", deleteTripQ = "Ștergi călătoria?", deletePlaceQ = "Ștergi locul?", deleteLegQ = "Ștergi etapa?",
    cantBeUndone = "Această acțiune nu poate fi anulată.", archivedTrips = "Călătorii arhivate", noArchivedTrips = "Nicio călătorie arhivată",
    noDatesYet = "Nicio dată încă", tripName = "Numele călătoriei", aLegBetween = "O etapă între", somewhereToVisit = "Undeva de vizitat",
)

private val GR = EN.copy(
    name = "Όνομα", surname = "Επώνυμο", dob = "Ημερομηνία γέννησης", street = "Οδός", city = "Πόλη", postelCode = "Ταχυδρομικός κώδικας",
    appTagline = "Εκεί που ξεκινά κάθε ταξίδι!", myTrips = "Τα ταξίδια μου", countries = "χώρες", legs = "σκέλη", done = "ολοκληρώθηκε",
    addLeg = "Προσθήκη σκέλους", newLeg = "Νέο σκέλος", from = "Από", to = "Προς", transport = "Μεταφορά", date = "Ημερομηνία", time = "Ώρα",
    bookingRef = "Αναφ. κράτησης", optional = "προαιρετικό", saveLeg = "Αποθήκευση σκέλους", tickets = "Εισιτήρια", event = "Εκδήλωση", beforeYouGo = "Πριν ξεκινήσετε",
    addItem = "Προσθήκη αντικειμένου", settings = "Ρυθμίσεις", backupSync = "Αντίγραφα & συγχρονισμός", appearance = "Εμφάνιση",
    language = "Γλώσσα", currencyUnits = "Νόμισμα & μονάδες", exportTrips = "Εξαγωγή ταξιδιών", about = "Σχετικά", logOut = "Αποσύνδεση",
    welcomeBack = "Καλώς ήρθατε", logInToTrips = "Συνδεθείτε στα ταξίδια σας", email = "E-mail", password = "Κωδικός",
    forgotPassword = "Ξεχάσατε τον κωδικό;", logIn = "Σύνδεση", orDivider = "ή", continueGoogle = "Συνέχεια με Google",
    continueApple = "Συνέχεια με Apple", newHere = "Νέος εδώ;", createAccount = "Δημιουργία λογαριασμού", calendar = "Ημερολόγιο",
    documents = "Έγγραφα", nextUp = "επόμενο", legsTravelled = "σκέλη που διανύθηκαν", syncWholeTrip = "Συγχρονισμός ταξιδιού",
    addToPhoneCalendar = "Προσθήκη στο ημερολόγιο", remindMe = "Υπενθύμιση", chooseLanguage = "Επιλογή γλώσσας",
    followPhone = "Γλώσσα τηλεφώνου", ok = "OK", cancel = "Ακύρωση", address = "Διεύθυνση",
    editProfile = "Επεξεργασία προφίλ", newPassword = "Νέος κωδικός", saveChanges = "Αποθήκευση αλλαγών",
    systemDefault = "Προεπιλογή συστήματος", light = "Ανοιχτό", dark = "Σκούρο", matchYourPhone = "Ταιριάζει με το τηλέφωνό σας", currency = "Νόμισμα", convert = "Μετατροπή", bookings = "Κρατήσεις", packing = "Συσκευασία", money = "Χρήματα", gadget = "Gadget", other = "Άλλα", needToAdd = "Τι πρέπει να προσθέσετε;", section = "Ενότητα",
    amount = "Ποσό", validAmount = "Εισαγάγετε ένα έγκυρο ποσό", couldntFetch = "Δεν ήταν δυνατή η ανάκτηση της ισοτιμίας. Ελέγξτε τη σύνδεσή σας.",

    add = "Προσθήκη", addToItinerary = "Προσθήκη στο δρομολόγιο", travel = "Ταξίδι", place = "Τοποθεσία",
    newPlace = "Νέα τοποθεσία", editPlace = "Επεξεργασία τοποθεσίας", landmark = "Ορόσημο (προαιρετικό)", savePlace = "Αποθήκευση τοποθεσίας", searchTrips = "Αναζήτηση ταξιδιών",
    newTrip = "Νέο ταξίδι", renameTrip = "Μετονομασία ταξιδιού", create = "Δημιουργία", save = "Αποθήκευση",
    pin = "Καρφίτσωμα", unpin = "Ξεκαρφίτσωμα", edit = "Επεξεργασία", archive = "Αρχειοθέτηση", unarchive = "Αποαρχειοθέτηση",
    delete = "Διαγραφή", deleteTripQ = "Διαγραφή ταξιδιού;", deletePlaceQ = "Διαγραφή τοποθεσίας;", deleteLegQ = "Διαγραφή σκέλους;",
    cantBeUndone = "Αυτή η ενέργεια δεν αναιρείται.", archivedTrips = "Αρχειοθετημένα ταξίδια", noArchivedTrips = "Δεν υπάρχουν αρχειοθετημένα ταξίδια",
    noDatesYet = "Δεν υπάρχουν ημερομηνίες", tripName = "Όνομα ταξιδιού", aLegBetween = "Ένα σκέλος μεταξύ", somewhereToVisit = "Κάπου για επίσκεψη",
)

private val KA = EN.copy(
    name = "სახელი", surname = "გვარი", dob = "დაბადების თარიღი", street = "ქუჩა", city = "ქალაქი", postelCode = "საფოსტო ინდექსი",
    appTagline = "სადაც ყოველი მოგზაურობა იწყება!", myTrips = "ჩემი მოგზაურობები", countries = "ქვეყანა", legs = "ეტაპი", done = "დასრულებული",
    addLeg = "ეტაპის დამატება", newLeg = "ახალი ეტაპი", from = "დან", to = "მდე", transport = "ტრანსპორტი", date = "თარიღი", time = "დრო",
    bookingRef = "ჯავშნის კოდი", optional = "არასავალდებულო", saveLeg = "ეტაპის შენახვა", tickets = "ბილეთები", event = "ღონისძიება", beforeYouGo = "გამგზავრებამდე",
    addItem = "ნივთის დამატება", settings = "პარამეტრები", backupSync = "მარქაფი და სინქრონიზაცია", appearance = "ვიზუალი",
    language = "ენა", currencyUnits = "ვალუტა და ერთეულები", exportTrips = "მოგზაურობის ექსპორტი", about = "შესახებ", logOut = "გამოსვლა",
    welcomeBack = "მოგესალმებით", logInToTrips = "შედით თქვენს მოგზაურობებში", email = "ელ-ფოსტა", password = "პაროლი",
    forgotPassword = "დაგავიწყდათ პაროლი?", logIn = "შესვლა", orDivider = "ან", continueGoogle = "Google-ით გაგრძელება",
    continueApple = "Apple-ით გაგრძელება", newHere = "ახალი ხართ?", createAccount = "ანგარიშის შექმნა", calendar = "კალენდარი",
    documents = "დოკუმენტები", nextUp = "შემდეგი", legsTravelled = "გავლილი ეტაპები", syncWholeTrip = "მთელი მოგზაურობის სინქრონიზაცია",
    addToPhoneCalendar = "კალენდარში დამატება", remindMe = "შეხსენება", chooseLanguage = "ენის არჩევა",
    followPhone = "ტელეფონის ენის მიყოლა", ok = "OK", cancel = "გაუქმება", address = "მისამართი",
    editProfile = "პროფილის რედაქტირება", newPassword = "ახალი პაროლი", saveChanges = "ცვლილებების შენახვა",systemDefault = "სისტემური ნაგულისხმევი",
    light = "ღია", dark = "მუქი", matchYourPhone = "დაემთხვეს თქვენს ტელეფონს", currency = "ვალუტა", convert = "კონვერტაცია", bookings = "ჯავშნები", packing = "შეფუთვა", money = "ფული", gadget = "გაჯეტი", other = "სხვა", needToAdd = "რა უნდა დაამატოთ?", section = "სექცია",
    amount = "თანხა", validAmount = "შეიყვანეთ სწორი თანხა", couldntFetch = "კურსის მიღება ვერ მოხერხდა. შეამოწმეთ კავშირი.",

    add = "დამატება", addToItinerary = "მარშრუტში დამატება", travel = "მგზავრობა", place = "ადგილი",
    newPlace = "ახალი ადგილი", editPlace = "ადგილის რედაქტირება", landmark = "ღირსშესანიშნაობა (არასავალდებულო)", savePlace = "ადგილის შენახვა", searchTrips = "მოგზაურობის ძებნა",
    newTrip = "ახალი მოგზაურობა", renameTrip = "მოგზაურობის გადარქმევა", create = "შექმნა", save = "შენახვა",
    pin = "ჩამაგრება", unpin = "მოხსნა", edit = "რედაქტირება", archive = "არქივირება", unarchive = "ამოარქივება",
    delete = "წაშლა", deleteTripQ = "წაიშალოს მოგზაურობა?", deletePlaceQ = "წაიშალოს ადგილი?", deleteLegQ = "წაიშალოს ეტაპი?",
    cantBeUndone = "ეს ქმედება შეუქცევადია.", archivedTrips = "დაარქივებული მოგზაურობები", noArchivedTrips = "დაარქივებული მოგზაურობები არ არის",
    noDatesYet = "თარიღები ჯერ არ არის", tripName = "მოგზაურობის სახელი", aLegBetween = "ეტაპი შორის", somewhereToVisit = "ადგილი მოსანახულებლად",
)

private val BG = EN.copy(
    name = "Име", surname = "Фамилия", dob = "Дата на раждане", street = "Улица", city = "Град", postelCode = "Пощенски код",
    appTagline = "Където започва всяко пътуване!", myTrips = "Моите пътувания", countries = "държави", legs = "етапи", done = "завършено",
    addLeg = "Добави етап", newLeg = "Нов етап", from = "От", to = "До", transport = "Транспорт", date = "Дата", time = "Час",
    bookingRef = "Код за резервация", optional = "по избор", saveLeg = "Запази етапа", tickets = "Билети", event = "Събитие", beforeYouGo = "Преди да тръгнете",
    addItem = "Добави елемент", settings = "Настройки", backupSync = "Резервно копие & синхронизация", appearance = "Изглед",
    language = "Език", currencyUnits = "Валута и единици", exportTrips = "Експортирай пътуванията", about = "Относно", logOut = "Изход",
    welcomeBack = "Добре дошли отново", logInToTrips = "Влезте в пътуванията си", email = "E-mail", password = "Парола",
    forgotPassword = "Забравена парола?", logIn = "Вход", orDivider = "или", continueGoogle = "Продължи с Google",
    continueApple = "Продължи с Apple", newHere = "За първи път тук?", createAccount = "Създай акаунт", calendar = "Календар",
    documents = "Документи", nextUp = "следващо", legsTravelled = "изминати етапи", syncWholeTrip = "Синхронизирай цялото пътуване",
    addToPhoneCalendar = "Добави в календара", remindMe = "Напомни ми", chooseLanguage = "Избери език",
    followPhone = "Език на телефона", ok = "OK", cancel = "Отказ", address = "Адрес",
    editProfile = "Редактирай профила", newPassword = "Нова парола", saveChanges = "Запази промените",
    systemDefault = "Системен по подразбиране", light = "Свет", dark = "Нощ", matchYourPhone = "Съвместите с телефоном", currency = "Валута", convert = "Конвертиране", bookings = "Резервации", packing = "Опаковане", money = "Пари", gadget = "Гаджет", other = "Друго", needToAdd = "Какво трябва да добавите?", section = "Раздел",
    amount = "Сума", validAmount = "Въведете валидна сума", couldntFetch = "Неуспешно извличане на курса. Проверете връзката си.",

    add = "Добави", addToItinerary = "Добави към маршрута", travel = "Пътуване", place = "Място",
    newPlace = "Ново място", editPlace = "Редактирай мястото", landmark = "Забележителност (по избор)", savePlace = "Запази мястото", searchTrips = "Търсене на пътувания",
    newTrip = "Ново пътуване", renameTrip = "Преименувай пътуването", create = "Създай", save = "Запази",
    pin = "Закачи", unpin = "Откачи", edit = "Редактирай", archive = "Архивирай", unarchive = "Разархивирай",
    delete = "Изтрий", deleteTripQ = "Изтриване на пътуването?", deletePlaceQ = "Изтриване на мястото?", deleteLegQ = "Изтриване на етапа?",
    cantBeUndone = "Това не може да бъде отменено.", archivedTrips = "Архивирани пътувания", noArchivedTrips = "Няма архивирани пътувания",
    noDatesYet = "Все още няма дати", tripName = "Име на пътуването", aLegBetween = "Етап между", somewhereToVisit = "Място за посещение",
)

private val CZ = EN.copy(
    name = "Jméno", surname = "Příjmení", dob = "Datum narození", street = "Ulice", city = "Město", postelCode = "PSČ",
    appTagline = "Kde každá cesta začíná!", myTrips = "Moje cesty", countries = "země", legs = "etapy", done = "hotovo",
    addLeg = "Přidat etapu", newLeg = "Nová etapa", from = "Z", to = "Do", transport = "Doprava", date = "Datum", time = "Čas",
    bookingRef = "Rezervační kód", optional = "volitelné", saveLeg = "Uložit etapu", tickets = "Jízdenky", event = "Událost", beforeYouGo = "Než vyrazíte",
    addItem = "Přidat položku", settings = "Nastavení", backupSync = "Záloha a synchronizace", appearance = "Vzhled",
    language = "Jazyk", currencyUnits = "Měna a jednotky", exportTrips = "Exportovat cesty", about = "O aplikaci", logOut = "Odhlásit se",
    welcomeBack = "Vítejte zpět", logInToTrips = "Přihlaste se ke svým cestám", email = "E-mail", password = "Heslo",
    forgotPassword = "Zapomenuté heslo?", logIn = "Přihlásit se", orDivider = "nebo", continueGoogle = "Pokračovat přes Google",
    continueApple = "Pokračovat přes Apple", newHere = "Poprvé zde?", createAccount = "Vytvořit účet", calendar = "Kalendář",
    documents = "Dokumenty", nextUp = "další", legsTravelled = "projeté etapy", syncWholeTrip = "Synchronizovat celou cestu",
    addToPhoneCalendar = "Přidat do kalendáře", remindMe = "Připomenout", chooseLanguage = "Vybrat jazyk",
    followPhone = "Podle jazyka telefonu", ok = "OK", cancel = "Zrušit", address = "Adresa",
    editProfile = "Upravit profil", newPassword = "Nové heslo", saveChanges = "Uložit změny",
    systemDefault = "Systémový výchozí", light = "Světlo", dark = "Tmavé", matchYourPhone = "Připadat s telefonem", currency = "Měna", convert = "Převést", bookings = "Rezervace", packing = "Balení", money = "Peníze", gadget = "Gadget", other = "Ostatní", needToAdd = "Co potřebujete přidat?", section = "Sekce",
    amount = "Částka", validAmount = "Zadejte platnou částku", couldntFetch = "Nepodařilo se načíst kurz. Zkontrolujte připojení.",

    add = "Přidat", addToItinerary = "Přidat do itineráře", travel = "Cesta", place = "Místo",
    newPlace = "Nové místo", editPlace = "Upravit místo", landmark = "Záchytný bod (volitelné)", savePlace = "Uložit místo", searchTrips = "Hledat cesty",
    newTrip = "Nová cesta", renameTrip = "Přejmenovat cestu", create = "Vytvořit", save = "Uložit",
    pin = "Připnout", unpin = "Odepnout", edit = "Upravit", archive = "Archivovat", unarchive = "Obnovit z archivu",
    delete = "Smazat", deleteTripQ = "Smazat cestu?", deletePlaceQ = "Smazat místo?", deleteLegQ = "Smazat etapu?",
    cantBeUndone = "Tuto akci nelze vzít zpět.", archivedTrips = "Archivované cesty", noArchivedTrips = "Žádné archivované cesty",
    noDatesYet = "Zatím žádná data", tripName = "Název cesty", aLegBetween = "Etapa mezi", somewhereToVisit = "Místo k návštěvě",
)

private val LV = EN.copy(
    name = "Vārds", surname = "Uzvārds", dob = "Dzimšanas datums", street = "Iela", city = "Pilsēta", postelCode = "Pasta indekss",
    appTagline = "Kur sākas katrs ceļojums!", myTrips = "Mani ceļojumi", countries = "valstis", legs = "posmi", done = "pabeigts",
    addLeg = "Pievienot posmu", newLeg = "Jauns posms", from = "No", to = "Uz", transport = "Transports", date = "Datums", time = "Laiks",
    bookingRef = "Rezervācijas kods", optional = "neobligāti", saveLeg = "Saglabāt posmu", tickets = "Biļetes", event = "Pasākums", beforeYouGo = "Pirms došanās",
    addItem = "Pievienot vienumu", settings = "Iestatījumi", backupSync = "Dublēšana & sinhronizācija", appearance = "Izskats",
    language = "Valoda", currencyUnits = "Valūta & mērvienības", exportTrips = "Eksportēt ceļojumus", about = "Par lietotni", logOut = "Izrakstīties",
    welcomeBack = "Sveicināti atpakaļ", logInToTrips = "Pieteikties savos ceļojumos", email = "E-pasts", password = "Parole",
    forgotPassword = "Aizmirsu paroli?", logIn = "Pieteikties", orDivider = "vai", continueGoogle = "Turpināt ar Google",
    continueApple = "Turpināt ar Apple", newHere = "Esi šeit pirmo reizi?", createAccount = "Izveidot kontu", calendar = "Kalendārs",
    documents = "Dokumenti", nextUp = "nākamais", legsTravelled = "veiktie posmi", syncWholeTrip = "Sinhronizēt visu ceļojumu",
    addToPhoneCalendar = "Pievienot kalendāram", remindMe = "Atgādināt man", chooseLanguage = "Izvēlēties valodu",
    followPhone = "Sekot tālruņa valodai", ok = "Labi", cancel = "Atcelt", address = "Adrese",
    editProfile = "Rediģēt profilu", newPassword = "Jauna parole", saveChanges = "Saglabāt izmaiņas",    systemDefault = "Sistēmas noklusējuma",
    light = "Gaišs", dark = "Tumšs", matchYourPhone = "Atbilst tālrunim", currency = "Valūta", convert = "Konvertēt", bookings = "Rezervācijas", packing = "Iepakošana", money = "Nauda", gadget = "Sīkrīks", other = "Cits", needToAdd = "Ko jums nepieciešams pievienot?", section = "Sadaļa",
    amount = "Summa", validAmount = "Ievadiet derīgu summu", couldntFetch = "Neizdevās iegūt kursu. Pārbaudiet savienojumu.",

    add = "Pievienot", addToItinerary = "Pievienot maršrutam", travel = "Ceļojums", place = "Vieta",
    newPlace = "Jauna vieta", editPlace = "Rediģēt vietu", landmark = "Ievērojama vieta (neobligāti)", savePlace = "Saglabāt vietu", searchTrips = "Meklēt ceļojumus",
    newTrip = "Jauns ceļojums", renameTrip = "Pārdēvēt ceļojumu", create = "Izveidot", save = "Saglabāt",
    pin = "Piespraust", unpin = "Atspraust", edit = "Rediģēt", archive = "Arhivēt", unarchive = "Atarhivēt",
    delete = "Dzēst", deleteTripQ = "Dzēst ceļojumu?", deletePlaceQ = "Dzēst vietu?", deleteLegQ = "Dzēst posmu?",
    cantBeUndone = "Šo darbību nevar atsaukt.", archivedTrips = "Arhivētie ceļojumi", noArchivedTrips = "Nav arhivētu ceļojumu",
    noDatesYet = "Vēl nav datumu", tripName = "Ceļojuma nosaukums", aLegBetween = "Posms starp", somewhereToVisit = "Vieta, ko apmeklēt",
)

private val LT = EN.copy(
    name = "Vardas", surname = "Pavardė", dob = "Gimimo data", street = "Gatvė", city = "Miestas", postelCode = "Pašto kodas",
    appTagline = "Kur prasideda kiekviena kelionė!", myTrips = "Mano kelionės", countries = "šalys", legs = "etapai", done = "atlikta",
    addLeg = "Pridėti etapą", newLeg = "Naujas etapas", from = "Iš", to = "Į", transport = "Transportas", date = "Data", time = "Laikas",
    bookingRef = "Rezervacijos kodas", optional = "nebūtina", saveLeg = "Išsaugoti etapą", tickets = "Bilietai", event = "Renginys", beforeYouGo = "Prieš išvykstant",
    addItem = "Pridėti elementą", settings = "Nustatymai", backupSync = "Atsarginė kopija & sinchronizacija", appearance = "Išvaizda",
    language = "Kalba", currencyUnits = "Valiuta & vienetai", exportTrips = "Eksportuoti keliones", about = "Apie", logOut = "Atsijungti",
    welcomeBack = "Sveiki sugrįžę", logInToTrips = "Prisijunkite prie savo kelionių", email = "El. paštas", password = "Slaptažodis",
    forgotPassword = "Pamiršote slaptažodį?", logIn = "Prisijungti", orDivider = "arba", continueGoogle = "Tęsti su „Google“",
    continueApple = "Tęsti su „Apple“", newHere = "Pirmą kartą čia?", createAccount = "Sukurti paskyrą", calendar = "Kalendorius",
    documents = "Dokumentai", nextUp = "kitas", legsTravelled = "nukeliauti etapai", syncWholeTrip = "Sinchronizuoti visą kelionę",
    addToPhoneCalendar = "Pridėti į kalendorių", remindMe = "Priminti man", chooseLanguage = "Pasirinkite kalbą",
    followPhone = "Naudoti telefono kalbą", ok = "Gerai", cancel = "Atšaukti", address = "Adresas",
    editProfile = "Redaguoti profilį", newPassword = "Naujas slaptažodis", saveChanges = "Išsaugoti pakeitimus", systemDefault = "Sistėmos nustatymai",
    light = "Šviesi", dark = "Tamsi", matchYourPhone = "Pagal telefoną", currency = "Valiuta", convert = "Konvertuoti", bookings = "Rezervacijos", packing = "Pakavimas", money = "Pinigai", gadget = "Programėlė", other = "Kita", needToAdd = "Ką jums reikia pridėti?", section = "Skyrius",
    amount = "Suma", validAmount = "Įveskite galiojančią sumą", couldntFetch = "Nepavyko gauti kurso. Patikrinkite ryšį.",

    add = "Pridėti", addToItinerary = "Pridėti į maršrutą", travel = "Kelionė", place = "Vieta",
    newPlace = "Nauja vieta", editPlace = "Redaguoti vietą", landmark = "Lankytina vieta (nebūtina)", savePlace = "Išsaugoti vietą", searchTrips = "Ieškoti kelionių",
    newTrip = "Nauja kelionė", renameTrip = "Pervadinti kelionę", create = "Sukurti", save = "Išsaugoti",
    pin = "Prisegti", unpin = "Atsegti", edit = "Redaguoti", archive = "Archyvuoti", unarchive = "Išarchyvuoti",
    delete = "Ištrinti", deleteTripQ = "Ištrinti kelionę?", deletePlaceQ = "Ištrinti vietą?", deleteLegQ = "Ištrinti etapą?",
    cantBeUndone = "Šio veiksmo negalima atšaukti.", archivedTrips = "Archyvuotos kelionės", noArchivedTrips = "Nėra archyvuotų kelionių",
    noDatesYet = "Dar nėra datų", tripName = "Kelionės pavadinimas", aLegBetween = "Etapas tarp", somewhereToVisit = "Vieta apsilankymui",
)

private val SK = EN.copy(
    name = "Meno", surname = "Priezvisko", dob = "Dátum narodenia", street = "Ulica", city = "Mesto", postelCode = "PSČ",
    appTagline = "Kde každá cesta začína!", myTrips = "Moje cesty", countries = "krajiny", legs = "etapy", done = "hotovo",
    addLeg = "Pridať etapu", newLeg = "Nová etapa", from = "Z", to = "Do", transport = "Doprava", date = "Dátum", time = "Čas",
    bookingRef = "Rezervačný kód", optional = "voliteľné", saveLeg = "Uložiť etapu", tickets = "Lístky", event = "Udalosť", beforeYouGo = "Skôr než vyrazíte",
    addItem = "Pridať položku", settings = "Nastavenia", backupSync = "Záloha a synchronizácia", appearance = "Vzhľad",
    language = "Jazyk", currencyUnits = "Mena a jednotky", exportTrips = "Exportovať cesty", about = "O aplikácii", logOut = "Odhlásiť sa",
    welcomeBack = "Vitajte späť", logInToTrips = "Prihláste sa k svojim cestám", email = "E-mail", password = "Heslo",
    forgotPassword = "Zabudnuté heslo?", logIn = "Prihlásiť sa", orDivider = "alebo", continueGoogle = "Pokračovať cez Google",
    continueApple = "Pokračovať cez Apple", newHere = "Prvýkrát tu?", createAccount = "Vytvoriť účet", calendar = "Kalendár",
    documents = "Dokumenty", nextUp = "ďalšie", legsTravelled = "prejdené etapy", syncWholeTrip = "Synchronizovať celú cestu",
    addToPhoneCalendar = "Pridať do kalendára", remindMe = "Pripomenúť", chooseLanguage = "Vybrať jazyk",
    followPhone = "Podľa jazyka telefónu", ok = "OK", cancel = "Zrušiť", address = "Adresa",
    editProfile = "Upraviť profil", newPassword = "Nové heslo", saveChanges = "Uložiť zmeny", systemDefault = "Systémové výchozí",
    light = "Svetlý", dark = "Tmavý", matchYourPhone = "Podľa telefónu", currency = "Mena", convert = "Previesť", bookings = "Rezervácie", packing = "Balenie", money = "Peniaze", gadget = "Gadget", other = "Iné", needToAdd = "Čo potrebujete pridať?", section = "Sekcia",
    amount = "Suma", validAmount = "Zadajte platnú sumu", couldntFetch = "Nepodarilo sa načítať kurz. Skontrolujte pripojenie.",

    add = "Pridať", addToItinerary = "Pridať do itinerára", travel = "Cesta", place = "Miesto",
    newPlace = "Nové miesto", editPlace = "Upraviť miesto", landmark = "Záchytný bod (voliteľné)", savePlace = "Uložiť miesto", searchTrips = "Hľadať cesty",
    newTrip = "Nová cesta", renameTrip = "Premenovať cestu", create = "Vytvoriť", save = "Uložiť",
    pin = "Pripnúť", unpin = "Odpnúť", edit = "Upraviť", archive = "Archivovať", unarchive = "Obnoviť z archívu",
    delete = "Zmazať", deleteTripQ = "Zmazať cestu?", deletePlaceQ = "Zmazať miesto?", deleteLegQ = "Zmazať etapu?",
    cantBeUndone = "Túto akciu nie je možné vrátiť späť.", archivedTrips = "Archivované cesty", noArchivedTrips = "Žiadne archivované cesty",
    noDatesYet = "Zatiaľ žiadne dáta", tripName = "Názov cesty", aLegBetween = "Etapa medzi", somewhereToVisit = "Miesto na návštevu",
)

private val SL = EN.copy(
    name = "Ime", surname = "Priimek", dob = "Datum rojstva", street = "Ulica", city = "Mesto", postelCode = "Poštna številka",
    appTagline = "Kjer se vsako potovanje začne!", myTrips = "Moja potovanja", countries = "države", legs = "etape", done = "opravljeno",
    addLeg = "Dodaj etapo", newLeg = "Nova etapa", from = "Od", to = "Do", transport = "Prevoz", date = "Datum", time = "Ura",
    bookingRef = "Koda rezervacije", optional = "neobvezno", saveLeg = "Shrani etapo", tickets = "Vstopnice", event = "Dogodek", beforeYouGo = "Preden odidete",
    addItem = "Dodaj predmet", settings = "Nastavitve", backupSync = "Varnostna kopija & sinhronizacija", appearance = "Videz",
    language = "Jezik", currencyUnits = "Valuta & enote", exportTrips = "Izvozi potovanja", about = "O aplikaciji", logOut = "Odjava",
    welcomeBack = "Dobrodošli nazaj", logInToTrips = "Prijavite se v svoja potovanja", email = "E-pošta", password = "Geslo",
    forgotPassword = "Pozabljeno geslo?", logIn = "Prijava", orDivider = "ali", continueGoogle = "Nadaljuj z Googlom",
    continueApple = "Nadaljuj z Applom", newHere = "Ste prvič tukaj?", createAccount = "Ustvari račun", calendar = "Koledar",
    documents = "Dokumenti", nextUp = "naslednje", legsTravelled = "prepotovane etape", syncWholeTrip = "Sinhroniziraj celotno potovanje",
    addToPhoneCalendar = "Dodaj v koledar", remindMe = "Opomni me", chooseLanguage = "Izberi jezik",
    followPhone = "Sledi jeziku telefona", ok = "V redu", cancel = "Prekliči", address = "Naslov",
    editProfile = "Uredi profil", newPassword = "Novo geslo", saveChanges = "Shrani spremembe",    systemDefault = "Sistemsko privzeto",
    light = "Svetlo", dark = "Temno", matchYourPhone = "Ujemaj s telefonom", currency = "Valuta", convert = "Pretvori", bookings = "Rezervacije", packing = "Pakiranje", money = "Denar", gadget = "Pripomoček", other = "Drugo", needToAdd = "Kaj morate dodati?", section = "Oddelek",
    amount = "Znesek", validAmount = "Vnesite veljaven znesek", couldntFetch = "Ni bilo mogoče pridobiti tečaja. Preverite povezavo.",

    add = "Dodaj", addToItinerary = "Dodaj v načrt poti", travel = "Potovanje", place = "Kraj",
    newPlace = "Nov kraj", editPlace = "Uredi kraj", landmark = "Znamenitost (neobvezno)", savePlace = "Shrani kraj", searchTrips = "Išči potovanja",
    newTrip = "Novo potovanje", renameTrip = "Preimenuj potovanje", create = "Ustvari", save = "Shrani",
    pin = "Pripni", unpin = "Odpni", edit = "Uredi", archive = "Arhiviraj", unarchive = "Obnovi",
    delete = "Izbriši", deleteTripQ = "Izbrišem potovanje?", deletePlaceQ = "Izbrišem kraj?", deleteLegQ = "Izbrišem etapo?",
    cantBeUndone = "Tega ni mogoče preklicati.", archivedTrips = "Arhivirana potovanja", noArchivedTrips = "Ni arhiviranih potovanj",
    noDatesYet = "Ni še datumov", tripName = "Ime potovanja", aLegBetween = "Etapa med", somewhereToVisit = "Kraj za obisk",
)

private val SR = EN.copy(
    name = "Ime", surname = "Prezime", dob = "Datum rođenja", street = "Ulica", city = "Grad", postelCode = "Poštanski broj",
    appTagline = "Gde svako putovanje počinje!", myTrips = "Moja putovanja", countries = "države", legs = "etape", done = "završeno",
    addLeg = "Dodaj etapu", newLeg = "Nova etapa", from = "Od", to = "Do", transport = "Prevoz", date = "Datum", time = "Vreme",
    bookingRef = "Kod rezervacije", optional = "opciono", saveLeg = "Sačuvaj etapu", tickets = "Karte", event = "Događaj", beforeYouGo = "Pre nego što krenete",
    addItem = "Dodaj stavku", settings = "Podešavanja", backupSync = "Rezervna kopija & sinhronizacija", appearance = "Izgled",
    language = "Jezik", currencyUnits = "Valuta & jedinice", exportTrips = "Izvezi putovanja", about = "O aplikaciji", logOut = "Odjava",
    welcomeBack = "Dobro došli nazad", logInToTrips = "Prijavite se u svoja putovanja", email = "E-pošta", password = "Lozinka",
    forgotPassword = "Zaboravljena lozinka?", logIn = "Prijava", orDivider = "ili", continueGoogle = "Nastavi putem Google-a",
    continueApple = "Nastavi putem Apple-a", newHere = "Novi ste ovde?", createAccount = "Napravi nalog", calendar = "Kalendar",
    documents = "Dokumenti", nextUp = "sledeće", legsTravelled = "pređene etape", syncWholeTrip = "Sinhronizuj celo putovanje",
    addToPhoneCalendar = "Dodaj u kalendar", remindMe = "Podseti me", chooseLanguage = "Izaberi jezik",
    followPhone = "Prati jezik telefona", ok = "U redu", cancel = "Otkaži", address = "Adresa",
    editProfile = "Uredi profil", newPassword = "Nova lozinka", saveChanges = "Sačuvaj izmene",
    systemDefault = "Системски подразумевано", light = "Светло", dark = "Тамνο", matchYourPhone = "Према телефону", currency = "Валута", convert = "Konvertuj", bookings = "Rezervacije", packing = "Pakovanje", money = "Novac", gadget = "Gedžet", other = "Ostalo", needToAdd = "Šta treba da dodate?", section = "Odjeljak",
    amount = "Износ", validAmount = "Унесите важећи износ", couldntFetch = "Није могуће преузети курс. Проверите везу.",

    add = "Dodaj", addToItinerary = "Dodaj u plan puta", travel = "Putovanje", place = "Mesto",
    newPlace = "Novo mesto", editPlace = "Izmeni mesto", landmark = "Obeležje (opciono)", savePlace = "Sačuvaj mesto", searchTrips = "Pretraži putovanja",
    newTrip = "Novo putovanje", renameTrip = "Preimenuj putovanje", create = "Napravi", save = "Sačuvaj",
    pin = "Zakači", unpin = "Otkači", edit = "Izmeni", archive = "Arhiviraj", unarchive = "Povrati",
    delete = "Obriši", deleteTripQ = "Obrisati putovanje?", deletePlaceQ = "Obrisati mesto?", deleteLegQ = "Obrisati etapu?",
    cantBeUndone = "Ovo se ne može poništiti.", archivedTrips = "Arhivirana putovanja", noArchivedTrips = "Nema arhiviranih putovanja",
    noDatesYet = "Još nema datuma", tripName = "Naziv putovanja", aLegBetween = "Etapa između", somewhereToVisit = "Mesto za posetu",
)

private val SQ = EN.copy(
    name = "Emri", surname = "Mbiemri", dob = "Datëlindja", street = "Rruga", city = "Qyteti", postelCode = "Kodi postar",
    appTagline = "Ku çdo udhëtim fillon!", myTrips = "Udhëtimet e mia", countries = "shtete", legs = "etapa", done = "përfunduar",
    addLeg = "Shto etapë", newLeg = "Etapë e re", from = "Nga", to = "Te", transport = "Transporti", date = "Data", time = "Ora",
    bookingRef = "Ref. i rezervimit", optional = "opsionale", saveLeg = "Ruaj etapën", tickets = "Bileta", event = "Event", beforeYouGo = "Para se të niseni",
    addItem = "Shto artikull", settings = "Cilësimet", backupSync = "Rezervimi & sinkronizimi", appearance = "Pamja",
    language = "Gjuha", currencyUnits = "Valuta & njësitë", exportTrips = "Eksporto udhëtimet", about = "Rreth", logOut = "Dil",
    welcomeBack = "Mirë se vini përsëri", logInToTrips = "Hyni në udhëtimet tuaja", email = "E-mail", password = "Fjalëkalimi",
    forgotPassword = "Harruat fjalëkalimin?", logIn = "Hyni", orDivider = "ose", continueGoogle = "Vazhdo me Google",
    continueApple = "Vazhdo me Apple", newHere = "I ri këtu?", createAccount = "Krijo llogari", calendar = "Kalendari",
    documents = "Dokumente", nextUp = "e radhës", legsTravelled = "etapa të bëra", syncWholeTrip = "Sinkronizo udhëtimin",
    addToPhoneCalendar = "Shto në kalendar", remindMe = "Më kujto", chooseLanguage = "Zgjidh një gjuhë",
    followPhone = "Ndiq gjuhën e telefonit", ok = "OK", cancel = "Anulo", address = "Adresa",
    editProfile = "Ndrysho profilin", newPassword = "Fjalëkalim i ri", saveChanges = "Ruaj ndryshimet",
    systemDefault = "Parazgjedhja e sistemit", light = "E çelët", dark = "E errët", matchYourPhone = "Përputh me telefonin", currency = "Monedha", convert = "Konverto", bookings = "Rezervime", packing = "Paketim", money = "Para", gadget = "Gadget", other = "Tjetër", needToAdd = "Çfarë ju duhet të shtoni?", section = "Seksioni",
    amount = "Shuma", validAmount = "Vendosni një shumë të vlefshme", couldntFetch = "Nuk u mor dot kursi. Kontrolloni lidhjen.",

    add = "Shto", addToItinerary = "Shto në itinerar", travel = "Udhëtim", place = "Vendi",
    newPlace = "Vend i ri", editPlace = "Edito vendin", landmark = "Pikë referimi (opsionale)", savePlace = "Ruaj vendin", searchTrips = "Kërko udhëtime",
    newTrip = "Udhëtim i ri", renameTrip = "Riemëro udhëtimin", create = "Krijo", save = "Ruaj",
    pin = "Fikso", unpin = "Hiq fiksimin", edit = "Edito", archive = "Arkivo", unarchive = "Hiq nga arkiva",
    delete = "Fshij", deleteTripQ = "Fshij udhëtimin?", deletePlaceQ = "Fshij vendin?", deleteLegQ = "Fshij etapën?",
    cantBeUndone = "Kjo nuk mund të kthehet mbrapsht.", archivedTrips = "Udhëtime të arkivuara", noArchivedTrips = "Nuk ka udhëtime të arkivuara",
    noDatesYet = "Nuk ka data ende", tripName = "Emri i udhëtimit", aLegBetween = "Një etapë mes", somewhereToVisit = "Diku për të vizituar",
)

private val MK = EN.copy(
    name = "Име", surname = "Презиме", dob = "Датум на раѓање", street = "Улица", city = "Град", postelCode = "Поштенски код",
    appTagline = "Каде што започнува секое патување!", myTrips = "Мои патувања", countries = "земји", legs = "етапи", done = "завршено",
    addLeg = "Додај етапа", newLeg = "Нова етапа", from = "Од", to = "До", transport = "Транспорт", date = "Датум", time = "Време",
    bookingRef = "Код за резервација", optional = "опционално", saveLeg = "Зачувај етапа", tickets = "Билети", event = "Настан", beforeYouGo = "Пред да тръгнете",
    addItem = "Додај ставка", settings = "Поставки", backupSync = "Резервна копија & синхронизација", appearance = "Изглед",
    language = "Јазик", currencyUnits = "Валута и единици", exportTrips = "Експортирај патувања", about = "За апликацијата", logOut = "Одјава",
    welcomeBack = "Добредојдовте назад", logInToTrips = "Најавете се во вашите патувања", email = "E-mail", password = "Лозинка",
    forgotPassword = "Заборавена лозинка?", logIn = "Најава", orDivider = "или", continueGoogle = "Продолжи со Google",
    continueApple = "Продолжи со Apple", newHere = "Нов тука?", createAccount = "Креирај сметка", calendar = "Календар",
    documents = "Документи", nextUp = "следно", legsTravelled = "поминати етапи", syncWholeTrip = "Синхронизирај го целото патување",
    addToPhoneCalendar = "Додај во календар", remindMe = "Потсети ме", chooseLanguage = "Избери јазик",
    followPhone = "Следи го јазикот на телефонот", ok = "Во ред", cancel = "Откажи", address = "Адреса",
    editProfile = "Уреди профил", newPassword = "Нова лозинка", saveChanges = "Зачувај промени",
    systemDefault = "Системски стандард", light = "Светло", dark = "Темно", matchYourPhone = "Според телефонот", currency = "Валута", convert = "Конвертирај", bookings = "Резервации", packing = "Пакување", money = "Пари", gadget = "Гаджет", other = "Друго", needToAdd = "Што треба да додадете?", section = "Секција",
    amount = "Износ", validAmount = "Внесете валиден износ", couldntFetch = "Не може да се преземе курсот. Проверете ја врската.",

    add = "Додај", addToItinerary = "Додај во итинерарот", travel = "Патување", place = "Место",
    newPlace = "Ново место", editPlace = "Уреди место", landmark = "Знаменитост (опционално)", savePlace = "Зачувај место", searchTrips = "Пребарај патувања",
    newTrip = "Ново патување", renameTrip = "Преименувај патување", create = "Креирај", save = "Зачувај",
    pin = "Закачи", unpin = "Откачи", edit = "Уреди", archive = "Архивирај", unarchive = "Врати од архива",
    delete = "Избриши", deleteTripQ = "Избриши патување?", deletePlaceQ = "Избриши место?", deleteLegQ = "Избриши етапа?",
    cantBeUndone = "Ова не може да се врати.", archivedTrips = "Архивирани патувања", noArchivedTrips = "Нема архивирани патувања",
    noDatesYet = "Сè уште нема датуми", tripName = "Име на патувањето", aLegBetween = "Етапа помеѓу", somewhereToVisit = "Место за посета",
)

private val MO = EN.copy(
    name = "Prenume", surname = "Nume", dob = "Data nașterii", street = "Stradă", city = "Oraș", postelCode = "Cod poștal",
    appTagline = "Unde începe fiecare călătorie!", myTrips = "Călătoriile mele", countries = "țări", legs = "etape", done = "finalizat",
    addLeg = "Adaugă etapă", newLeg = "Etapă nouă", from = "De la", to = "Până la", transport = "Transport", date = "Dată", time = "Oră",
    bookingRef = "Ref. rezervare", optional = "opțional", saveLeg = "Salvează etapa", tickets = "Bilete", event = "Eveniment", beforeYouGo = "Înainte de plecare",
    addItem = "Adaugă element", settings = "Setări", backupSync = "Backup și sincronizare", appearance = "Aspect",
    language = "Limbă", currencyUnits = "Monedă și unități", exportTrips = "Exportă călătoriile", about = "Despre", logOut = "Deconectare",
    welcomeBack = "Bine ai revenit", logInToTrips = "Conectează-te la călătorii", email = "E-mail", password = "Parolă",
    forgotPassword = "Ai uitat parola?", logIn = "Conectare", orDivider = "sau", continueGoogle = "Continuă cu Google",
    continueApple = "Continuă cu Apple", newHere = "Nou aici?", createAccount = "Creează cont", calendar = "Calendar",
    documents = "Documente", nextUp = "următoarea", legsTravelled = "etape parcurse", syncWholeTrip = "Sincronizează călătoria",
    addToPhoneCalendar = "Adaugă în calendar", remindMe = "Amintește-mi", chooseLanguage = "Alege o limbă",
    followPhone = "Urmează limba telefonului", ok = "OK", cancel = "Anulează", address = "Adresă",
    editProfile = "Editează profilul", newPassword = "Parolă nouă", saveChanges = "Salvează modificările",
    systemDefault = "Implicit de sistem", light = "Luminos", dark = "Întunecat", matchYourPhone = "Potrivește cu telefonul", currency = "Monedă", convert = "Convertiți", bookings = "Rezervări", packing = "Ambalare", money = "Bani", gadget = "Gadget", other = "Altele", needToAdd = "Ce trebuie să adăugați?", section = "Secțiune",
    amount = "Sumă", validAmount = "Introduceți o sumă validă", couldntFetch = "Nu s-a putut obține cursul. Verificați conexiunea.",

    add = "Adaugă", addToItinerary = "Adaugă la itinerar", travel = "Călătorie", place = "Loc",
    newPlace = "Loc nou", editPlace = "Editează locul", landmark = "Punct de reper (opțional)", savePlace = "Salvează locul", searchTrips = "Caută călătorii",
    newTrip = "Călătorie nouă", renameTrip = "Redenumește călătoria", create = "Creează", save = "Salvează",
    pin = "Fixează", unpin = "Anulează fixarea", edit = "Editează", archive = "Arhivează", unarchive = "Dezarhivează",
    delete = "Șterge", deleteTripQ = "Ștergi călătoria?", deletePlaceQ = "Ștergi locul?", deleteLegQ = "Ștergi etapa?",
    cantBeUndone = "Această acțiune nu poate fi anulată.", archivedTrips = "Călătorii arhivate", noArchivedTrips = "Nicio călătorie arhivată",
    noDatesYet = "Nicio dată încă", tripName = "Numele călătoriei", aLegBetween = "O etapă între", somewhereToVisit = "Undeva de vizitat",
)

private val NO = EN.copy(
    name = "Navn", surname = "Etternavn", dob = "Fødselsdato", street = "Gate", city = "By", postelCode = "Postnummer",
    appTagline = "Hvor hver reise begynner!", myTrips = "Mine reiser", countries = "land", legs = "etapper", done = "ferdig",
    addLeg = "Legg til etappe", newLeg = "Ny etappe", from = "Fra", to = "Til", transport = "Transport", date = "Dato", time = "Tid",
    bookingRef = "Bestillingsref.", optional = "valgfritt", saveLeg = "Lagre etappe", tickets = "Billetter", event = "Hendelse", beforeYouGo = "Før du drar",
    addItem = "Legg til element", settings = "Innstillinger", backupSync = "Sikkerhetskopi & synk", appearance = "Utseende",
    language = "Språk", currencyUnits = "Valuta & enheter", exportTrips = "Eksporter reiser", about = "Om", logOut = "Logg ut",
    welcomeBack = "Velkommen tilbake", logInToTrips = "Logg inn på reisene dine", email = "E-post", password = "Passord",
    forgotPassword = "Glemt passord?", logIn = "Logg inn", orDivider = "eller", continueGoogle = "Fortsett med Google",
    continueApple = "Fortsett med Apple", newHere = "Ny her?", createAccount = "Opprett konto", calendar = "Kalender",
    documents = "Dokumenter", nextUp = "neste", legsTravelled = "etapper reist", syncWholeTrip = "Synkroniser hele reisen",
    addToPhoneCalendar = "Legg til i kalender", remindMe = "Påminn meg", chooseLanguage = "Velg et språk",
    followPhone = "Følg telefonens språk", ok = "OK", cancel = "Avbryt", address = "Adresse",
    editProfile = "Rediger profil", newPassword = "Nytt passord", saveChanges = "Lagre endringer",
    systemDefault = "Systemstandard", light = "Lys", dark = "Mørk", matchYourPhone = "Følg telefonen", currency = "Valuta", convert = "Konverter", bookings = "Bestillinger", packing = "Pakking", money = "Penger", gadget = "Gadget", other = "Annet", needToAdd = "Hva trenger du å legge til?", section = "Seksjon",
    amount = "Beløp", validAmount = "Skriv inn et gyldig beløp", couldntFetch = "Kunne ikke hente kurs. Sjekk tilkoblingen.",

    add = "Legg til", addToItinerary = "Legg til i reiseplan", travel = "Reise", place = "Sted",
    newPlace = "Nytt sted", editPlace = "Rediger sted", landmark = "Landemerke (valgfritt)", savePlace = "Lagre sted", searchTrips = "Søk i reiser",
    newTrip = "Ny reise", renameTrip = "Gi reisen nytt navn", create = "Opprett", save = "Lagre",
    pin = "Fest", unpin = "Løsne", edit = "Rediger", archive = "Arkiver", unarchive = "Gjenopprett",
    delete = "Slett", deleteTripQ = "Slett reisen?", deletePlaceQ = "Slett stedet?", deleteLegQ = "Slett etappen?",
    cantBeUndone = "Dette kan ikke angres.", archivedTrips = "Arkiverte reiser", noArchivedTrips = "Ingen arkiverte reiser",
    noDatesYet = "Ingen datoer ennå", tripName = "Navn på reisen", aLegBetween = "En etappe mellom", somewhereToVisit = "Et sted å besøke",
)

private val FI = EN.copy(
    name = "Etunimi", surname = "Sukunimi", dob = "Syntymäaika", street = "Katu", city = "Kaupunki", postelCode = "Postinumero",
    appTagline = "Mistä jokainen matka alkaa!", myTrips = "Omat matkat", countries = "maat", legs = "osuudet", done = "valmis",
    addLeg = "Lisää osuus", newLeg = "Uusi osuus", from = "Mistä", to = "Minne", transport = "Kuljetus", date = "Päivämäärä", time = "Aika",
    bookingRef = "Varaustunnus", optional = "valinnainen", saveLeg = "Tallenna osuus", tickets = "Liput", event = "Tapahtuma", beforeYouGo = "Ennen lähtöä",
    addItem = "Lisää kohde", settings = "Asetukset", backupSync = "Varmuuskopio & synkronointi", appearance = "Ulkoasu",
    language = "Kieli", currencyUnits = "Valuutta ja yksiköt", exportTrips = "Vie matkat", about = "Tietoja", logOut = "Kirjaudu ulos",
    welcomeBack = "Tervetuloa takaisin", logInToTrips = "Kirjaudu matkoihisi", email = "Sähköposti", password = "Salasana",
    forgotPassword = "Unohditko salasanan?", logIn = "Kirjaudu sisään", orDivider = "tai", continueGoogle = "Jatka Googlella",
    continueApple = "Jatka Applella", newHere = "Uusi täällä?", createAccount = "Luo tili", calendar = "Kalenteri",
    documents = "Asiakirjat", nextUp = "seuraavaksi", legsTravelled = "matkustetut osuudet", syncWholeTrip = "Synkronoi koko matka",
    addToPhoneCalendar = "Lisää kalenteriin", remindMe = "Muistuta minua", chooseLanguage = "Valitse kieli",
    followPhone = "Seuraa puhelimen kieltä", ok = "OK", cancel = "Peruuta", address = "Osoite",
    editProfile = "Muokkaa profiilia", newPassword = "Uusi salasana", saveChanges = "Tallenna muutokset",
    systemDefault = "Järjestelmän oletus", light = "Vaalea", dark = "Tumma", matchYourPhone = "Seuraa puhelinta", currency = "Valuutta", convert = "Muunna", bookings = "Varaukset", packing = "Pakkaaminen", money = "Raha", gadget = "Gadget", other = "Muu", needToAdd = "Mitä sinun tarvitsee lisätä?", section = "Osa",
    amount = "Summa", validAmount = "Syötä kelvollinen summa", couldntFetch = "Kurssia ei voitu noutaa. Tarkista yhteys.",

    add = "Lisää", addToItinerary = "Lisää matkaohjelmaan", travel = "Matka", place = "Paikka",
    newPlace = "Uusi paikka", editPlace = "Muokkaa paikkaa", landmark = "Maamerkki (valinnainen)", savePlace = "Tallenna paikka", searchTrips = "Hae matkoja",
    newTrip = "Uusi matka", renameTrip = "Nimeä matka uudelleen", create = "Luo", save = "Tallenna",
    pin = "Kiinnitä", unpin = "Irrota", edit = "Muokkaa", archive = "Arkistoi", unarchive = "Palauta arkistosta",
    delete = "Poista", deleteTripQ = "Poista matka?", deletePlaceQ = "Poista paikka?", deleteLegQ = "Poista osuus?",
    cantBeUndone = "Tätä ei voi kumota.", archivedTrips = "Arkistoidut matkat", noArchivedTrips = "Ei arkistoituja matkoja",
    noDatesYet = "Ei vielä päivämääriä", tripName = "Matkan nimi", aLegBetween = "Osuus välillä", somewhereToVisit = "Jokin paikka vierailla",
)

private val SV = EN.copy(
    name = "Namn", surname = "Efternamn", dob = "Födelsedatum", street = "Gata", city = "Stad", postelCode = "Postnummer",
    appTagline = "Där varje resa börjar!", myTrips = "Mina resor", countries = "länder", legs = "etapper", done = "klar",
    addLeg = "Lägg till etapp", newLeg = "Ny etapp", from = "Från", to = "Till", transport = "Transport", date = "Datum", time = "Tid",
    bookingRef = "Bokningsref.", optional = "valfritt", saveLeg = "Spara etapp", tickets = "Biljetter", event = "Händelse", beforeYouGo = "Innan du åker",
    addItem = "Lägg till objekt", settings = "Inställningar", backupSync = "Säkerhetskopiering & synk", appearance = "Utseende",
    language = "Språk", currencyUnits = "Valuta & enheter", exportTrips = "Exportera resor", about = "Om", logOut = "Logga ut",
    welcomeBack = "Välkommen tillbaka", logInToTrips = "Logga in på dina resor", email = "E-post", password = "Lösenord",
    forgotPassword = "Glömt lösenord?", logIn = "Logga in", orDivider = "eller", continueGoogle = "Fortsätt med Google",
    continueApple = "Fortsätt med Apple", newHere = "Ny här?", createAccount = "Skapa konto", calendar = "Kalender",
    documents = "Dokument", nextUp = "nästa", legsTravelled = "etapper resta", syncWholeTrip = "Synkronisera hela resan",
    addToPhoneCalendar = "Lägg till i kalender", remindMe = "Påminn mig", chooseLanguage = "Välj ett språk",
    followPhone = "Följ telefonens språk", ok = "OK", cancel = "Avbryt", address = "Adress",
    editProfile = "Redigera profil", newPassword = "Nytt lösenord", saveChanges = "Spara ändringar",
    systemDefault = "Systemstandard", light = "Ljus", dark = "Mörk", matchYourPhone = "Matcha telefonen", currency = "Valuta", convert = "Konvertera", bookings = "Bokningar", packing = "Packning", money = "Pengar", gadget = "Gadget", other = "Annat", needToAdd = "Vad behöver du lägga till?", section = "Sektion",
    amount = "Belopp", validAmount = "Ange ett giltigt belopp", couldntFetch = "Kunde inte hämta växelkurs. Kontrollera anslutningen.",

    add = "Lägg till", addToItinerary = "Lägg till i resplan", travel = "Resa", place = "Plats",
    newPlace = "Ny plats", editPlace = "Redigera plats", landmark = "Landmärke (valfritt)", savePlace = "Spara plats", searchTrips = "Sök resor",
    newTrip = "Ny resa", renameTrip = "Byt namn på resa", create = "Skapa", save = "Spara",
    pin = "Fäst", unpin = "Lossa", edit = "Redigera", archive = "Arkivera", unarchive = "Återställ",
    delete = "Ta bort", deleteTripQ = "Ta bort resa?", deletePlaceQ = "Ta bort plats?", deleteLegQ = "Ta bort etapp?",
    cantBeUndone = "Detta kan inte ångras.", archivedTrips = "Arkiverade resor", noArchivedTrips = "Inga arkiverade resor",
    noDatesYet = "Inga datum än", tripName = "Resans namn", aLegBetween = "En etapp mellan", somewhereToVisit = "Någonstans att besöka", 
)

private val DA = EN.copy(
    name = "Navn", surname = "Efternavn", dob = "Fødselsdato", street = "Vej", city = "By", postelCode = "Postnummer",
    appTagline = "Hvor enhver rejse begynder!", myTrips = "Mine rejser", countries = "lande", legs = "etaper", done = "færdig",
    addLeg = "Tilføj etape", newLeg = "Ny etape", from = "Fra", to = "Til", transport = "Transport", date = "Dato", time = "Tid",
    bookingRef = "Bookingref.", optional = "valgfrit", saveLeg = "Gem etape", tickets = "Billetter", event = "Begivenhed", beforeYouGo = "Før du rejser",
    addItem = "Tilføj element", settings = "Indstillinger", backupSync = "Backup & synkronisering", appearance = "Udseende",
    language = "Sprog", currencyUnits = "Valuta & enheder", exportTrips = "Eksporter rejser", about = "Om", logOut = "Log ud",
    welcomeBack = "Velkommen tilbage", logInToTrips = "Log ind på dine rejser", email = "E-mail", password = "Kodeord",
    forgotPassword = "Glemt kodeord?", logIn = "Log ind", orDivider = "eller", continueGoogle = "Fortsæt med Google",
    continueApple = "Fortsæt med Apple", newHere = "Ny her?", createAccount = "Opret konto", calendar = "Kalender",
    documents = "Dokumenter", nextUp = "næste", legsTravelled = "etaper rejst", syncWholeTrip = "Synkroniser hele rejsen",
    addToPhoneCalendar = "Tilføj til kalender", remindMe = "Påmind mig", chooseLanguage = "Vælg et sprog",
    followPhone = "Følg telefonens sprog", ok = "OK", cancel = "Annuller", address = "Adresse",
    editProfile = "Rediger profil", newPassword = "Nyt kodeord", saveChanges = "Gem ændringer",
    systemDefault = "Systemstandard", light = "Lys", dark = "Mørk", matchYourPhone = "Følg telefonen", currency = "Valuta", convert = "Konverter", bookings = "Bookinger", packing = "Pakning", money = "Penge", gadget = "Gadget", other = "Andet", needToAdd = "Hvad har du brug for at tilføje?", section = "Sektion",
    amount = "Beløb", validAmount = "Indtast et gyldigt beløb", couldntFetch = "Kunne ikke hente kurs. Tjek din forbindelse.",

    add = "Tilføj", addToItinerary = "Tilføj til rejseplan", travel = "Rejse", place = "Sted",
    newPlace = "Nyt sted", editPlace = "Rediger sted", landmark = "Landemærke (valgfritt)", savePlace = "Gem sted", searchTrips = "Søg rejser",
    newTrip = "Ny rejse", renameTrip = "Omdøb rejse", create = "Opret", save = "Gem",
    pin = "Fastgør", unpin = "Frigør", edit = "Rediger", archive = "Arkiver", unarchive = "Gendan",
    delete = "Slet", deleteTripQ = "Slet rejse?", deletePlaceQ = "Slet sted?", deleteLegQ = "Slet etape?",
    cantBeUndone = "Dette kan ikke fortrydes.", archivedTrips = "Arkiverede rejser", noArchivedTrips = "Ingen arkiverede rejser",
    noDatesYet = "Ingen datoer endnu", tripName = "Navn på rejse", aLegBetween = "En etape mellem", somewhereToVisit = "Et sted at besøge",
)

private val ET = EN.copy(
    name = "Eesnimi", surname = "Perekonnanimi", dob = "Sünnikuupäev", street = "Tänav", city = "Linn", postelCode = "Postiindeks",
    appTagline = "Kus iga teekond algab!", myTrips = "Minu reisid", countries = "riiki", legs = "etappi", done = "tehtud",
    addLeg = "Lisa etapp", newLeg = "Uus etapp", from = "Kust", to = "Kuhu", transport = "Transport", date = "Kuupäev", time = "Aeg",
    bookingRef = "Broneeringu nr", optional = "valikuline", saveLeg = "Salvesta etapp", tickets = "Piletid", event = "Sündmus", beforeYouGo = "Enne minekut",
    addItem = "Lisa ese", settings = "Seaded", backupSync = "Varundus ja sünkroonimine", appearance = "Välimus",
    language = "Keel", currencyUnits = "Valuuta ja ühikud", exportTrips = "Ekspordi reisid", about = "Rakendusest", logOut = "Logi välja",
    welcomeBack = "Tere tulemast tagasi", logInToTrips = "Logi sisse oma reisidesse", email = "E-post", password = "Parool",
    forgotPassword = "Unustasid parooli?", logIn = "Logi sisse", orDivider = "või", continueGoogle = "Jätka Google'iga",
    continueApple = "Jätka Apple'iga", newHere = "Uus siin?", createAccount = "Loo konto", calendar = "Kalender",
    documents = "Dokumendid", nextUp = "järgmisena", legsTravelled = "läbitud etapid", syncWholeTrip = "Sünkrooni kogu reis",
    addToPhoneCalendar = "Lisa kalendrisse", remindMe = "Tuleta meelde", chooseLanguage = "Vali keel",
    followPhone = "Järgi telefoni keelt", ok = "OK", cancel = "Tühista", address = "Aadress",
    editProfile = "Muuda profiili", newPassword = "Uus parool", saveChanges = "Salvesta muudatused",
    systemDefault = "Süsteemi vaikeseade", light = "Hele", dark = "Tume", matchYourPhone = "Järgi telefoni", currency = "Valuuta", convert = "Teisenda", bookings = "Broneeringud", packing = "Pakkimine", money = "Raha", gadget = "Vidin", other = "Muu", needToAdd = "Mida teil on vaja lisada?", section = "Jaotis",
    amount = "Summa", validAmount = "Sisestage kehtiv summa", couldntFetch = "Kurssi ei õnnestunud pärida. Kontrollige ühendust.",

    add = "Lisa", addToItinerary = "Lisa teekonnale", travel = "Reis", place = "Koht",
    newPlace = "Uus koht", editPlace = "Muuda kohta", landmark = "Maamärk (valikuline)", savePlace = "Salvesta koht", searchTrips = "Otsi reise",
    newTrip = "Uus reis", renameTrip = "Nimeta reis ümber", create = "Loo", save = "Salvesta",
    pin = "Kinnita", unpin = "Eemalda kinnitus", edit = "Muuda", archive = "Arhiveeri", unarchive = "Taasta arhiivist",
    delete = "Kustuta", deleteTripQ = "Kustuta reis?", deletePlaceQ = "Kustuta koht?", deleteLegQ = "Kustuta etapp?",
    cantBeUndone = "Seda ei saa tagasi võtta.", archivedTrips = "Arhiveeritud reisid", noArchivedTrips = "Arhiveeritud reise pole",
    noDatesYet = "Kuupäevi pole veel", tripName = "Reisi nimi", aLegBetween = "Etapp vahel", somewhereToVisit = "Koht, mida külastada",
)

private val AR = EN.copy(
    name = "الاسم", surname = "النسبة", dob = "تاريخ الميلاد", street = "الشارع", city = "المدينة", postelCode = "الرمز البريدي",
    appTagline = "حيث تبدأ كل رحلة!", myTrips = "رحلاتي", countries = "دول", legs = "مراحل", done = "تم",
    addLeg = "إضافة مرحلة", newLeg = "مرحلة جديدة", from = "من", to = "إلى", transport = "وسيلة النقل", date = "التاريخ", time = "الوقت",
    bookingRef = "رقم الحجز", optional = "اختياري", saveLeg = "حفظ المرحلة", tickets = "التذاكر", event = "حدث", beforeYouGo = "قبل الذهاب",
    addItem = "إضافة عنصر", settings = "الإعدادات", backupSync = "النسخ الاحتياطي والمزامنة", appearance = "المظهر",
    language = "اللغة", currencyUnits = "العملة والوحدات", exportTrips = "تصدير الرحلات", about = "حول التطبيق", logOut = "تسجيل الخروج",
    welcomeBack = "مرحباً بعودتك", logInToTrips = "سجل الدخول لرحلاتك", email = "البريد الإلكتروني", password = "كلمة المرور",
    forgotPassword = "نسيت كلمة المرور؟", logIn = "تسجيل الدخول", orDivider = "أو", continueGoogle = "المتابعة باستخدام Google",
    continueApple = "المتابعة باستخدام Apple", newHere = "جديد هنا؟", createAccount = "إنشاء حساب", calendar = "التقويم",
    documents = "المستندات", nextUp = "القادم", legsTravelled = "مراحل مكتملة", syncWholeTrip = "مزامنة الرحلة بالكامل",
    addToPhoneCalendar = "إضافة لتقويم الهاتف", remindMe = "ذكرني", chooseLanguage = "اختر اللغة",
    followPhone = "اتباع لغة الهاتف", ok = "موافق", cancel = "إلغاء", address = "العنوان",
    editProfile = "تعديل الملف الشخصي", newPassword = "كلمة مرور جديدة", saveChanges = "حفظ التغييرات",
    systemDefault = "افتراضي النظام", light = "فاتح", dark = "داكن", matchYourPhone = "مطابقة الهاتف", currency = "العملة", convert = "تحويل", bookings = "حجوزات", packing = "تعبئة", money = "مال", gadget = "أداة", other = "أخرى", needToAdd = "ماذا تحتاج لإضافته؟", section = "قسم",
    amount = "المبلغ", validAmount = "أدخل مبلغاً صالحاً", couldntFetch = "تعذر جلب السعر. تحقق من اتصالك.",

    add = "إضافة", addToItinerary = "إضافة إلى المسار", travel = "سفر", place = "مكان",
    newPlace = "مكان جديد", editPlace = "تعديل المكان", landmark = "معلم (اختياري)", savePlace = "حفظ المكان", searchTrips = "بحث عن رحلات",
    newTrip = "رحلة جديدة", renameTrip = "إعادة تسمية الرحلة", create = "إنشاء", save = "حفظ",
    pin = "تثبيت", unpin = "إلغاء التثبيت", edit = "تعديل", archive = "أرشفة", unarchive = "إلغاء الأرشفة",
    delete = "حذف", deleteTripQ = "حذف الرحلة؟", deletePlaceQ = "حذف المكان؟", deleteLegQ = "حذف المرحلة؟",
    cantBeUndone = "لا يمكن التراجع عن هذا الإجراء.", archivedTrips = "الرحلات المؤرشفة", noArchivedTrips = "لا توجد رحلات مؤرشفة",
    noDatesYet = "لا توجد تواريخ بعد", tripName = "اسم الرحلة", aLegBetween = "مرحلة بين", somewhereToVisit = "مكان للزيارة",
)

private val TR = EN.copy(
    name = "Ad", surname = "Soyad", dob = "Doğum tarihi", street = "Sokak", city = "Şehir", postelCode = "Posta kodu",
    appTagline = "Her yolculuğun başladığı yer!", myTrips = "Gezilerim", countries = "ülke", legs = "etap", done = "tamamlandı",
    addLeg = "Etap ekle", newLeg = "Yeni etap", from = "Nereden", to = "Nereye", transport = "Ulaşım", date = "Tarih", time = "Saat",
    bookingRef = "Rezervasyon no", optional = "isteğe bağlı", saveLeg = "Etabı kaydet", tickets = "Biletler", event = "Etkinlik", beforeYouGo = "Gitmeden önce",
    addItem = "Öğe ekle", settings = "Ayarlar", backupSync = "Yedekle ve senkronize et", appearance = "Görünüm",
    language = "Dil", currencyUnits = "Para birimi ve birimler", exportTrips = "Gezileri dışa aktar", about = "Hakkında", logOut = "Çıkış yap",
    welcomeBack = "Tekrar hoş geldiniz", logInToTrips = "Gezilerinize giriş yapın", email = "E-posta", password = "Şifre",
    forgotPassword = "Şifremi unuttum?", logIn = "Giriş yap", orDivider = "veya", continueGoogle = "Google ile devam et",
    continueApple = "Apple ile devam et", newHere = "Burada yeni misiniz?", createAccount = "Hesap oluştur", calendar = "Takvim",
    documents = "Belgeler", nextUp = "sırada", legsTravelled = "gidilen etaplar", syncWholeTrip = "Tüm geziyi senkronize et",
    addToPhoneCalendar = "Telefona ekle", remindMe = "Hatırlat", chooseLanguage = "Dil seçin",
    followPhone = "Telefon dilini takip et", ok = "Tamam", cancel = "İptal", address = "Adres",
    editProfile = "Profili düzenle", newPassword = "Yeni şifre", saveChanges = "Değişiklikleri kaydet",
    systemDefault = "Sistem varsayılanı", light = "Açık", dark = "Koyu", matchYourPhone = "Telefonla eşleştir", currency = "Para birimi", convert = "Dönüştür", bookings = "Rezervasyonlar", packing = "Paketleme", money = "Para", gadget = "Gereç", other = "Diğer", needToAdd = "Ne eklemeniz gerekiyor?", section = "Bölüm",
    amount = "Tutar", validAmount = "Geçerli bir tutar girin", couldntFetch = "Kur alınamadı. Bağlantınızı kontrol edin.",

    add = "Ekle", addToItinerary = "Seyahat planına ekle", travel = "Seyahat", place = "Yer",
    newPlace = "Yeni yer", editPlace = "Yeri düzenle", landmark = "Önemli yer (isteğe bağlı)", savePlace = "Yeri kaydet", searchTrips = "Gezilerde ara",
    newTrip = "Yeni gezi", renameTrip = "Geziyi yeniden adlandır", create = "Oluştur", save = "Kaydet",
    pin = "Sabitle", unpin = "Sabitlemeyi kaldır", edit = "Düzenle", archive = "Arşivle", unarchive = "Arşivden çıkar",
    delete = "Sil", deleteTripQ = "Geziyi sil?", deletePlaceQ = "Yeri sil?", deleteLegQ = "Etabı sil?",
    cantBeUndone = "Bu işlem geri alınamaz.", archivedTrips = "Arşivlenen geziler", noArchivedTrips = "Arşivlenen gezi yok",
    noDatesYet = "Henüz tarih yok", tripName = "Gezi adı", aLegBetween = "Arasındaki etap", somewhereToVisit = "Ziyaret edilecek bir yer",
)

private val VI = EN.copy(
    name = "Tên", surname = "Họ", dob = "Ngày sinh", street = "Đường", city = "Thành phố", postelCode = "Mã bưu điện",
    appTagline = "Nơi mọi hành trình bắt đầu!", myTrips = "Chuyến đi của tôi", countries = "quốc gia", legs = "chặng", done = "hoàn thành",
    addLeg = "Thêm chặng", newLeg = "Chặng mới", from = "Từ", to = "Đến", transport = "Phương tiện", date = "Ngày", time = "Giờ",
    bookingRef = "Mã đặt chỗ", optional = "tùy chọn", saveLeg = "Lưu chặng", tickets = "Vé", event = "Sự kiện", beforeYouGo = "Trước khi đi",
    addItem = "Thêm mục", settings = "Cài đặt", backupSync = "Sao lưu & đồng bộ", appearance = "Giao diện",
    language = "Ngôn ngữ", currencyUnits = "Tiền tệ & đơn vị", exportTrips = "Xuất chuyến đi", about = "Giới thiệu", logOut = "Đăng xuất",
    welcomeBack = "Chào mừng trở lại", logInToTrips = "Đăng nhập vào chuyến đi", email = "Email", password = "Mật khẩu",
    forgotPassword = "Quên mật khẩu?", logIn = "Đăng nhập", orDivider = "hoặc", continueGoogle = "Tiếp tục với Google",
    continueApple = "Tiếp tục với Apple", newHere = "Mới ở đây?", createAccount = "Tạo tài khoản", calendar = "Lịch",
    documents = "Tài liệu", nextUp = "tiếp theo", legsTravelled = "chặng đã đi", syncWholeTrip = "Đồng bộ toàn bộ chuyến đi",
    addToPhoneCalendar = "Thêm vào lịch điện thoại", remindMe = "Nhắc tôi", chooseLanguage = "Chọn ngôn ngữ",
    followPhone = "Theo ngôn ngữ điện thoại", ok = "OK", cancel = "Hủy", address = "Địa chỉ",
    editProfile = "Chỉnh sửa hồ sơ", newPassword = "Mật khẩu mới", saveChanges = "Lưu thay đổi",
    systemDefault = "Mặc định hệ thống", light = "Sáng", dark = "Tối", matchYourPhone = "Theo điện thoại", currency = "Tiền tệ", convert = "Chuyển đổi", bookings = "Đặt chỗ", packing = "Đóng gói", money = "Tiền", gadget = "Tiện ích", other = "Khác", needToAdd = "Bạn cần thêm gì?", section = "Phần",
    amount = "Số tiền", validAmount = "Nhập số tiền hợp lệ", couldntFetch = "Không thể lấy tỷ giá. Kiểm tra kết nối của bạn.",

    add = "Thêm", addToItinerary = "Thêm vào hành trình", travel = "Du lịch", place = "Địa điểm",
    newPlace = "Địa điểm mới", editPlace = "Chỉnh sửa địa điểm", landmark = "Địa danh (tùy chọn)", savePlace = "Lưu địa điểm", searchTrips = "Tìm kiếm chuyến đi",
    newTrip = "Chuyến đi mới", renameTrip = "Đổi tên chuyến đi", create = "Tạo", save = "Lưu",
    pin = "Ghim", unpin = "Bỏ ghim", edit = "Chỉnh sửa", archive = "Lưu trữ", unarchive = "Bỏ lưu trữ",
    delete = "Xóa", deleteTripQ = "Xóa chuyến đi?", deletePlaceQ = "Xóa địa điểm?", deleteLegQ = "Xóa chặng?",
    cantBeUndone = "Hành động này không thể hoàn tác.", archivedTrips = "Chuyến đi đã lưu trữ", noArchivedTrips = "Không có chuyến đi lưu trữ",
    noDatesYet = "Chưa có ngày", tripName = "Tên chuyến đi", aLegBetween = "Một chặng giữa", somewhereToVisit = "Nơi nào đó để tham quan",
)

private val ID = EN.copy(
    name = "Nama", surname = "Nama belakang", dob = "Tanggal lahir", street = "Jalan", city = "Kota", postelCode = "Kode pos",
    appTagline = "Di mana setiap perjalanan dimulai!", myTrips = "Perjalananku", countries = "negara", legs = "tahap", done = "selesai",
    addLeg = "Tambah tahap", newLeg = "Tahap baru", from = "Dari", to = "Ke", transport = "Transportasi", date = "Tanggal", time = "Waktu",
    bookingRef = "Ref. pemesanan", optional = "opsional", saveLeg = "Simpan tahap", tickets = "Tiket", event = "Acara", beforeYouGo = "Sebelum pergi",
    addItem = "Tambah item", settings = "Pengaturan", backupSync = "Cadangan & sinkronisasi", appearance = "Tampilan",
    language = "Bahasa", currencyUnits = "Mata uang & unit", exportTrips = "Ekspor perjalanan", about = "Tentang", logOut = "Keluar",
    welcomeBack = "Selamat datang kembali", logInToTrips = "Masuk ke perjalananmu", email = "Email", password = "Kata sandi",
    forgotPassword = "Lupa kata sandi?", logIn = "Masuk", orDivider = "atau", continueGoogle = "Lanjutkan dengan Google",
    continueApple = "Lanjutkan with Apple", newHere = "Baru di sini?", createAccount = "Buat akun", calendar = "Kalender",
    documents = "Dokumen", nextUp = "selanjutnya", legsTravelled = "tahap dilalui", syncWholeTrip = "Sinkronkan seluruh perjalanan",
    addToPhoneCalendar = "Tambah ke kalender ponsel", remindMe = "Ingatkan saya", chooseLanguage = "Pilih bahasa",
    followPhone = "Ikuti bahasa ponsel", ok = "OK", cancel = "Batal", address = "Alamat",
    editProfile = "Edit profil", newPassword = "Kata sandi baru", saveChanges = "Simpan perubahan",
    systemDefault = "Default sistem", light = "Terang", dark = "Gelap", matchYourPhone = "Ikuti ponsel", currency = "Mata uang", convert = "Konversi", bookings = "Pemesanan", packing = "Pengepakan", money = "Uang", gadget = "Gawai", other = "Lainnya", needToAdd = "Apa yang perlu Anda tambahkan?", section = "Bagian",
    amount = "Jumlah", validAmount = "Masukkan jumlah yang valid", couldntFetch = "Tidak dapat mengambil kurs. Periksa koneksi Anda.",

    add = "Tambah", addToItinerary = "Tambah ke rencana perjalanan", travel = "Perjalanan", place = "Tempat",
    newPlace = "Tempat baru", editPlace = "Edit tempat", landmark = "Landmark (opsional)", savePlace = "Simpan tempat", searchTrips = "Cari perjalanan",
    newTrip = "Perjalanan baru", renameTrip = "Ganti nama perjalanan", create = "Buat", save = "Simpan",
    pin = "Sematkan", unpin = "Lepas sematan", edit = "Edit", archive = "Arsip", unarchive = "Buka arsip",
    delete = "Hapus", deleteTripQ = "Hapus perjalanan?", deletePlaceQ = "Hapus tempat?", deleteLegQ = "Hapus tahap?",
    cantBeUndone = "Tindakan ini tidak dapat dibatalkan.", archivedTrips = "Perjalanan diarsipkan", noArchivedTrips = "Tidak ada perjalanan diarsipkan",
    noDatesYet = "Belum ada tanggal", tripName = "Nama perjalanan", aLegBetween = "Tahap antara", somewhereToVisit = "Suatu tempat untuk dikunjungi",
)

private val TH = EN.copy(
    name = "ชื่อ", surname = "นามสกุล", dob = "วันเกิด", street = "ถนน", city = "เมือง", postelCode = "รหัสไปรษณีย์",
    appTagline = "ที่ซึ่งทุกการเดินทางเริ่มต้น!", myTrips = "การเดินทางของฉัน", countries = "ประเทศ", legs = "ช่วง", done = "เสร็จสิ้น",
    addLeg = "เพิ่มช่วง", newLeg = "ช่วงใหม่", from = "จาก", to = "ถึง", transport = "การเดินทาง", date = "วันที่", time = "เวลา",
    bookingRef = "เลขการจอง", optional = "เลือกได้", saveLeg = "บันทึกช่วง", tickets = "ตั๋ว", event = "กิจกรรม", beforeYouGo = "ก่อนเดินทาง",
    addItem = "เพิ่มรายการ", settings = "ตั้งค่า", backupSync = "สำรองและซิงค์", appearance = "รูปลักษณ์",
    language = "ภาษา", currencyUnits = "สกุลเงินและหน่วย", exportTrips = "ส่งออกการเดินทาง", about = "เกี่ยวกับ", logOut = "ออกจากระบบ",
    welcomeBack = "ยินดีต้อนรับกลับมา", logInToTrips = "เข้าสู่ระบบการเดินทาง", email = "อีเมล", password = "รหัสผ่าน",
    forgotPassword = "ลืมรหัสผ่าน?", logIn = "เข้าสู่ระบบ", orDivider = "หรือ", continueGoogle = "ไปต่อด้วย Google",
    continueApple = "ไปต่อด้วย Apple", newHere = "เพิ่งมาครั้งแรก?", createAccount = "สร้างบัญชี", calendar = "ปฏิทิน",
    documents = "เอกสาร", nextUp = "รายการถัดไป", legsTravelled = "ช่วงที่เดินทางแล้ว", syncWholeTrip = "ซิงค์การเดินทางทั้งหมด",
    addToPhoneCalendar = "เพิ่มในปฏิทินโทรศัพท์", remindMe = "เตือนฉัน", chooseLanguage = "เลือกภาษา",
    followPhone = "ตามภาษาโทรศัพท์", ok = "ตกลง", cancel = "ยกเลิก", address = "ที่อยู่",
    editProfile = "แก้ไขโปรไฟล์", newPassword = "รหัสผ่านใหม่", saveChanges = "บันทึกการเปลี่ยนแปลง",
    systemDefault = "ค่าเริ่มต้นระบบ", light = "สว่าง", dark = "มืด", matchYourPhone = "ตามโทรศัพท์", currency = "สกุลเงิน", convert = "แปลง", bookings = "การจอง", packing = "การจัดกระเป๋า", money = "เงิน", gadget = "แกดเจ็ต", other = "อื่นๆ", needToAdd = "คุณต้องเพิ่มอะไร?", section = "ส่วน",
    amount = "จำนวน", validAmount = "กรุณาใส่จำนวนเงินที่ถูกต้อง", couldntFetch = "ไม่สามารถดึงข้อมูลอัตราแลกเปลี่ยนได้ โปรดตรวจสอบการเชื่อมต่อของคุณ",

    add = "เพิ่ม", addToItinerary = "เพิ่มในแผนการเดินทาง", travel = "การเดินทาง", place = "สถานที่",
    newPlace = "สถานที่ใหม่", editPlace = "แก้ไขสถานที่", landmark = "สถานที่สำคัญ (เลือกได้)", savePlace = "บันทึกสถานที่", searchTrips = "ค้นหาการเดินทาง",
    newTrip = "การเดินทางใหม่", renameTrip = "เปลี่ยนชื่อการเดินทาง", create = "สร้าง", save = "บันทึก",
    pin = "ปักหมุด", unpin = "ถอนหมุด", edit = "แก้ไข", archive = "เก็บถาวร", unarchive = "ยกเลิกการเก็บถาวร",
    delete = "ลบ", deleteTripQ = "ลบการเดินทาง?", deletePlaceQ = "ลบสถานที่?", deleteLegQ = "ลบช่วง?",
    cantBeUndone = "ไม่สามารถย้อนกลับการดำเนินการนี้ได้", archivedTrips = "การเดินทางที่เก็บถาวร", noArchivedTrips = "ไม่มีการเดินทางที่เก็บถาวร",
    noDatesYet = "ยังไม่มีวันที่", tripName = "ชื่อการเดินทาง", aLegBetween = "ช่วงระหว่าง", somewhereToVisit = "สถานที่ที่น่าไปเยือน",
)

private val HE = EN.copy(
    name = "שם", surname = "שם משפחה", dob = "תאריך לידה", street = "רחוב", city = "עיר", postelCode = "מיקוד",
    appTagline = "המקום בו כל מסע מתחיל!", myTrips = "הטיולים שלי", countries = "מדינות", legs = "מקטעים", done = "בוצע",
    addLeg = "הוספת מקטע", newLeg = "מקטע חדש", from = "מ-", to = "עד", transport = "תחבורה", date = "תאריך", time = "שעה",
    bookingRef = "מספר הזמנה", optional = "אופציונלי", saveLeg = "שמירת מקטע", tickets = "כרטיסים", event = "אירוע", beforeYouGo = "לפני היציאה",
    addItem = "הוספת פריט", settings = "הגדרות", backupSync = "גיבוי וסנכרון", appearance = "מראה",
    language = "שפה", currencyUnits = "מטבע ויחידות", exportTrips = "ייצוא טיולים", about = "אודות", logOut = "התנתקות",
    welcomeBack = "ברוך שובך", logInToTrips = "התחבר לטיולים שלך", email = "דואר אלקטרוני", password = "סיסמה",
    forgotPassword = "שכחת סיסמה?", logIn = "התחברות", orDivider = "או", continueGoogle = "המשך עם Google",
    continueApple = "המשך עם Apple", newHere = "חדש כאן?", createAccount = "יצירת חשבון", calendar = "לוח שנה",
    documents = "מסמכים", nextUp = "הבא בתור", legsTravelled = "מקטעים שעברו", syncWholeTrip = "סנכרון הטיול המלא",
    addToPhoneCalendar = "הוספה ללוח השנה", remindMe = "הזכר לי", chooseLanguage = "בחר שפה",
    followPhone = "עקוב אחר שפת הטלפון", ok = "אישור", cancel = "ביטול", address = "כתובת",
    editProfile = "עריכת פרופיל", newPassword = "סיסמה חדשה", saveChanges = "שמירת שינויים",
    systemDefault = "ברירת מחדל של המערכת", light = "בהיר", dark = "כהה", matchYourPhone = "התאם לטלפון", currency = "מטבע", convert = "המרה", bookings = "הזמנות", packing = "אריזה", money = "כסף", gadget = "גאדג'ט", other = "אחר", needToAdd = "מה אתה צריך להוסיף?", section = "חלק",
    amount = "סכום", validAmount = "הזן סכום תקין", couldntFetch = "לא ניתן היה לקבל שער חליפין. בדוק את החיבור שלך.",

    add = "הוספה", addToItinerary = "הוספה למסלול", travel = "נסיעה", place = "מקום",
    newPlace = "מקום חדש", editPlace = "עריכת מקום", landmark = "נקודת ציון (אופציונלי)", savePlace = "שמירת מקום", searchTrips = "חיפוש טיולים",
    newTrip = "טיול חדש", renameTrip = "שינוי שם הטיול", create = "יצירה", save = "שמירה",
    pin = "נעיצה", unpin = "ביטול נעיצה", edit = "עריכה", archive = "ארכוב", unarchive = "ביטול ארכוב",
    delete = "מחיקה", deleteTripQ = "למחוק את הטיול?", deletePlaceQ = "למחוק את המקום?", deleteLegQ = "למחוק את המקטע?",
    cantBeUndone = "לא ניתן לבטל פעולה זו.", archivedTrips = "טיולים בארכיון", noArchivedTrips = "אין טיולים בארכיון",
    noDatesYet = "עדיין אין תאריכים", tripName = "שם הטיול", aLegBetween = "מקטע בין", somewhereToVisit = "מקום לבקר בו",
)

private val BN = EN.copy(
    name = "নাম", surname = "পদবী", dob = "জন্ম তারিখ", street = "রাস্তা", city = "শহর", postelCode = "পোস্টাল কোড",
    appTagline = "যেখানে প্রতিটি ভ্রমণ শুরু হয়!", myTrips = "আমার ভ্রমণ", countries = "দেশ", legs = "পর্যায়", done = "সম্পন্ন",
    addLeg = "পর্যায় যোগ করুন", newLeg = "নতুন পর্যায়", from = "থেকে", to = "পর্যন্ত", transport = "পরিবহন", date = "তারিখ", time = "সময়",
    bookingRef = "বুকিং নম্বর", optional = "ঐচ্ছিক", saveLeg = "পর্যায় সংরক্ষণ করুন", tickets = "টিকিট", event = "ইভেন্ট", beforeYouGo = "যাওয়ার আগে",
    addItem = "আইটেম যোগ করুন", settings = "সেটিংস", backupSync = "ব্যাকআপ ও সিঙ্ক", appearance = "চেহারা",
    language = "ভাষা", currencyUnits = "মুদ্রা ও একক", exportTrips = "ভ্রমণ রপ্তানি করুন", about = "সম্পর্কে", logOut = "লগ আউট",
    welcomeBack = "স্বাগতম", logInToTrips = "আপনার ভ্রমণে লগ ইন করুন", email = "ই-মেইল", password = "পাসওয়ার্ড",
    forgotPassword = "পাসওয়ার্ড ভুলে গেছেন?", logIn = "লগ ইন", orDivider = "অথবা", continueGoogle = "Google-এর সাথে এগিয়ে যান",
    continueApple = "Apple-এর সাথে এগিয়ে যান", newHere = "এখানে নতুন?", createAccount = "অ্যাকাউন্ট তৈরি করুন", calendar = "ক্যালেন্ডার",
    documents = "ডকুমেন্টস", nextUp = "পরবর্তী", legsTravelled = "ভ্রমণ করা পর্যায়", syncWholeTrip = "পুরো ভ্রমণ ক্যালেন্ডারে সিঙ্ক করুন",
    addToPhoneCalendar = "ফোন ক্যালেন্ডারে যোগ করুন", remindMe = "আমাকে মনে করিয়ে দিন", chooseLanguage = "ভাষা চয়ন করুন",
    followPhone = "ফোনের ভাষা অনুসরণ করুন", ok = "ঠিক আছে", cancel = "বাতিল", address = "ঠিকানা",
    editProfile = "প্রোফাইল সম্পাদনা", newPassword = "নতুন পাসওয়ার্ড", saveChanges = "পরিবর্তনগুলি সংরক্ষণ করুন",
    systemDefault = "সিস্টেম ডিফল্ট", light = "হালকা", dark = "গাঢ়", matchYourPhone = "ফোন অনুসরণ করুন", currency = "মুদ্রা", convert = "রূপান্তর করুন", bookings = "বুকিং", packing = "প্যাকিং", money = "টাকা", gadget = "গ্যাজেট", other = "অন্যান্য", needToAdd = "আপনার কী যোগ করা দরকার?", section = "বিভাগ",
    amount = "পরিমাণ", validAmount = "একটি সঠিক পরিমাণ লিখুন", couldntFetch = "রেট পাওয়া যায়নি। আপনার সংযোগ পরীক্ষা করুন।",

    add = "যোগ করুন", addToItinerary = "ভ্রমণসূচীতে যোগ করুন", travel = "ভ্রমণ", place = "স্থান",
    newPlace = "নতুন স্থান", editPlace = "স্থান সম্পাদনা করুন", landmark = "ল্যান্ডমার্ক (ঐচ্ছিক)", savePlace = "স্থান সংরক্ষণ করুন", searchTrips = "ভ্রমণ খুঁজুন",
    newTrip = "নতুন ভ্রমণ", renameTrip = "ভ্রমণের নাম পরিবর্তন করুন", create = "তৈরি করুন", save = "সংরক্ষণ করুন",
    pin = "পিন করুন", unpin = "আনপিন করুন", edit = "সম্পাদনা করুন", archive = "আর্কাইভ করুন", unarchive = "আর্কাইভ থেকে সরান",
    delete = "মুছে ফেলুন", deleteTripQ = "ভ্রমণটি কি মুছে ফেলবেন?", deletePlaceQ = "স্থানটি কি মুছে ফেলবেন?", deleteLegQ = "পর্যায়টি কি মুছে ফেলবেন?",
    cantBeUndone = "এই কাজটি ফিরিয়ে নেওয়া যাবে না।", archivedTrips = "আর্কাইভ করা ভ্রমণ", noArchivedTrips = "কোন আর্কাইভ করা ভ্রমণ নেই",
    noDatesYet = "এখনও কোন তারিখ নেই", tripName = "ভ্রমণের নাম", aLegBetween = "এর মধ্যে একটি পর্যায়", somewhereToVisit = "ভ্রমণের জন্য কোনো জায়গা",
)

private val TG = EN.copy(
    name = "Pangalan", surname = "Apelyido", dob = "Petsa ng kapanganakan", street = "Kalye", city = "Lungsod",
    postelCode = "Postal code", appTagline = "Kung saan nagsisimula ang bawat paglalakbay!", myTrips = "Aking mga biyahe",
    countries = "mga bansa", legs = "mga yugto", done = "tapos na", addLeg = "Magdagdag ng yugto", newLeg = "Bagong yugto",
    from = "Mula sa", to = "Hanggang sa", transport = "Transportasyon", date = "Petsa", time = "Oras",
    bookingRef = "Booking ref", optional = "opsyonal", saveLeg = "I-save ang yugto",
    tickets = "Mga Tiket", event = "Kaganapan", beforeYouGo = "Bago ka umalis", addItem = "Magdagdag ng item",
    settings = "Mga Setting", backupSync = "Backup at sync", appearance = "Itsura",
    language = "Wika", currencyUnits = "Pera at mga yunit", exportTrips = "I-export ang mga biyahe",
    about = "Tungkol sa", logOut = "Mag-log out", welcomeBack = "Maligayang pagbabalik",
    logInToTrips = "Mag-log in sa iyong mga biyahe", email = "E-mail", password = "Password",
    forgotPassword = "Nakalimutan ang password?", logIn = "Mag-log in", orDivider = "o",
    continueGoogle = "Magpatuloy gamit ang Google", continueApple = "Magpatuloy gamit ang Apple",
    newHere = "Bago rito?", createAccount = "Gumawa ng account", calendar = "Kalendaryo",
    documents = "Mga Dokumento", nextUp = "susunod", legsTravelled = "mga yugtong nalakbay na",
    syncWholeTrip = "I-sync ang buong biyahe sa kalendaryo", addToPhoneCalendar = "Idagdag sa kalendaryo ng telepono",
    remindMe = "Paalalahanan ako", chooseLanguage = "Pumili ng wika", followPhone = "Sundin ang wika ng telepono",
    ok = "OK", cancel = "Kanselahin", address = "Address", editProfile = "I-edit ang profile",
    newPassword = "Bagong password", saveChanges = "I-save ang mga pagbabago",
    systemDefault = "Default ng sistema", light = "Maliwanag", dark = "Madilim", matchYourPhone = "Itugma sa telepono", currency = "Pera", convert = "I-convert", bookings = "Mga Booking", packing = "Pag-iimpake", money = "Pera", gadget = "Gadget", other = "Iba pa", needToAdd = "Ano ang kailangan mong idagdag?", section = "Seksyon",
    amount = "Halaga", validAmount = "Maglagay ng wastong halaga", couldntFetch = "Hindi makuha ang rate. Suriin ang iyong koneksyon.",

    add = "Idagdag", addToItinerary = "Idagdag sa itinerary", travel = "Paglalakbay", place = "Lugar",
    newPlace = "Bagong lugar", editPlace = "I-edit ang lugar", landmark = "Landmark (opsyonal)", savePlace = "I-save ang lugar", searchTrips = "Maghanap ng mga biyahe",
    newTrip = "Bagong biyahe", renameTrip = "Palitan ang pangalan ng biyahe", create = "Gumawa", save = "I-save",
    pin = "I-pin", unpin = "I-unpin", edit = "I-edit", archive = "I-archive", unarchive = "I-unarchive",
    delete = "I-delete", deleteTripQ = "I-delete ang biyahe?", deletePlaceQ = "I-delete ang lugar?", deleteLegQ = "I-delete ang yugto?",
    cantBeUndone = "Hindi na ito mababawi.", archivedTrips = "Mga naka-archive na biyahe", noArchivedTrips = "Walang mga naka-archive na biyahe",
    noDatesYet = "Wala pang mga petsa", tripName = "Pangalan ng biyahe", aLegBetween = "Isang yugto sa pagitan ng", somewhereToVisit = "Isang lugar na mapupuntahan",
)

/**
 * Resolve the active Strings table for a language.
 * Portuguese and Dutch fall back to English for now — add full
 * tables the same way FR/ES/DE/IT/PL are defined above. SYSTEM resolves upstream.
 */
fun systemLanguage(): Language = when (deviceLanguageCode().lowercase()) {
    "fr" -> Language.FRENCH
    "es" -> Language.SPANISH
    "de" -> Language.GERMAN
    "it" -> Language.ITALIAN
    "pt" -> Language.PORTUGUESE
    "nl" -> Language.DUTCH
    "pl" -> Language.POLISH
    "zh" -> Language.CHINESE
    "ja" -> Language.JAPANESE
    "ko" -> Language.KOREAN
    "ru" -> Language.RUSSIAN
    "ta" -> Language.TAMIL
    "hi" -> Language.HINDI
    "uk" -> Language.UKRAINIAN
    "hu" -> Language.HUNGARIAN
    "ro" -> Language.ROMANIAN
    "el" -> Language.GREEK
    "ka" -> Language.GEORGIAN
    "bg" -> Language.BULGARIAN
    "cs" -> Language.CZECH
    "lv" -> Language.LATVIAN
    "lt" -> Language.LITHUANIAN
    "sk" -> Language.SLOVAK
    "sl" -> Language.SLOVENIAN
    "sr" -> Language.SERBIAN
    "sq" -> Language.ALBANIAN
    "mk" -> Language.MACEDONIAN
    "no", "nb", "nn" -> Language.NORWEGIAN
    "fi" -> Language.FINNISH
    "sv" -> Language.SWEDISH
    "da" -> Language.DANISH
    "et" -> Language.ESTONIAN
    "ar" -> Language.ARABIC
    "tr" -> Language.TURKISH
    "vi" -> Language.VIETNAMESE
    "id", "in" -> Language.INDONESIAN
    "th" -> Language.THAI
    "he", "iw" -> Language.HEBREW
    "bn" -> Language.BENGALI
    "tl", "fil" -> Language.TAGALOG
    else -> Language.ENGLISH
}

/**
 * Resolves the active Strings table for a language.
 */
fun stringsFor(language: Language): Strings = when (language) {
    Language.SYSTEM -> stringsFor(systemLanguage())
    Language.ENGLISH -> EN
    Language.FRENCH -> FR
    Language.SPANISH -> ES
    Language.GERMAN -> DE
    Language.ITALIAN -> IT
    Language.PORTUGUESE -> PT
    Language.DUTCH -> NL
    Language.POLISH -> PL
    Language.CHINESE -> ZH
    Language.JAPANESE -> JP
    Language.KOREAN -> KR
    Language.RUSSIAN -> RU
    Language.TAMIL -> TAM
    Language.HINDI -> HI
    Language.UKRAINIAN -> UKR
    Language.HUNGARIAN -> HUN
    Language.ROMANIAN -> RO
    Language.GREEK -> GR
    Language.GEORGIAN -> KA
    Language.BULGARIAN -> BG
    Language.CZECH -> CZ
    Language.LATVIAN -> LV
    Language.LITHUANIAN -> LT
    Language.SLOVAK -> SK
    Language.SLOVENIAN -> SL
    Language.SERBIAN -> SR
    Language.ALBANIAN -> SQ
    Language.MACEDONIAN -> MK
    Language.MOLDAVIAN -> MO
    Language.NORWEGIAN -> NO
    Language.FINNISH -> FI
    Language.SWEDISH -> SV
    Language.DANISH -> DA
    Language.ESTONIAN -> ET
    Language.ARABIC -> AR
    Language.TURKISH -> TR
    Language.VIETNAMESE -> VI
    Language.INDONESIAN -> ID
    Language.THAI -> TH
    Language.HEBREW -> HE
    Language.BENGALI -> BN
    Language.TAGALOG -> TG
}
