from collections import deque
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter


INPUTS = [
    Path("/Users/tonyaloysius/Documents/Apps - Playstore/Phone SS/iOS/phone/nerachat_1.png"),
    Path("/Users/tonyaloysius/Documents/Apps - Playstore/Phone SS/iOS/phone/nerachat_2.png"),
    Path("/Users/tonyaloysius/Documents/Apps - Playstore/Phone SS/iOS/phone/nerachat_3.png"),
]
OUTPUT_DIR = Path("/Users/tonyaloysius/Documents/Itinera/processed-images")


def remove_connected_black_background(image: Image.Image) -> Image.Image:
    rgba = image.convert("RGBA")
    pixels = rgba.load()
    width, height = rgba.size
    visited = bytearray(width * height)
    queue = deque()

    def eligible(x: int, y: int) -> bool:
        r, g, b, _ = pixels[x, y]
        return max(r, g, b) <= 34 and max(r, g, b) - min(r, g, b) <= 12

    def enqueue(x: int, y: int) -> None:
        index = y * width + x
        if not visited[index] and eligible(x, y):
            visited[index] = 1
            queue.append((x, y))

    for x in range(width):
        enqueue(x, 0)
        enqueue(x, height - 1)
    for y in range(height):
        enqueue(0, y)
        enqueue(width - 1, y)

    while queue:
        x, y = queue.popleft()
        for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
            if 0 <= nx < width and 0 <= ny < height:
                enqueue(nx, ny)

    for y in range(height):
        for x in range(width):
            if visited[y * width + x]:
                r, g, b, _ = pixels[x, y]
                level = max(r, g, b)
                alpha = 0 if level <= 3 else round(255 * (level - 3) / 31)
                pixels[x, y] = (r, g, b, max(0, min(255, alpha)))
    return rgba


def largest_opaque_component_bbox(image: Image.Image) -> tuple[int, int, int, int]:
    alpha = image.getchannel("A")
    width, height = image.size
    data = alpha.load()
    visited = bytearray(width * height)
    largest = (0, (0, 0, width, height))

    for y in range(height):
        for x in range(width):
            index = y * width + x
            if visited[index] or data[x, y] <= 8:
                continue
            visited[index] = 1
            queue = deque([(x, y)])
            count = 0
            min_x = max_x = x
            min_y = max_y = y
            while queue:
                cx, cy = queue.popleft()
                count += 1
                min_x, max_x = min(min_x, cx), max(max_x, cx)
                min_y, max_y = min(min_y, cy), max(max_y, cy)
                for nx, ny in ((cx - 1, cy), (cx + 1, cy), (cx, cy - 1), (cx, cy + 1)):
                    if 0 <= nx < width and 0 <= ny < height:
                        next_index = ny * width + nx
                        if not visited[next_index] and data[nx, ny] > 8:
                            visited[next_index] = 1
                            queue.append((nx, ny))
            if count > largest[0]:
                largest = (count, (min_x, min_y, max_x + 1, max_y + 1))
    return largest[1]


def remove_status_indicators(image: Image.Image) -> Image.Image:
    cleaned = image.convert("RGBA")
    draw = ImageDraw.Draw(cleaned)
    status_background = (10, 10, 10, 255)
    # Remove the time on the left and signal/Wi-Fi/battery group on the right.
    # The Dynamic Island occupies the untouched center of the status area.
    draw.rectangle((122, 136, 166, 153), fill=status_background)
    draw.rectangle((329, 136, 402, 153), fill=status_background)
    return cleaned


def process(source: Path, destination: Path, scale: int = 2, clean_status: bool = False) -> None:
    source_image = Image.open(source)
    if clean_status:
        source_image = remove_status_indicators(source_image)
    cutout = remove_connected_black_background(source_image)
    left, top, right, bottom = largest_opaque_component_bbox(cutout)
    # Six source pixels preserve the antialiased phone edge while excluding the
    # simulator toolbar, whose lower edge sits just above the phone.
    margin = 6
    left, top = max(0, left - margin), max(0, top - margin)
    right, bottom = min(cutout.width, right + margin), min(cutout.height, bottom + margin)
    cropped = cutout.crop((left, top, right, bottom))
    enlarged = cropped.resize((cropped.width * scale, cropped.height * scale), Image.Resampling.LANCZOS)
    alpha = enlarged.getchannel("A")
    rgb = enlarged.convert("RGB").filter(ImageFilter.UnsharpMask(radius=0.7, percent=55, threshold=3))
    final = rgb.convert("RGBA")
    final.putalpha(alpha)
    final.save(destination, format="PNG", optimize=True)


OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
for index, source in enumerate(INPUTS, start=1):
    process(
        source,
        OUTPUT_DIR / f"nerachat_{index}_transparent_hd.png",
        scale=4 if index in (2, 3) else 2,
        clean_status=index in (2, 3),
    )
