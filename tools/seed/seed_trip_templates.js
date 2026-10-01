// One-off script to upload tools/seed/trip_templates.json into the `tripTemplates`
// Firestore collection. Not run automatically — see instructions below.
//
// Usage:
//   1. Download a service-account key for the Itinera Firebase project
//      (Firebase console -> Project settings -> Service accounts -> Generate new private key).
//   2. GOOGLE_APPLICATION_CREDENTIALS=/path/to/key.json node tools/seed/seed_trip_templates.js
//
// Uses the firebase-admin dependency already vendored under functions/node_modules,
// so no separate npm install is needed if functions/ has been installed.

const path = require("path");
const fs = require("fs");
const { createRequire } = require("module");

// firebase-admin (with its package.json "exports" map) only resolves correctly
// through Node's normal algorithm, so anchor a require() at functions/ rather
// than reaching into its node_modules by literal path.
const requireFromFunctions = createRequire(path.join(__dirname, "..", "..", "functions", "package.json"));
const admin = requireFromFunctions("firebase-admin");
const { getFirestore } = requireFromFunctions("firebase-admin/firestore");

const keyPath = process.env.GOOGLE_APPLICATION_CREDENTIALS;
const serviceAccount = JSON.parse(fs.readFileSync(keyPath, "utf8"));
admin.initializeApp({ credential: admin.cert(serviceAccount) });
const db = getFirestore();

async function main() {
  const templates = JSON.parse(
    fs.readFileSync(path.join(__dirname, "trip_templates.json"), "utf8"),
  );

  const batch = db.batch();
  for (const template of templates) {
    const ref = db.collection("tripTemplates").doc(template.id);
    batch.set(ref, template);
  }
  await batch.commit();
  console.log(`Seeded ${templates.length} trip templates.`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
