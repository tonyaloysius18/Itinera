# Nera translations: native-speaker review

The Nera text (the chat, buttons, errors and the free-trial messages) is translated into 42 languages. These
translations were written by an AI and **have not been reviewed by native speakers**. Please have a fluent speaker
check the languages they know, especially the lower-resource ones (Tajik, Sinhala, Georgian, Macedonian,
Albanian, Estonian, Moldovan).

**File:** [`nera-translation-review.csv`](nera-translation-review.csv). It opens in Excel, Numbers or Google Sheets
(UTF-8). One row per string per language, 27 strings each.

## For the reviewer

For each row, read the English source and the current translation, then:

- Put **Y** in *Reviewer: OK?* if it sounds natural and correct for an app, or
- put **N** and write the corrected text in *corrected text / notes*.

Guidelines:
- Keep `%s` exactly as written. The app replaces it with a number (for example the days left).
- Keep the name **Nera** in Latin letters.
- Nera is a friendly, helpful assistant: warm and concise, not formal or stiff. She addresses the traveller directly.
- Short labels (`Approve`, `Make changes`, `Send`, `Free`) should be short, like buttons.
- `neraSuggest1..3` are example prompts the user taps to send, so they should read like something a person would
  type ("5 days in London").
- Flag any string that is too long for a button or chip.

## Getting corrections back into the app

Send the finished CSV back. The strings live in
`shared/src/commonMain/kotlin/com/itinera/app/i18n/StringsGroup*.kt` (keys starting with `nera` or `planWithNera`),
and corrections can be applied in one pass.
