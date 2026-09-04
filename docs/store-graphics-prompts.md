# Itinera — Store Graphics: AI Image Generation Prompts

Prompts for generating the **background/feature graphics** that sit behind the framed
app screenshots in the App Store gallery (and the Google Play feature graphic).

## Brand system (keep consistent across ALL images)
- **Primary:** deep navy blue `#1E2A66`
- **Accent:** mint / emerald green `#3FBF8F` (the ribbon in the logo)
- **Support:** sky blue `#4A90D9`, soft off-white `#F4F6F8`
- **Mood:** aspirational, calm, premium, wanderlust — like the login screen (open road toward a snow-capped mountain, blue sky)
- **Style:** clean, modern, lots of negative space at top for a caption headline, subtle depth, no clutter, no text baked in (you'll add captions in your framing tool)

---

## A. Per-screenshot background panels (portrait 1320×2868, ratio ~0.46:1)
Each generated image is the backdrop; the phone mockup + a short caption go on top afterward.

### 1. Trips / Home — "Every trip in one place"
> A serene, minimal travel-themed vertical background, deep navy blue to sky blue gradient from top to bottom, a thin flowing mint-green ribbon arcing gently across the upper third like a flight path, soft abstract cloud shapes, generous empty space at the top for a headline, premium and calm, no text, subtle grain, 9:19.5 aspect ratio.

### 2. Trip Planning (day-by-day legs) — "Plan day by day"
> Vertical premium background, layered navy blue paper-cut mountains at the bottom, a subtle dotted route line winding upward with small mint-green location pins, clean sky-blue gradient sky above with room for a caption, flat modern illustration style, calming, no text, 9:19.5.

### 3. Live Currency Conversion — "Convert with live rates"
> Vertical minimal fintech-meets-travel background, deep navy base with floating translucent glass cards suggesting currency, soft mint-green upward arrows and subtle coin/ring motifs, elegant and trustworthy, lots of top negative space, no text, 9:19.5.

### 4. Pre-departure Checklist — "Never forget a thing"
> Vertical calm background, soft off-white to sky-blue gradient, faint oversized checkmark watermark in mint green, a few floating minimalist luggage/passport/ticket line-icons in navy, airy and organized feeling, top space for headline, no text, 9:19.5.

### 5. Documents & Tickets — "Tickets always ready"
> Vertical premium background, deep navy gradient, abstract boarding-pass and ticket shapes fanned softly with a mint-green accent stripe, subtle depth and shadow, secure and organized mood, top area clear for a caption, no text, 9:19.5.

### 6. Themes & 40+ Languages — "Yours, in your language"
> Vertical background split subtly between a light and a dark half to suggest light/dark themes, navy and mint accents bridging the two, small globe motif with soft orbiting dots for languages, modern and elegant, room for headline at top, no text, 9:19.5.

---

## B. Google Play Feature Graphic (landscape 1024×500) — for your Android listing
> A wide cinematic banner, open road stretching toward a snow-capped mountain under a bright blue sky (like a road-trip hero shot), deep navy-to-sky-blue color grade, a slim mint-green ribbon sweeping across like a flight path, clean and premium, empty space on the left third for the app name and logo, photographic realism with a subtle stylized finish, no text, 1024x500.

## C. Optional App Store "hero" first-panel background (portrait)
> A striking vertical hero background, dawn sky gradient from deep navy at top to warm sky blue, a lone airplane contrail curving as a mint-green ribbon, distant minimal mountain silhouette at the very bottom, vast calm negative space for a large headline, aspirational and premium, no text, 9:19.5.

---

## How to use
1. Generate with Midjourney, DALL·E 3, Ideogram, or Gemini. For Midjourney add `--ar 9:19.5` (portrait panels) or `--ar 1024:500`.
2. Keep the **navy + mint** palette consistent so the 6 panels read as one set.
3. In your framing tool (Figma, Canva, or the HTML compositor in this repo), place the
   device mockup + a 2–4 word caption on each panel. Captions suggested above in quotes.
4. Do NOT bake text into the AI image — add captions as editable text on top.

## Caption set (short, benefit-first)
1. Every trip in one place
2. Plan it day by day
3. Live currency, real rates
4. Never forget a thing
5. Tickets always ready
6. Your language, your theme
