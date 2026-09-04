# Itinera — Carousel Feature-Image Prompts (for ChatGPT / GPT-4o image gen)

A connected **carousel/storyboard set** in the *blue illustrated* Itinera style — bold headline per
panel, a phone mockup at a varied 3D angle, playful travel props, one continuous deep-blue scene so
the panels read as a single story (like the reference gallery you shared, but in our brand look).

---

## 1. Exact dimensions (make these your final canvas)

| Use | Final size (px) | Orientation | Notes |
|-----|-----------------|-------------|-------|
| **iOS App Store — 6.9″** (iPhone 16/17 Pro Max) | **1320 × 2868** | portrait | Required set |
| iOS App Store — 6.5″ (optional) | 1242 × 2688 | portrait | Reuse same art |
| **Android Play — phone screenshots** | **1080 × 1920** | portrait | 2–8 images (9:16) |
| **Android Play — Feature Graphic** | **1024 × 500** | landscape | Required · JPG/PNG · **no transparency** |

**ChatGPT’s native output sizes** are `1024×1536` (portrait), `1536×1024` (landscape), `1024×1024`.
So you can’t render 1320×2868 directly. Do this:

- **Screenshots:** ask ChatGPT for **portrait 1024×1536**. Compose with the phone centered and
  generous background all around (headroom top, floor bottom) so you can **extend/pad** to the taller
  store ratio. Then in any editor place it on a 1320×2868 (iOS) or 1080×1920 (Android) canvas filled
  with the same blue gradient — the extra background blends invisibly.
- **Feature graphic:** ask for **landscape 1536×1024**, then crop to **1024×500**.

> Tip: at the end of each prompt add `Output size: 1024x1536 portrait.` (or `1536x1024 landscape`).

---

## 2. MASTER STYLE BLOCK — paste at the top of EVERY panel prompt
> Premium mobile-app store panel, portrait. Deep royal-blue background with a soft radial glow —
> darker navy at the top fading to a brighter teal-cyan swoosh in the lower area — subtle dotted paper
> texture. Playful semi-3D flat-illustration style, warm and inviting, clean vector shapes with soft
> long shadows, cohesive palette of royal blue #1B6FD3, deep navy #0E2A5A, teal #2FC7C0, mint #3FBF8F,
> warm cream #F2E4C9. A thin WHITE dashed airplane flight-path line curves across the panel with a
> small white paper airplane. A single realistic smartphone mockup with a titanium frame is the hero,
> its screen left BLANK/empty (a solid dark placeholder) so a screenshot can be dropped in later.
> Bold rounded sans-serif HEADLINE with the key phrase in mint. Crisp, premium, high detail, no
> extra UI text, no watermark.

**Carousel rules (keep consistent across all 8 so they line up):**
- Same background gradient + dashed flight path on every panel, so a viewer swiping feels one scene.
- **Alternate the headline position** panel-to-panel: odd panels headline **top**, even panels **bottom**.
- **Vary the phone angle** panel-to-panel: front-straight → tilted-left 3D → tilted-right 3D → raised-straight → … so the set has rhythm (like the reference).
- Keep the phone roughly centered with clear space around it for the extend-to-store-ratio step.

---

## 3. The 8 panels — copy each block into ChatGPT (one at a time)

> For consistency, after the first image, attach the **previous generated panel** as a style
> reference and say “match this exact background, palette, and phone style.”

**Panel 1 — Trips**  *(headline TOP · phone front-straight)*
> [MASTER STYLE BLOCK] Headline at top: “All your trips, **one place**”. Props around the phone: a
> red-and-cream hot-air balloon upper-left, snow-capped mountains, two vintage travel postcards (a
> tropical sunset beach and the Roman Colosseum) leaning lower-left, Big Ben and a red Japanese pagoda
> lower-right, a classic globe on a brass stand and a brass compass bottom-right. Phone centered,
> facing forward. Output size: 1024x1536 portrait.

**Panel 2 — Plan day by day**  *(headline BOTTOM · phone tilted-left 3D)*
> [MASTER STYLE BLOCK] Headline at bottom: “Plan it, **day by day**”. Props: a folded paper map with a
> dotted route and glowing location pins, a stack of day-numbered calendar cards, small mountains and
> a winding road, tiny train and airplane icons. Phone tilted at a 3D angle to the left, floating.
> Output size: 1024x1536 portrait.

**Panel 3 — Itinerary timeline**  *(headline TOP · phone tilted-right 3D, lower)*
> [MASTER STYLE BLOCK] Headline at top: “Your itinerary, **hour by hour**”. Props: a large brass
> pocket-watch/clock, a vertical timeline ribbon with dots, a small train, a departures board, little
> suitcases, a soft sunrise behind hills. Phone tilted to the right in 3D, set a bit lower. Output
> size: 1024x1536 portrait.

**Panel 4 — Live currency**  *(headline BOTTOM · phone raised-straight)*
> [MASTER STYLE BLOCK] Headline at bottom: “Live currency, **real rates**”. Props: floating gold coins
> and banknotes, €/$/£ symbols, a rising line chart, a circular exchange-arrows motif, a small globe.
> Trustworthy fintech-meets-travel feel. Phone facing forward, raised slightly. Output size:
> 1024x1536 portrait.

**Panel 5 — Checklist**  *(headline TOP · phone tilted-left 3D)*
> [MASTER STYLE BLOCK] Headline at top: “Never forget **a thing**”. Props: an open suitcase with neatly
> packed items, a clipboard checklist with green ticks, a passport, a boarding pass, sunglasses, a
> camera, a rolled umbrella. Phone tilted left in 3D. Output size: 1024x1536 portrait.

**Panel 6 — Split expenses**  *(headline BOTTOM · phone front-straight)*
> [MASTER STYLE BLOCK] Headline at bottom: “Split costs, **stay square**”. Props: paper receipts, gold
> coins, a calculator, a colorful pie-chart split into slices, two friendly avatar bubbles linked by a
> line, a wallet. Phone facing forward. Output size: 1024x1536 portrait.

**Panel 7 — Travel together**  *(headline TOP · phone tilted-right 3D)*
> [MASTER STYLE BLOCK] Headline at top: “**Travel** together”. Props: a friendly group of 3–4 diverse
> illustrated travelers with backpacks waving, avatar circles linked by dotted lines, a shared map,
> small landmark silhouettes behind them. Phone tilted right in 3D. Output size: 1024x1536 portrait.

**Panel 8 — Documents**  *(headline BOTTOM · phone raised-straight)*
> [MASTER STYLE BLOCK] Headline at bottom: “Every ticket, **one tap away**”. Props: a fan of boarding
> passes and train tickets, a passport, a hotel key card, a QR code, an open document folder, all
> floating with soft shadows. Phone facing forward. Output size: 1024x1536 portrait.

---

## 4. Android Feature Graphic (1024 × 500, REQUIRED)
> Premium landscape app-store banner, 1536x1024 (crop to 1024x500 after). Deep royal-blue gradient
> (#0E2A5A → #1B6FD3) with a teal swoosh, subtle dotted texture, a thin white dashed airplane
> flight-path arcing across. LEFT third: calm space for the app name “Itinera” + a small logo mark.
> RIGHT two-thirds: the illustrated travel scene — hot-air balloon, snow mountains, Big Ben, red
> pagoda, globe on a brass stand, brass compass — with a titanium phone mockup (blank screen) peeking
> in from the right edge at a slight tilt. Bold, premium, cohesive palette #1B6FD3 / #2FC7C0 /
> #3FBF8F / #F2E4C9, no baked-in body text. Output size: 1536x1024 landscape.

---

## 5. Dropping the real screenshots in (two ways)
The prompts leave the phone screen **blank** on purpose — keep the real UI crisp.
- **In ChatGPT:** upload the illustrated panel **and** the matching Itinera screenshot, and ask:
  “Place this screenshot onto the blank phone screen, matching the phone’s angle and perspective.”
- **In an editor (Figma/Canva/Photoshop):** paste the screenshot into the phone screen, match the
  tilt with a little perspective/skew, add a soft screen glare. Then extend the background and export
  at the exact store size from the table in §1.

Match screenshots to panels: 1 Trips · 2 Trip-detail · 3 Calendar · 4 Currency · 5 Checklist ·
6 Expenses · 7 Travellers · 8 Documents. (Your anonymized raw screens are in
`docs/appstore-screenshots/raw/`.)

## 6. Keeping the 8 consistent in ChatGPT
- Generate **Panel 1 first**, get it right, then feed it back as a reference image for Panels 2–8
  (“same background, palette, lighting, phone and flight-path style as this”).
- Reuse the **exact palette hexes** every time.
- Keep the **dashed flight path** and **one balloon or landmark** recurring so the set feels linked.
- Generate all 8 in one sitting to avoid style drift.
