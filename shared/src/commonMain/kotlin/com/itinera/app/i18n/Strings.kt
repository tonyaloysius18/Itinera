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

    MOLDAVIAN("Moldovan", "Română (Moldova)"),

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
    TAJIK("Tajik", "Тоҷикӣ"),
    SINHALA("Sinhala", "සිංහල"),

}

/**
 * Every piece of UI text the app owns. User-entered content (trip titles city
 * names) is NOT in here on purpose — only the app's own chrome gets translated.
 *
 * In production this would be Compose Multiplatform string resources
 * (composeResources/values-fr/strings.xml etc.). A plain data class is used here
 * so language switching works at runtime with zero extra build setup.
 */
class Strings {
    var name: String = ""
    var surname: String = ""
    var dob: String = ""
    var street: String = ""
    var city: String = ""
    var postelCode: String = ""
    var appTagline: String = ""
    var myTrips: String = ""
    var countries: String = ""
    var leg: String = ""
    var legs: String = ""
    var done: String = ""
    var addLeg: String = ""
    var newLeg: String = ""
    var from: String = ""
    var to: String = ""
    var transport: String = ""
    var date: String = ""
    var time: String = ""
    var bookingRef: String = ""
    var optional: String = ""
    var saveLeg: String = ""
    var tickets: String = ""
    var event: String = ""
    var beforeYouGo: String = ""
    var addItem: String = ""
    var settings: String = ""
    var backupSync: String = ""
    var appearance: String = ""
    var language: String = ""
    var currencyUnits: String = ""
    var exportTrips: String = ""
    var about: String = ""
    var logOut: String = ""
    var welcomeBack: String = ""
    var logInToTrips: String = ""
    var email: String = ""
    var password: String = ""
    var forgotPassword: String = ""
    var logIn: String = ""
    var orDivider: String = ""
    var continueGoogle: String = ""
    var continueApple: String = ""
    var newHere: String = ""
    var createAccount: String = ""
    var calendar: String = ""
    var documents: String = ""
    var nextUp: String = ""
    var legsTravelled: String = ""
    var syncWholeTrip: String = ""
    var addToPhoneCalendar: String = ""
    var remindMe: String = ""
    var chooseLanguage: String = ""
    var followPhone: String = ""
    var ok: String = ""
    var cancel: String = ""
    var address: String = ""
    var editProfile: String = ""
    var newPassword: String = ""
    var saveChanges: String = ""
    var systemDefault: String = ""
    var light: String = ""
    var dark: String = ""
    var alwaysLight: String = ""
    var alwaysDark: String = ""
    var matchYourPhone: String = ""
    var currency: String = ""
    var convert: String = ""
    var bookings: String = ""
    var packing: String = ""
    var money: String = ""
    var gadget: String = ""
    var other: String = ""
    var needToAdd: String = ""
    var section: String = ""
    var amount: String = ""
    var validAmount: String = ""
    var couldntFetch: String = ""
    var fillAllFields: String = ""
    var loginFailed: String = ""
    var passwordTooShort: String = ""
    var signupFailed: String = ""
    var deleteAccount: String = ""
    var deleteAccountConfirm: String = ""
    var deleteButton: String = ""
    var showPassword: String = ""
    var hidePassword: String = ""
    var back: String = ""
    var recentLoginRequired: String = ""
    var accountCreated: String = ""
    var accountDeleted: String = ""
    var changesSaved: String = ""
    var invalidCredentials: String = ""
    var uppercase: String = ""
    var lowercase: String = ""
    var number: String = ""
    var specialCharacter: String = ""
    var minimumCharacters: String = ""

    var choosePhoto: String = ""
    var uploadFromGallery: String = ""
    var takePhoto: String = ""
    var removePhoto: String = ""
    var cropAndScale: String = ""
    var usePhoto: String = ""
    var resetPasswordPrompt: String = ""
    var resetEmailSent: String = ""
    var resetEmailFailed: String = ""
    var mobile: String = ""
    var today: String = ""
    var days: String = ""
    var noTripsYet: String = ""
    var noTripsSubtitle: String = ""
    var noResults: String = ""
    var noResultsSubtitle: String = ""
    var startTime: String = ""
    var endTime: String = ""
    var operatorGeneric: String = ""
    var busOperator: String = ""
    var trainOperator: String = ""
    var flightOperator: String = ""
    var ferryOperator: String = ""
    var country: String = ""
    var search: String = ""


    var add: String = ""
    var addToItinerary: String = ""
    var travel: String = ""
    var place: String = ""
    var newPlace: String = ""
    var editPlace: String = ""
    var landmark: String = ""
    var savePlace: String = ""
    var searchTrips: String = ""
    var newTrip: String = ""
    var renameTrip: String = ""
    var create: String = ""
    var save: String = ""
    var pin: String = ""
    var unpin: String = ""
    var edit: String = ""
    var archive: String = ""
    var unarchive: String = ""
    var delete: String = ""
    var deleteTripQ: String = ""
    var deletePlaceQ: String = ""
    var deleteLegQ: String = ""
    var cantBeUndone: String = ""
    var archivedTrips: String = ""
    var noArchivedTrips: String = ""
    var noDatesYet: String = ""
    var tripName: String = ""
    var aLegBetween: String = ""
    var somewhereToVisit: String = ""

    var addDocument: String = ""
    var title: String = ""
    var attachTo: String = ""
    var wholeTrip: String = ""
    var open: String = ""
    var noDocuments: String = ""
    var noDocumentsSubtitle: String = ""
    var deleteDocumentQ: String = ""
    var fileNotUploaded: String = ""
    var category: String = ""
    var accommodation: String = ""
    var attraction: String = ""
    var uploading: String = ""
    var uploadFailed: String = ""
    var day: String = ""
    var editLeg: String = ""
    var close: String = ""
    var swap: String = ""
    var capture: String = ""
    var profilePhoto: String = ""
    var changePhoto: String = ""
    var selected: String = ""
    var itineraLogo: String = ""
    var share: String = ""
    var shareFailed: String = ""
    var travellers: String = ""
    var addTraveller: String = ""
    var editTraveller: String = ""
    var deleteTravellerQ: String = ""
    var you: String = ""
    var phone: String = ""
    var split: String = ""
    var noTripsToSplit: String = ""
    var noTripsToSplitSubtitle: String = ""
    var expenseSingular: String = ""
    var expensePlural: String = ""
    var noExpenses: String = ""
    var noExpensesSubtitle: String = ""
    var totalSpent: String = ""
    var settleUp: String = ""
    var allSettled: String = ""
    var expensesLabel: String = ""
    var paidBy: String = ""
    var paidFor: String = ""
    var paid: String = ""
    var addExpense: String = ""
    var editExpense: String = ""
    var tripCurrency: String = ""
    var deleteExpenseQ: String = ""
    var description: String = ""
    var over: String = ""
    var paidByLabel: String = ""
    var splitBetween: String = ""
    var splitEqually: String = ""
    var splitCustom: String = ""
    var splitMatches: String = ""
    var remaining: String = ""
    var whoOwesWhom: String = ""
    var whoOwesWhomHint: String = ""
    var splitLabel: String = ""
    var owes: String = ""
    var settleUpHint: String = ""
    var selectTripsToExport: String = ""
    var shareAsText: String = ""
    var selectAll: String = ""
    var deselectAll: String = ""
    var noTripsToExport: String = ""
    var noTripsToExportSubtitle: String = ""
    var shareAsPdf: String = ""
    var account: String = ""
    var signedInAs: String = ""
    var addAnotherAccount: String = ""
    var notifications: String = ""
    var backupStatus: String = ""
    var help: String = ""
    var comingSoon: String = ""
    var notificationsSoonSubtitle: String = ""
    var backupSoonSubtitle: String = ""
    var helpSoonSubtitle: String = ""
    var aboutSoonSubtitle: String = ""
    var version: String = ""
    var developer: String = ""
    var contact: String = ""
    var acknowledgements: String = ""
    var aboutTagline: String = ""
    var backupAllSynced: String = ""
    var lastSynced: String = ""
    var never: String = ""
    var justNow: String = ""
    var minuteAgo: String = ""
    var minutesAgo: String = ""
    var hourAgo: String = ""
    var hoursAgo: String = ""
    var dayAgo: String = ""
    var daysAgo: String = ""
    var storedInCloud: String = ""
    var tripsLabel: String = ""
    var expensesCountLabel: String = ""
    var documentsLabel: String = ""
    var syncNow: String = ""
    var syncComplete: String = ""
    var syncFailed: String = ""
    var backupHint: String = ""

    var noLegsYet: String =""
    var noLegsSubtitle: String =""
    var noChecklistItems: String =""
    var noChecklistSubtitle: String = ""

    var inviteToTrip: String = ""
    var inviteFailed: String = ""
    var inviteCodeHint: String = ""
    var inviteShareMessage: String = ""
    var copy: String = ""

    var createTripOption: String = ""
    var joinTripOption: String = ""
    var enterInviteCode: String = ""
    var invalidCode: String = ""
    var joined: String = ""
    var join: String = ""

    var members: String = ""
    var roleOwner: String = ""
    var roleEditor: String = ""
    var roleViewer: String = ""
    var removeMember: String = ""
    var removeMemberQ: String = ""
    var removeMemberDesc: String = ""
    var remove: String = ""

    var leaveTrip: String = ""
    var leaveTripQ: String = ""
    var leaveTripDesc: String = ""

    var removeAccount: String = ""
    var removeAccountConfirm: String = ""

    var allowNotificationsPrompt: String = ""
    var customReminder: String = ""
    var remindBeforeDeparture: String = ""
    var hours: String = ""
    var minutes: String = ""
    var set: String = ""
    var pickAtLeast5: String = ""
    var hourSingular: String = ""
    var hoursPlural: String = ""
    var minuteSingular: String = ""
    var minutesPlural: String = ""
    var before: String = ""
    var custom: String = ""

    var markPaid: String = ""
    var unsettleTrip: String = ""
    var settleUpTrip: String = ""
    var payments: String = ""
    var recordedRepayments: String = ""
    var undoPayment: String = ""
    var theOwner: String = ""
    var markedAsSettled: String = ""
    var youOweBanner: String = ""
    var youAreOwedBanner: String = ""
    var allSquareBanner: String = ""

    // World Clock
    var worldClock: String = ""
    var localLabel: String = ""
    var addTimeZone: String = ""
    var searchAnyCity: String = ""
    var addCityForTime: String = ""

    // Emergency / SOS
    var emergency: String = ""
    var police: String = ""
    var ambulance: String = ""
    var fire: String = ""
    var selectCountry: String = ""
    var searchCountry: String = ""
    var change: String = ""
    var emergencyDialerNote: String = ""
    var noEmergencyData: String = ""

    // Weather
    var weather: String = ""
    var noCitiesYet: String = ""
    var tapPlusAddCity: String = ""
    var addCity: String = ""
    var searchCity: String = ""
    var getWeather: String = ""
    var nextDays: String = ""
    var couldntLoadWeather: String = ""
    var loadingLabel: String = ""
    var noCityMatches: String = ""
    var couldntFindPlace: String = ""

    // Translate
    var translate: String = ""
    var translateEnterText: String = ""
    var translating: String = ""
    var translationLabel: String = ""
    var translateError: String = ""
    var history: String = ""
    var clear: String = ""
    var languageLabel: String = ""
    var searchLanguage: String = ""
    var favorite: String = ""

    // Compass
    var compass: String = ""
    var compassCalibrate: String = ""

    // Leg ↔ document link
    var attachToLeg: String = ""
    var attachToNone: String = ""
    var viewTicket: String = ""

    fun copyApply(block: Strings.() -> Unit): Strings {

        val c = Strings()

        c.name = this.name
        c.surname = this.surname
        c.dob = this.dob
        c.street = this.street
        c.city = this.city
        c.postelCode = this.postelCode
        c.appTagline = this.appTagline
        c.myTrips = this.myTrips
        c.countries = this.countries
        c.leg = this.leg
        c.legs = this.legs
        c.done = this.done
        c.addLeg = this.addLeg
        c.newLeg = this.newLeg
        c.from = this.from
        c.to = this.to
        c.transport = this.transport
        c.date = this.date
        c.time = this.time
        c.bookingRef = this.bookingRef
        c.optional = this.optional
        c.saveLeg = this.saveLeg
        c.tickets = this.tickets
        c.event = this.event
        c.beforeYouGo = this.beforeYouGo
        c.addItem = this.addItem
        c.settings = this.settings
        c.backupSync = this.backupSync
        c.appearance = this.appearance
        c.language = this.language
        c.currencyUnits = this.currencyUnits
        c.exportTrips = this.exportTrips
        c.about = this.about
        c.logOut = this.logOut
        c.welcomeBack = this.welcomeBack
        c.logInToTrips = this.logInToTrips
        c.email = this.email
        c.password = this.password
        c.forgotPassword = this.forgotPassword
        c.logIn = this.logIn
        c.orDivider = this.orDivider
        c.continueGoogle = this.continueGoogle
        c.continueApple = this.continueApple
        c.newHere = this.newHere
        c.createAccount = this.createAccount
        c.calendar = this.calendar
        c.documents = this.documents
        c.nextUp = this.nextUp
        c.legsTravelled = this.legsTravelled
        c.syncWholeTrip = this.syncWholeTrip
        c.addToPhoneCalendar = this.addToPhoneCalendar
        c.remindMe = this.remindMe
        c.chooseLanguage = this.chooseLanguage
        c.followPhone = this.followPhone
        c.ok = this.ok
        c.cancel = this.cancel
        c.address = this.address
        c.editProfile = this.editProfile
        c.newPassword = this.newPassword
        c.saveChanges = this.saveChanges
        c.systemDefault = this.systemDefault
        c.light = this.light
        c.dark = this.dark
        c.matchYourPhone = this.matchYourPhone
        c.currency = this.currency
        c.convert = this.convert
        c.bookings = this.bookings
        c.packing = this.packing
        c.money = this.money
        c.gadget = this.gadget
        c.other = this.other
        c.needToAdd = this.needToAdd
        c.section = this.section
        c.amount = this.amount
        c.validAmount = this.validAmount
        c.couldntFetch = this.couldntFetch
        c.fillAllFields = this.fillAllFields
        c.loginFailed = this.loginFailed
        c.passwordTooShort = this.passwordTooShort
        c.signupFailed = this.signupFailed
        c.deleteAccount = this.deleteAccount
        c.deleteAccountConfirm = this.deleteAccountConfirm
        c.deleteButton = this.deleteButton
        c.showPassword = this.showPassword
        c.hidePassword = this.hidePassword
        c.back = this.back
        c.recentLoginRequired = this.recentLoginRequired
        c.accountCreated = this.accountCreated
        c.accountDeleted = this.accountDeleted
        c.changesSaved = this.changesSaved
        c.invalidCredentials = this.invalidCredentials
        c.uppercase = this.uppercase
        c.lowercase = this.lowercase
        c.number = this.number
        c.specialCharacter = this.specialCharacter
        c.minimumCharacters = this.minimumCharacters
        c.add = this.add
        c.addToItinerary = this.addToItinerary
        c.travel = this.travel
        c.place = this.place
        c.newPlace = this.newPlace
        c.editPlace = this.editPlace
        c.landmark = this.landmark
        c.savePlace = this.savePlace
        c.searchTrips = this.searchTrips
        c.newTrip = this.newTrip
        c.renameTrip = this.renameTrip
        c.create = this.create
        c.save = this.save
        c.pin = this.pin
        c.unpin = this.unpin
        c.edit = this.edit
        c.archive = this.archive
        c.unarchive = this.unarchive
        c.delete = this.delete
        c.deleteTripQ = this.deleteTripQ
        c.deletePlaceQ = this.deletePlaceQ
        c.deleteLegQ = this.deleteLegQ
        c.cantBeUndone = this.cantBeUndone
        c.archivedTrips = this.archivedTrips
        c.noArchivedTrips = this.noArchivedTrips
        c.noDatesYet = this.noDatesYet
        c.tripName = this.tripName
        c.aLegBetween = this.aLegBetween
        c.somewhereToVisit = this.somewhereToVisit
        c.choosePhoto = this.choosePhoto
        c.uploadFromGallery = this.uploadFromGallery
        c.takePhoto = this.takePhoto
        c.removePhoto = this.removePhoto
        c.cropAndScale = this.cropAndScale
        c.usePhoto = this.usePhoto
        c.resetPasswordPrompt = this.resetPasswordPrompt
        c.resetEmailSent = this.resetEmailSent
        c.resetEmailFailed = this.resetEmailFailed
        c.mobile = this.mobile
        c.today = this.today
        c.days = this.days
        c.noTripsYet = this.noTripsYet
        c.noTripsSubtitle = this.noTripsSubtitle
        c.noResults = this.noResults
        c.noResultsSubtitle = this.noResultsSubtitle
        c.startTime = this.startTime
        c.endTime = this.endTime
        c.operatorGeneric = this.operatorGeneric
        c.busOperator = this.busOperator
        c.trainOperator = this.trainOperator
        c.flightOperator = this.flightOperator
        c.ferryOperator = this.ferryOperator
        c.country = this.country
        c.search = this.search
        c.addDocument = this.addDocument
        c.title = this.title
        c.attachTo = this.attachTo
        c.wholeTrip = this.wholeTrip
        c.open = this.open
        c.noDocuments = this.noDocuments
        c.noDocumentsSubtitle = this.noDocumentsSubtitle
        c.deleteDocumentQ = this.deleteDocumentQ
        c.fileNotUploaded = this.fileNotUploaded
        c.category = this.category
        c.accommodation = this.accommodation
        c.attraction = this.attraction
        c.uploading = this.uploading
        c.uploadFailed = this.uploadFailed
        c.day = this.day
        c.editLeg = this.editLeg
        c.close = this.close
        c.swap = this.swap
        c.capture = this.capture
        c.profilePhoto = this.profilePhoto
        c.changePhoto = this.changePhoto
        c.selected = this.selected
        c.itineraLogo = this.itineraLogo
        c.share = this.share
        c.shareFailed = this.shareFailed
        c.travellers = this.travellers
        c.addTraveller = this.addTraveller
        c.editTraveller = this.editTraveller
        c.deleteTravellerQ = this.deleteTravellerQ
        c.you = this.you
        c.phone = this.phone
        c.split = this.split
        c.noTripsToSplit = this.noTripsToSplit
        c.noTripsToSplitSubtitle = this.noTripsToSplitSubtitle
        c.expenseSingular = this.expenseSingular
        c.expensePlural = this.expensePlural
        c.noExpenses = this.noExpenses
        c.noExpensesSubtitle = this.noExpensesSubtitle
        c.totalSpent = this.totalSpent
        c.settleUp = this.settleUp
        c.allSettled = this.allSettled
        c.expensesLabel = this.expensesLabel
        c.paidBy = this.paidBy
        c.addExpense = this.addExpense
        c.editExpense = this.editExpense
        c.tripCurrency = this.tripCurrency
        c.deleteExpenseQ = this.deleteExpenseQ
        c.description = this.description
        c.paidByLabel = this.paidByLabel
        c.splitBetween = this.splitBetween
        c.splitEqually = this.splitEqually
        c.splitCustom = this.splitCustom
        c.splitMatches = this.splitMatches
        c.remaining = this.remaining
        c.over = this.over
        c.paidFor = this.paidFor
        c.paid = this.paid
        c.settleUpHint = this.settleUpHint
        c.whoOwesWhom = this.whoOwesWhom
        c.whoOwesWhomHint = this.whoOwesWhomHint
        c.splitLabel = this.splitLabel
        c.owes = this.owes
        c.selectTripsToExport = this.selectTripsToExport
        c.shareAsText = this.shareAsText
        c.selectAll = this.selectAll
        c.deselectAll = this.deselectAll
        c.noTripsToExport = this.noTripsToExport
        c.noTripsToExportSubtitle = this.noTripsToExportSubtitle
        c.shareAsPdf = this.shareAsPdf
        c.account = this.account
        c.signedInAs = this.signedInAs
        c.addAnotherAccount = this.addAnotherAccount
        c.notifications = this.notifications
        c.backupStatus = this.backupStatus
        c.help = this.help
        c.comingSoon = this.comingSoon
        c.notificationsSoonSubtitle = this.notificationsSoonSubtitle
        c.backupSoonSubtitle = this.backupSoonSubtitle
        c.helpSoonSubtitle = this.helpSoonSubtitle
        c.aboutSoonSubtitle = this.aboutSoonSubtitle
        c.version = this.version
        c.developer = this.developer
        c.contact = this.contact
        c.acknowledgements = this.acknowledgements
        c.aboutTagline = this.aboutTagline
        c.backupAllSynced = this.backupAllSynced
        c.lastSynced = this.lastSynced
        c.never = this.never
        c.justNow = this.justNow
        c.minuteAgo = this.minuteAgo
        c.minutesAgo = this.minutesAgo
        c.hourAgo = this.hourAgo
        c.hoursAgo = this.hoursAgo
        c.dayAgo = this.dayAgo
        c.daysAgo = this.daysAgo
        c.storedInCloud = this.storedInCloud
        c.tripsLabel = this.tripsLabel
        c.expensesCountLabel = this.expensesCountLabel
        c.documentsLabel = this.documentsLabel
        c.syncNow = this.syncNow
        c.syncComplete = this.syncComplete
        c.syncFailed = this.syncFailed
        c.backupHint = this.backupHint
        c.noLegsYet = this.noLegsYet
        c.noLegsSubtitle = this.noLegsSubtitle
        c.noChecklistItems = this.noChecklistItems
        c.noChecklistSubtitle = this.noChecklistSubtitle
        c.inviteToTrip = this.inviteToTrip
        c.inviteFailed = this.inviteFailed
        c.inviteCodeHint = this.inviteCodeHint
        c.inviteShareMessage = this.inviteShareMessage
        c.copy = this.copy
        c.createTripOption = this.createTripOption
        c. joinTripOption = this.joinTripOption
        c.enterInviteCode = this.enterInviteCode
        c.invalidCode = this.invalidCode
        c.joined = this.joined
        c.join = this.join

        c.members = this.members
        c.roleOwner = this.roleOwner
        c.roleEditor = this.roleEditor
        c.roleViewer = this.roleViewer
        c.removeMember = this.removeMember
        c.removeMemberQ = this.removeMemberQ
        c.removeMemberDesc = this.removeMemberDesc
        c.remove = this.remove
        c.leaveTrip = this.leaveTrip
        c.leaveTripQ = this.leaveTripQ
        c.leaveTripDesc = this.leaveTripDesc

        c.removeAccount = this.removeAccount
        c.removeAccountConfirm = this.removeAccountConfirm

        c.allowNotificationsPrompt = this.allowNotificationsPrompt
        c.customReminder = this.customReminder
        c.remindBeforeDeparture = this.remindBeforeDeparture
        c.hours = this.hours
        c.minutes = this.minutes
        c.set = this.set
        c.pickAtLeast5 = this.pickAtLeast5
        c.hourSingular = this.hourSingular
        c.hoursPlural = this.hoursPlural
        c.minuteSingular = this.minuteSingular
        c.minutesPlural = this.minutesPlural
        c.before = this.before
        c.custom = this.custom

        c.markPaid = this.markPaid
        c.unsettleTrip = this.unsettleTrip
        c.settleUpTrip = this.settleUpTrip
        c.payments = this.payments
        c.recordedRepayments = this.recordedRepayments
        c.undoPayment = this.undoPayment
        c.theOwner = this.theOwner
        c.markedAsSettled = this.markedAsSettled
        c.youOweBanner = this.youOweBanner
        c.youAreOwedBanner = this.youAreOwedBanner
        c.allSquareBanner = this.allSquareBanner

        c.worldClock = this.worldClock
        c.localLabel = this.localLabel
        c.addTimeZone = this.addTimeZone
        c.searchAnyCity = this.searchAnyCity
        c.addCityForTime = this.addCityForTime

        c.emergency = this.emergency
        c.police = this.police
        c.ambulance = this.ambulance
        c.fire = this.fire
        c.selectCountry = this.selectCountry
        c.searchCountry = this.searchCountry
        c.change = this.change
        c.emergencyDialerNote = this.emergencyDialerNote
        c.noEmergencyData = this.noEmergencyData

        c.weather = this.weather
        c.noCitiesYet = this.noCitiesYet
        c.tapPlusAddCity = this.tapPlusAddCity
        c.addCity = this.addCity
        c.searchCity = this.searchCity
        c.getWeather = this.getWeather
        c.nextDays = this.nextDays
        c.couldntLoadWeather = this.couldntLoadWeather
        c.loadingLabel = this.loadingLabel
        c.noCityMatches = this.noCityMatches
        c.couldntFindPlace = this.couldntFindPlace

        c.translate = this.translate
        c.translateEnterText = this.translateEnterText
        c.translating = this.translating
        c.translationLabel = this.translationLabel
        c.translateError = this.translateError
        c.history = this.history
        c.clear = this.clear
        c.languageLabel = this.languageLabel
        c.searchLanguage = this.searchLanguage
        c.favorite = this.favorite
        c.compass = this.compass
        c.compassCalibrate = this.compassCalibrate
        c.attachToLeg = this.attachToLeg
        c.attachToNone = this.attachToNone
        c.viewTicket = this.viewTicket

        c.block()
        return c
    }
}



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
    "tg" -> Language.TAJIK
    "tl", "fil" -> Language.TAGALOG
    "si" -> Language.SINHALA
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
    Language.TAGALOG -> EN // No Tagalog strings yet
    Language.TAJIK -> TG
    Language.SINHALA -> SI
}