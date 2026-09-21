#!/usr/bin/env node
// Manage the friends & family list (emails that get Nera free).
//   node scripts/friends.mjs add mum@example.com "Mum"
//   node scripts/friends.mjs remove mum@example.com
//   node scripts/friends.mjs list
// Runs against the live database; add --local to use the local test database.
import { execFileSync } from "node:child_process";

const args = process.argv.slice(2);
const local = args.includes("--local");
const [cmd, rawEmail, ...noteParts] = args.filter((a) => a !== "--local");
const EMAIL = /^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$/;

function run(sql, json = false) {
  return execFileSync("npx", ["wrangler", "d1", "execute", "nera", local ? "--local" : "--remote", ...(json ? ["--json"] : []), "--command", sql],
    { cwd: new URL("..", import.meta.url).pathname, stdio: ["ignore", "pipe", "inherit"] }).toString();
}

if (cmd === "list") {
  const rows = JSON.parse(run("SELECT email, note, added_at FROM nera_friends ORDER BY added_at", true))[0].results;
  if (!rows.length) console.log("(no friends yet)");
  for (const r of rows) console.log(`${r.email}\t${r.note}\t${new Date(r.added_at).toISOString().slice(0, 10)}`);
} else if ((cmd === "add" || cmd === "remove") && rawEmail) {
  const email = rawEmail.trim().toLowerCase();
  if (!EMAIL.test(email)) { console.error(`"${rawEmail}" doesn't look like an email address.`); process.exit(1); }
  if (cmd === "add") {
    const note = noteParts.join(" ").replace(/[^\p{L}\p{N} ._-]/gu, "").slice(0, 60);
    run(`INSERT OR REPLACE INTO nera_friends (email, note, added_at) VALUES ('${email}', '${note}', ${Date.now()})`);
    console.log(`Added ${email}. They need to sign in with a VERIFIED account using this email (Google sign-in works).`);
  } else {
    run(`DELETE FROM nera_friends WHERE email = '${email}'`);
    console.log(`Removed ${email}.`);
  }
} else {
  console.error("Usage: friends.mjs add <email> [note] | remove <email> | list   (add --local for the test database)");
  process.exit(1);
}
