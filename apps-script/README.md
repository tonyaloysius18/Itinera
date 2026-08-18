# Itinera feedback endpoint

This Google Apps Script web app validates the sender's Firebase ID token and
delivers feedback to `ynotlabs.dev@gmail.com` without a paid Cloud project.

## One-time deployment

1. Create a standalone project at <https://script.google.com> while signed in as
   `ynotlabs.dev@gmail.com`.
2. Replace `Code.gs` and `appsscript.json` with the files in this folder.
3. In **Project settings → Script properties**, add `FIREBASE_WEB_API_KEY` with
   the API key from `androidApp/google-services.json`.
4. Select **Deploy → New deployment → Web app**. Execute as **Me** and allow
   access to **Anyone**.
5. Put the resulting `/exec` URL in `FeedbackEndpoint` inside
   `shared/src/commonMain/kotlin/com/itinera/app/data/FeedbackService.kt`.

Deploy a new version after changing `Code.gs`; the existing web-app URL can stay
the same. Do not log or store feedback messages, image bytes, or Firebase tokens.
