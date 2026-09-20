# Nera mascot: image prompts for ChatGPT

Nera is the AI trip-planning companion inside Itinera. These prompts generate the mascot artwork that gets
implemented in the app: an animated button at the top-right of the My Trips screen, plus small avatars in the
chat.

## How to use this file

1. Open ChatGPT (image generation) in a **new chat**.
2. **Attach the reference image** (the "Nera by Itinera" brand board with the white robot and the teal scarf) to
   the first message, and paste **Prompt 1**.
3. Keep using the **same chat** for Prompts 2 and 3 so the character stays identical. Say "Use the same character
   as before" if it drifts.
4. If a result is off, reply with a correction ("keep the antenna smaller", "eyes must stay glowing white") instead
   of starting over.
5. Download every result as **PNG at the highest resolution** offered, and send me the file paths.

## Character bible (the same in every prompt)

- **Who:** Nera, a curious, helpful, adventurous AI travel companion.
- **Body:** rounded white glossy capsule body, small rounded arms, no visible legs needed.
- **Head:** big rounded white head with a **black glossy visor face**.
- **Eyes and mouth:** glowing soft white/cyan **curved arcs** (happy "^ ^" eyes) with a small smile. No pupils.
- **Antenna:** one short antenna with a **dark teal ball** on top.
- **Accessories:** a **teal scarf** with a small white **compass-cross badge**, and a small **teal backpack**.
- **Style:** soft 3D vinyl-toy render, smooth glossy-matte materials, clean and friendly. Not scary, not realistic.
- **Palette (only these):** dark teal `#1F5F57`, white, light grey, and the soft glow on the eyes.

## Background rule (important for cutting the character out)

The body is white, so a white background can't be removed cleanly. Every prompt asks for a **transparent PNG**, and
if that isn't possible, a **flat solid magenta background (#FF00FF)**, which the character doesn't contain. No
gradients, floor, shadow or text behind the character.

---

## Prompt 1: main character sheet (needed)

Attach the reference image, then paste:

```text
Use the attached image as the exact character reference. Create a character sheet of the SAME mascot, "Nera": a cute, friendly AI travel-companion robot with a rounded white glossy capsule body, a big rounded white head with a black glossy visor face, glowing soft white-cyan curved smiling eyes (no pupils) and a small smile, one short antenna with a dark teal ball tip, a teal scarf with a small white compass-cross badge, and a small teal backpack. Soft 3D vinyl-toy render, smooth glossy-matte materials, clean soft lighting, palette limited to dark teal (#1F5F57), white and light grey.

Show FOUR poses in a 2x2 grid. It must be the identical character, identical scale, identical camera angle (front view, very slightly 3/4), each pose centered with generous empty margin around it:
1. IDLE: standing, both arms relaxed at the sides, happy curved eyes open, gentle smile.
2. BLINK: exactly the same as pose 1 but the eyes are closed as soft happy downward arcs.
3. WAVE A: right arm raised high waving hello, slight head tilt, happy open eyes and smile.
4. WAVE B: exactly the same as pose 3 but with the raised arm lowered a little, as the second frame of a waving motion.

Background: transparent PNG. If you cannot produce transparency, use a perfectly flat solid magenta background (#FF00FF) with no gradient, no floor, no shadow, and no text. Do not add any props, logos or text other than the small compass-cross badge on the scarf. Very high resolution, crisp clean edges suitable for cutting out.
```

## Prompt 2: head icon for the top-right button (needed)

This is the small version. The full body is too detailed to read at 56 dp.

```text
Using the same mascot Nera as before, create a head-and-shoulders app-icon portrait: the rounded white head with the black glossy visor face, glowing curved smiling eyes and small smile, the antenna with the dark teal ball, and a hint of the teal scarf at the bottom. Front-facing and symmetrical, with big simple shapes so it stays readable at 48 pixels. Same soft 3D vinyl-toy style and the same palette (dark teal, white, light grey).

Create THREE images at the identical framing, scale and position, so they can be used as animation frames:
A. eyes open, happy
B. eyes closed as soft happy arcs (blink)
C. one eye winking with a tiny smile (playful)

Each image is square (1:1) with the head filling about 80% of the frame and a little margin on all sides. Background: transparent PNG. If you cannot produce transparency, use a perfectly flat solid magenta background (#FF00FF), with no gradient, no shadow and no text. Very high resolution, crisp edges.
```

## Prompt 3: chat state poses (nice to have)

These give the chat screen an avatar with a "thinking" state while Nera is working, plus friendly error and success
poses.

```text
Using the same mascot Nera as before, create a second character sheet in a 2x2 grid with the identical character, scale and camera angle as the first sheet, each pose centered with generous margin:
1. THINKING: one hand raised to the chin, eyes looking slightly up and to the side as small curved arcs, tiny thought-dot glow above the head (three small glowing teal dots, no text).
2. CELEBRATE: both arms raised in joy, big happy curved eyes and a wide smile, small sparkle shapes around (no confetti, no text).
3. SORRY: head slightly tilted, eyes as gentle downward-tilted arcs, one hand rubbing the back of the head, apologetic but still cute (no tears, nothing sad or scary).
4. PLANNING: holding a small folded paper map in both hands, looking down at it with happy curved eyes.

Same soft 3D vinyl-toy style and palette. Background: transparent PNG. If you cannot produce transparency, use a perfectly flat solid magenta background (#FF00FF) with no gradient, floor, shadow or text. Very high resolution, crisp edges.
```

---

## Quality checklist (check before sending me the files)

- [ ] Same character in every pose (same face proportions, same scarf, same teal).
- [ ] Eyes are glowing curved arcs on a black visor, with no pupils and no extra text.
- [ ] The head icon frames A, B and C line up exactly, so the blink looks smooth when swapped.
- [ ] Waving frames A and B differ only by the arm.
- [ ] No text, watermark, logo or extra objects, and no shadow or floor.
- [ ] Nothing looks scary or uncanny at small size.

## Files to send back

Save with these names if you can (any names are fine, I'll rename):

| File | From |
|---|---|
| `nera_sheet_main.png` | Prompt 1 |
| `nera_head_open.png`, `nera_head_blink.png`, `nera_head_wink.png` | Prompt 2 (A, B, C) |
| `nera_sheet_states.png` | Prompt 3 (optional) |

If the tool returns each pose as a separate image instead of a grid, that's even better. Just name them in
order.

## What I'll do with them

- Cut out the backgrounds and export crisp 512 px PNGs into the app's resources.
- Build the top-right button on My Trips: a gentle bob, a blink every few seconds, and a periodic wave with a small
  "Plan with Nera" speech bubble.
- Respect the "reduce motion" system setting (the button stays still).
- Move the entry point out of the "+" menu.
- Use the head icon as Nera's avatar in the chat, with the thinking pose while she is working.
