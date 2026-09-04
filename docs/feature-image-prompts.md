# Itinera — Feature Image Prompts (reference-style)

Prompts to generate rich, illustrated App Store / Play Store feature images in the style of the
approved reference (deep-blue scene, playful travel props, bold headline, centered phone mockup).

> **Workflow:** generate the **illustrated background scene only** (leave the center vertical band
> empty for the phone, and the top ~25% clear for the headline). Then composite your real app
> screenshot into the phone and add the headline text in your editor (Figma/Canva/the Python
> compositor in this repo). Generating the scene separately keeps the app UI crisp and text
> editable. If your generator supports it, attach the reference image as a style reference.

---

## MASTER STYLE BLOCK (paste into every prompt)
> Premium mobile app store feature graphic, portrait orientation. Deep royal-blue background with a
> soft radial glow, darker navy at the top fading to a brighter teal-cyan swoosh in the lower-right,
> subtle dotted paper texture. Playful semi-3D flat-illustration style, warm and inviting, clean
> vector shapes with soft long shadows, cohesive blue + teal + warm-cream palette. A thin white
> dashed airplane flight-path line loops across the upper area with a small white paper airplane.
> Leave the CENTER empty (reserved for a phone mockup) and the TOP 25% clear (reserved for a
> headline). High detail, crisp, premium, no text, no watermark, 9:19.5 aspect ratio.

**Palette:** royal blue `#1B6FD3` · deep navy `#0E2A5A` · teal `#2FC7C0` · mint `#3FBF8F` · warm cream `#F2E4C9`

---

## A. iOS App Store — 8 scenes (portrait, `--ar 9:19.5`, target 1320×2868)

Each = MASTER STYLE BLOCK + the accent props below. Headline (added later) in brackets.

**1 — Trips home** ["All your trips, one place"]
> …surrounded by travel icons: a red-and-cream hot air balloon upper-left, snow-capped mountains,
> two vintage travel postcards (a tropical sunset beach and the Roman Colosseum) leaning lower-left,
> Big Ben clock tower and a red Japanese pagoda lower-right, a classic globe on a brass stand and a
> brass compass at the bottom-right. (This is the reference scene.)

**2 — Day-by-day plan** ["Plan it, day by day"]
> …travel props: a folded paper map with a dotted route and glowing location pins, a stack of
> day-numbered calendar cards, small mountains and a winding road, a tiny train and airplane icon.

**3 — Itinerary timeline** ["Your itinerary, hour by hour"]
> …travel props: a large brass pocket watch / clock, a vertical timeline ribbon with dots, a train,
> a departures board, small suitcases, a sunrise behind hills.

**4 — Live currency** ["Live currency, real rates"]
> …travel props: floating gold coins and banknotes, €/$/£ currency symbols, a rising line chart, a
> circular exchange arrows motif, a small globe — trustworthy fintech-meets-travel feel.

**5 — Packing checklist** ["Never forget a thing"]
> …travel props: an open suitcase with neatly packed items, a clipboard checklist with green ticks,
> a passport, a boarding pass, sunglasses, a camera, a toiletry bottle, a rolled umbrella.

**6 — Split expenses** ["Split costs, stay square"]
> …travel props: paper receipts, gold coins, a calculator, a pie-chart split into colored slices,
> two friends’ avatar bubbles connected by a line, a wallet.

**7 — Travel together** ["Travel together"]
> …travel props: a friendly group of 3–4 diverse illustrated travelers with backpacks waving,
> avatar circles linked by dotted lines, a shared map, small landmark silhouettes behind them.

**8 — Documents wallet** ["Every ticket, one tap away"]
> …travel props: a fan of boarding passes and train tickets, a passport, a hotel key card, a QR
> code, a document folder, all floating with soft shadows.

---

## B. Android — Play Store phone screenshots (portrait, `--ar 9:16`, target 1080×1920)
Same 8 scenes as above; only the aspect ratio changes. Use `--ar 9:16` (Play accepts up to 3840px
tall; 1080×1920 or 1080×2160 both fine). Keep the phone mockup an Android-framed device if you want
platform accuracy (or keep it frameless).

## C. Google Play — Feature Graphic (landscape, 1024×500, REQUIRED)
> MASTER STYLE BLOCK but LANDSCAPE 1024×500: the Itinera wordmark + logo on the LEFT third, the
> illustrated travel scene (hot air balloon, mountains, Big Ben, pagoda, globe, compass, dashed
> flight path) filling the right two-thirds, phone mockup peeking in from the right edge. Leave the
> left third calm for the app name. Aspect ratio 1024:500, no text baked in.

## D. Optional — App Store hero (first slot, portrait)
> MASTER STYLE BLOCK, extra dramatic: a large dawn sky, the dashed flight path forming a big arc, a
> single hero landmark montage (Big Ben + pagoda + Colosseum) along the bottom, vast clear top for a
> big headline, phone mockup centered.

---

## Generation notes
- **Midjourney:** append `--ar 9:19.5 --style raw --v 6`; drop the reference image in as `--sref` /
  image prompt for consistent style across all 8.
- **DALL·E 3 / Gemini / Ideogram:** paste MASTER BLOCK + scene; explicitly say “leave the center and
  top empty,” and “no text.”
- Generate all 8 in **one session** with the same seed/style ref so the set looks unified.
- Then composite: real screenshot into the phone + headline text (white line + mint line, bold
  rounded sans like the reference). The repo’s `frame.py` can be adapted, or use Figma/Canva.
- Keep the **navy→teal palette** identical across all 8 so the carousel reads as one story.

## Headlines (white first line / mint second line)
1 All your trips, / one place · 2 Plan it, / day by day · 3 Your itinerary, / hour by hour ·
4 Live currency, / real rates · 5 Never forget / a thing · 6 Split costs, / stay square ·
7 Travel / together · 8 Every ticket, / one tap away
