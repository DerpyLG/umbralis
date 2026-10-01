#!/usr/bin/env python3
"""Genera texturas, modelos, lang, loot tables y datos de worldgen de Umbralis."""
import json, math, os, random, struct, zlib

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "umbralis")
DATA = os.path.join(RES, "data", "umbralis")
MC = os.path.join(RES, "data", "minecraft")


def wjson(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)


# ---------------------------------------------------------------- PNG ----
def write_png(path, rows):
    h = len(rows); w = len(rows[0])
    raw = b"".join(b"\x00" + bytes(c for px in row for c in px) for row in rows)
    def chunk(t, d):
        c = struct.pack(">I", len(d)) + t + d
        return c + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)
    png = (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
           + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


def cl(v): return max(0, min(255, int(v)))
def jit(c, a, r):
    d = r.randint(-a, a)
    return (cl(c[0] + d), cl(c[1] + d), cl(c[2] + d))
def lerp(a, b, t): return tuple(cl(a[i] + (b[i] - a[i]) * t) for i in range(3))


def noise_rows(w, h, base, var, accent, p, seed):
    r = random.Random(seed)
    rows = []
    for y in range(h):
        row = []
        for x in range(w):
            c = accent if r.random() < p else base
            row.append(jit(c, var, r) + (255,))
        rows.append(row)
    return rows


def block_tex(name, rows):
    write_png(os.path.join(ASSETS, "textures", "block", name + ".png"), rows)

def item_tex(name, rows):
    write_png(os.path.join(ASSETS, "textures", "item", name + ".png"), rows)

def blank(w=16, h=16): return [[(0, 0, 0, 0)] * w for _ in range(h)]


# ----------------------------------------------------- block textures ----
block_tex("umbral_stone", noise_rows(16, 16, (34, 22, 52), 12, (130, 70, 190), 0.07, 1))
block_tex("ether_moss", noise_rows(16, 16, (28, 88, 110), 16, (130, 255, 230), 0.09, 2))

r = random.Random(3); rows = []
for y in range(16):
    row = []
    for x in range(16):
        t = (x + y) / 30
        c = lerp((205, 60, 255), (90, 230, 255), t)
        if x in (0, 15) or y in (0, 15): c = lerp(c, (40, 10, 70), 0.55)
        elif (x * 3 + y * 5) % 11 == 0: c = lerp(c, (255, 255, 255), 0.5)
        row.append(jit(c, 8, r) + (255,))
    rows.append(row)
block_tex("void_crystal", rows)

rows = blank()
stem = (40, 150, 110)
for y in range(8, 16):
    rows[y][7] = stem + (255,); rows[y][8] = stem + (255,)
for dx, dy in [(0, 0), (-2, 0), (2, 0), (0, -2), (-1, -1), (1, -1), (-1, 1), (1, 1), (0, 1), (-2, -1), (2, -1)]:
    rows[5 + dy][7 + dx] = (110, 230, 255, 255)
rows[5][7] = (255, 240, 120, 255); rows[5][8] = (255, 240, 120, 255)
block_tex("ether_flower", rows)

r = random.Random(5); rows = []
for y in range(16):
    row = []
    for x in range(16):
        v = (math.sin(x * 0.7 + y * 0.45) + math.sin(y * 0.8 - x * 0.3) + 2) / 4
        c = lerp((60, 10, 120), (200, 90, 255), v)
        row.append(jit(c, 8, r) + (170,))
    rows.append(row)
block_tex("umbral_portal", rows)

def altar(name, rune):
    r = random.Random(hash(name) & 0xFFFF)
    rows = noise_rows(16, 16, (22, 14, 32), 8, (60, 30, 90), 0.1, 7)
    for i in range(16):
        for (x, y) in ((i, 0), (i, 15), (0, i), (15, i)):
            rows[y][x] = rune + (255,)
    for i in range(-5, 6):
        for (x, y) in ((8 + i, 8 + 5 - abs(i)), (8 + i, 8 - 5 + abs(i))):
            if 0 <= x < 16 and 0 <= y < 16: rows[y][x] = rune + (255,)
    block_tex(name, rows)
altar("altar_colossus", (255, 120, 30))
altar("altar_weaver", (90, 255, 120))
altar("altar_monarch", (255, 230, 140))

# ------------------------------------------------------ item textures ----
def shape_item(name, fn):
    rows = blank()
    for y in range(16):
        for x in range(16):
            c = fn(x, y)
            if c: rows[y][x] = c + (255,)
    item_tex(name, rows)

def heart(x, y):
    u = (x - 7.5) / 6.5; v = -(y - 7.0) / 6.5
    if (u * u + v * v - 1) ** 3 - u * u * v ** 3 <= 0:
        return lerp((60, 20, 110), (255, 100, 40), max(0, (y - 3) / 12) * 0.6 + (0.3 if x < 7 and y < 6 else 0))
shape_item("obsidian_heart", heart)

def eye(x, y):
    dx, dy = x - 7.5, y - 7.5
    if dx * dx / 49 + dy * dy / 20 <= 1:
        d = math.hypot(dx, dy)
        if d <= 1.4: return (10, 10, 10)
        if d <= 3.2: return (60, 230, 100)
        return (235, 255, 235)
shape_item("weaver_eye", eye)

def shard(x, y):
    if abs(x - 7.5) / 5 + abs(y - 7.5) / 7.2 <= 1:
        return lerp((120, 40, 220), (255, 255, 255), 1 - (y / 16) * 0.8 - (0.1 if x > 8 else 0))
shape_item("eclipse_shard", shard)

def key(x, y):
    d = math.hypot(x - 4.5, y - 4.5)
    if 1.6 <= d <= 3.9: return (230, 190, 90)
    if 7 <= x <= 14 and abs((x - 7) - (y - 7)) <= 0: return (190, 140, 255)
    if (x, y) in {(12, 10), (13, 10), (13, 11), (14, 12), (14, 13), (10, 12), (11, 12), (11, 13)}: return (230, 190, 90)
shape_item("umbral_key", key)

# ----------------------------------------------------- entity textures ----
def entity_tex(name, w, h, base, accent, p, eyes, eye_col, seed, biped=False, extra=None):
    rows = noise_rows(w, h, base, 10, accent, p, seed)
    for y in range(h):
        for x in range(w):
            if (x + y * 2) % 9 == 0 and random.Random(x * 131 + y * 7 + seed).random() < 0.35:
                rows[y][x] = jit(accent, 10, random.Random(x + y)) + (255,)
    if biped:
        for y in range(0, 16):
            for x in range(32, 64):
                rows[y][x] = (0, 0, 0, 0)
    if extra: extra(rows)
    for (ex, ey) in eyes:
        rows[ey][ex] = eye_col + (255,)
        if ex + 1 < w: rows[ey][ex + 1] = eye_col + (255,)
    write_png(os.path.join(ASSETS, "textures", "entity", name + ".png"), rows)

BIPED_EYES = [(9, 11), (13, 11)]
def core(rows):
    for y in range(20, 32):
        for x in range(20, 28):
            if math.hypot(x - 23.5, y - 25.5) <= 3.2: rows[y][x] = (255, 170 + (x * 7) % 60, 40, 255)
entity_tex("umbral_stalker", 64, 64, (28, 20, 40), (160, 80, 220), 0.08, BIPED_EYES, (255, 80, 255), 11, True)
entity_tex("umbral_wraith", 64, 64, (62, 42, 112), (96, 224, 255), 0.10, BIPED_EYES, (120, 255, 255), 12, True)
entity_tex("pilgrim", 64, 64, (170, 150, 200), (120, 200, 230), 0.07, BIPED_EYES, (40, 40, 70), 13, True)
entity_tex("obsidian_colossus", 64, 64, (18, 12, 26), (255, 110, 30), 0.12, BIPED_EYES, (255, 160, 40), 14, True, core)
entity_tex("eclipse_monarch", 64, 64, (10, 8, 20), (190, 130, 255), 0.09, BIPED_EYES, (255, 255, 255), 15, True)
entity_tex("umbral_bison", 64, 32, (70, 50, 110), (210, 200, 255), 0.10, [(7, 9), (12, 9)], (255, 230, 90), 16)
entity_tex("crystal_pig", 64, 32, (170, 120, 200), (240, 200, 255), 0.10, [(9, 10), (14, 10)], (60, 40, 90), 17)
entity_tex("purpur_crawler", 64, 32, (30, 18, 45), (200, 60, 255), 0.08,
           [(41, 14), (45, 14), (41, 16), (45, 16)], (255, 60, 255), 18)
entity_tex("void_weaver", 64, 32, (14, 28, 22), (90, 255, 120), 0.09,
           [(41, 14), (45, 14), (41, 16), (45, 16)], (140, 255, 160), 19)

# ------------------------------------------------- blockstates/models ----
def cube(name):
    wjson(f"{ASSETS}/blockstates/{name}.json", {"variants": {"": {"model": f"umbralis:block/{name}"}}})
    wjson(f"{ASSETS}/models/block/{name}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"umbralis:block/{name}"}})
    wjson(f"{ASSETS}/models/item/{name}.json", {"parent": f"umbralis:block/{name}"})

for n in ["umbral_stone", "ether_moss", "void_crystal", "altar_colossus", "altar_weaver", "altar_monarch"]:
    cube(n)

wjson(f"{ASSETS}/blockstates/ether_flower.json", {"variants": {"": {"model": "umbralis:block/ether_flower"}}})
wjson(f"{ASSETS}/models/block/ether_flower.json", {"parent": "minecraft:block/cross", "textures": {"cross": "umbralis:block/ether_flower"}})
wjson(f"{ASSETS}/models/item/ether_flower.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "umbralis:block/ether_flower"}})

wjson(f"{ASSETS}/blockstates/umbral_portal.json", {"variants": {
    "axis=x": {"model": "umbralis:block/umbral_portal_x"}, "axis=z": {"model": "umbralis:block/umbral_portal_z"}}})
face = lambda: {"uv": [0, 0, 16, 16], "texture": "#portal"}
wjson(f"{ASSETS}/models/block/umbral_portal_x.json", {
    "textures": {"particle": "umbralis:block/umbral_portal", "portal": "umbralis:block/umbral_portal"},
    "elements": [{"from": [0, 0, 6], "to": [16, 16, 10], "faces": {"north": face(), "south": face()}}]})
wjson(f"{ASSETS}/models/block/umbral_portal_z.json", {
    "textures": {"particle": "umbralis:block/umbral_portal", "portal": "umbralis:block/umbral_portal"},
    "elements": [{"from": [6, 0, 0], "to": [10, 16, 16], "faces": {"east": face(), "west": face()}}]})

for n in ["umbral_key", "obsidian_heart", "weaver_eye", "eclipse_shard"]:
    wjson(f"{ASSETS}/models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"umbralis:item/{n}"}})
EGGS = ["umbral_stalker", "umbral_wraith", "purpur_crawler", "umbral_bison", "crystal_pig", "pilgrim",
        "obsidian_colossus", "void_weaver", "eclipse_monarch"]
for n in EGGS:
    wjson(f"{ASSETS}/models/item/{n}_spawn_egg.json", {"parent": "minecraft:item/template_spawn_egg"})

# ------------------------------------------------------------- lang ----
ES = {
 "itemGroup.umbralis.main": "Umbralis: Más Allá del End",
 "block.umbralis.umbral_stone": "Piedra Umbral", "block.umbralis.ether_moss": "Musgo Etéreo",
 "block.umbralis.void_crystal": "Cristal del Vacío", "block.umbralis.ether_flower": "Flor Etérea",
 "block.umbralis.umbral_portal": "Portal del Umbral",
 "block.umbralis.altar_colossus": "Altar del Coloso", "block.umbralis.altar_weaver": "Altar de la Tejedora",
 "block.umbralis.altar_monarch": "Altar del Monarca",
 "item.umbralis.umbral_key": "Llave del Umbral",
 "item.umbralis.umbral_key.tooltip": "Úsala sobre un marco de obsidiana y purpur (mín. 2x3 interior).",
 "item.umbralis.obsidian_heart": "Corazón de Obsidiana", "item.umbralis.weaver_eye": "Ojo de la Tejedora",
 "item.umbralis.eclipse_shard": "Fragmento del Eclipse",
 "entity.umbralis.umbral_stalker": "Acechador Umbral", "entity.umbralis.umbral_wraith": "Espectro Umbral",
 "entity.umbralis.purpur_crawler": "Reptador de Purpur", "entity.umbralis.void_hatchling": "Cría del Vacío",
 "entity.umbralis.umbral_bison": "Bisonte Etéreo", "entity.umbralis.crystal_pig": "Cerdo de Cristal",
 "entity.umbralis.pilgrim": "Peregrino Silente", "entity.umbralis.obsidian_colossus": "Coloso de Obsidiana",
 "entity.umbralis.void_weaver": "Tejedora del Vacío", "entity.umbralis.eclipse_monarch": "Monarca del Eclipse",
 "entity.umbralis.eclipse_illusion": "Reflejo del Eclipse", "entity.umbralis.web_shot": "Red del Vacío",
 "biome.umbralis.purpur_spires": "Agujas de Purpur", "biome.umbralis.crystal_forest": "Bosque de Cristal",
 "biome.umbralis.obsidian_wastes": "Páramos de Obsidiana", "biome.umbralis.ethereal_gardens": "Jardines Etéreos",
 "biome.umbralis.crying_canyons": "Cañones Llorosos", "biome.umbralis.heart_of_umbral": "Corazón del Umbral",
 "message.umbralis.dragon_required": "El Umbral solo responde a quien ha derrotado al Dragón del End.",
 "message.umbralis.portal_failed": "No hay un marco válido (obsidiana/purpur, mínimo 2x3 de interior).",
 "message.umbralis.portal_in_umbral": "Ya estás en el Umbral.",
 "message.umbralis.no_dimension": "La dimensión del Umbral no está disponible.",
 "message.umbralis.altar_busy": "Ya hay un jefe despierto cerca.",
 "message.umbralis.altar_needs": "El altar exige: %s",
 "message.umbralis.altar_peaceful": "El altar no responde en dificultad Pacífica.",
 "boss.umbralis.phase": "¡%s entra en una nueva fase!",
 "boss.umbralis.defeated": "%s ha sido derrotado",
 "boss.umbralis.slam_incoming": "¡El Coloso alza los puños! ¡SALTA!",
 "boss.umbralis.core_exposed": "¡El núcleo del Coloso está expuesto! ¡Ataca ahora!",
 "boss.umbralis.weaver_shielded": "La Tejedora se alimenta de sus crías. ¡Acaba con ellas!",
 "boss.umbralis.weaver_exposed": "¡Sin crías, la Tejedora queda expuesta!",
 "boss.umbralis.monarch_mirrors": "¡Reflejos! El verdadero Monarca lleva una corona de luz.",
 "boss.umbralis.monarch_gravity": "¡La gravedad se invierte!",
 "boss.umbralis.monarch_blackhole": "¡Agujero negro! ¡Resiste o aléjate!",
 "boss.umbralis.monarch_exposed": "¡El corazón del Monarca queda expuesto!",
 "pilgrim.umbralis.blessing": "El Peregrino te bendice con luz de amatista.",
 "pilgrim.umbralis.lore.1": "El Coloso es una montaña de obsidiana: su pecho es roca viva, pero su espalda es de barro. Rodéalo.",
 "pilgrim.umbralis.lore.2": "Cuando el Coloso golpea el suelo, su núcleo queda al descubierto. Salta y luego castígalo.",
 "pilgrim.umbralis.lore.3": "La Tejedora vive de sus crías. Mata a las crías y caerá su escudo.",
 "pilgrim.umbralis.lore.4": "El Monarca se esconde tras sus reflejos. El verdadero lleva una corona de luz.",
 "pilgrim.umbralis.lore.5": "Dicen que al Monarca le duele una flecha en la cabeza, o un golpe que cae del cielo.",
}
EN = dict(ES)
EN.update({
 "item.umbralis.umbral_key.tooltip": "Use on an obsidian/purpur frame (min. 2x3 interior).",
 "block.umbralis.umbral_stone": "Umbral Stone", "block.umbralis.ether_moss": "Ether Moss",
 "block.umbralis.void_crystal": "Void Crystal", "block.umbralis.ether_flower": "Ether Flower",
 "block.umbralis.umbral_portal": "Umbral Portal", "block.umbralis.altar_colossus": "Colossus Altar",
 "block.umbralis.altar_weaver": "Weaver Altar", "block.umbralis.altar_monarch": "Monarch Altar",
 "item.umbralis.umbral_key": "Umbral Key", "item.umbralis.obsidian_heart": "Obsidian Heart",
 "item.umbralis.weaver_eye": "Weaver's Eye", "item.umbralis.eclipse_shard": "Eclipse Shard",
 "entity.umbralis.umbral_stalker": "Umbral Stalker", "entity.umbralis.umbral_wraith": "Umbral Wraith",
 "entity.umbralis.purpur_crawler": "Purpur Crawler", "entity.umbralis.void_hatchling": "Void Hatchling",
 "entity.umbralis.umbral_bison": "Ethereal Bison", "entity.umbralis.crystal_pig": "Crystal Pig",
 "entity.umbralis.pilgrim": "Silent Pilgrim", "entity.umbralis.obsidian_colossus": "Obsidian Colossus",
 "entity.umbralis.void_weaver": "Void Weaver", "entity.umbralis.eclipse_monarch": "Eclipse Monarch",
 "entity.umbralis.eclipse_illusion": "Eclipse Reflection", "entity.umbralis.web_shot": "Void Web",
 "biome.umbralis.purpur_spires": "Purpur Spires", "biome.umbralis.crystal_forest": "Crystal Forest",
 "biome.umbralis.obsidian_wastes": "Obsidian Wastes", "biome.umbralis.ethereal_gardens": "Ethereal Gardens",
 "biome.umbralis.crying_canyons": "Crying Canyons", "biome.umbralis.heart_of_umbral": "Heart of the Umbral",
 "message.umbralis.dragon_required": "The Umbral only answers to those who have slain the Ender Dragon.",
 "message.umbralis.portal_failed": "No valid frame (obsidian/purpur, min. 2x3 interior).",
 "message.umbralis.portal_in_umbral": "You are already in the Umbral.",
 "message.umbralis.no_dimension": "The Umbral dimension is unavailable.",
 "message.umbralis.altar_busy": "A boss is already awake nearby.",
 "message.umbralis.altar_needs": "The altar demands: %s",
 "message.umbralis.altar_peaceful": "The altar does not answer on Peaceful.",
 "boss.umbralis.phase": "%s enters a new phase!", "boss.umbralis.defeated": "%s has been defeated",
 "boss.umbralis.slam_incoming": "The Colossus raises its fists! JUMP!",
 "boss.umbralis.core_exposed": "The Colossus' core is exposed! Strike now!",
 "boss.umbralis.weaver_shielded": "The Weaver feeds on her brood. Slay them!",
 "boss.umbralis.weaver_exposed": "Without her brood, the Weaver is exposed!",
 "boss.umbralis.monarch_mirrors": "Reflections! The true Monarch wears a crown of light.",
 "boss.umbralis.monarch_gravity": "Gravity inverts!", "boss.umbralis.monarch_blackhole": "Black hole! Resist or flee!",
 "boss.umbralis.monarch_exposed": "The Monarch's heart is exposed!",
 "pilgrim.umbralis.blessing": "The Pilgrim blesses you with amethyst light.",
 "pilgrim.umbralis.lore.1": "The Colossus is a mountain of obsidian: its chest is living rock, but its back is clay. Circle it.",
 "pilgrim.umbralis.lore.2": "When the Colossus slams the ground, its core lies bare. Jump, then punish it.",
 "pilgrim.umbralis.lore.3": "The Weaver lives off her brood. Kill the hatchlings and her shield falls.",
 "pilgrim.umbralis.lore.4": "The Monarch hides behind reflections. The real one wears a crown of light.",
 "pilgrim.umbralis.lore.5": "They say an arrow to the Monarch's head hurts him, or a blow falling from the sky.",
})
for n in EGGS:
    k = f"item.umbralis.{n}_spawn_egg"
    ES[k] = f"Huevo de {ES['entity.umbralis.' + n]}"
    EN[k] = f"{EN['entity.umbralis.' + n]} Spawn Egg"
wjson(f"{ASSETS}/lang/es_es.json", ES)
wjson(f"{ASSETS}/lang/en_us.json", EN)

# ------------------------------------------------------ loot / recipe ----
def item_entry(name, lo=None, hi=None):
    e = {"type": "minecraft:item", "name": name}
    if lo is not None:
        e["functions"] = [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]
    return e

def entity_loot(name, pools):
    wjson(f"{DATA}/loot_tables/entities/{name}.json", {"type": "minecraft:entity", "pools": [
        {"rolls": 1, "entries": [p]} for p in pools]})

entity_loot("obsidian_colossus", [item_entry("umbralis:obsidian_heart"), item_entry("minecraft:obsidian", 8, 16)])
entity_loot("void_weaver", [item_entry("umbralis:weaver_eye"), item_entry("minecraft:string", 8, 16)])
entity_loot("eclipse_monarch", [item_entry("umbralis:eclipse_shard", 1, 3), item_entry("minecraft:nether_star")])
entity_loot("umbral_stalker", [item_entry("minecraft:ender_pearl", 0, 1)])
entity_loot("umbral_wraith", [item_entry("minecraft:amethyst_shard", 1, 3)])
entity_loot("purpur_crawler", [item_entry("minecraft:string", 0, 2), item_entry("minecraft:spider_eye", 0, 1)])
entity_loot("umbral_bison", [item_entry("minecraft:leather", 0, 2), item_entry("minecraft:beef", 1, 3)])
entity_loot("crystal_pig", [item_entry("minecraft:porkchop", 1, 3), item_entry("minecraft:amethyst_shard", 0, 1)])

for n in ["umbral_stone", "ether_moss", "void_crystal", "ether_flower"]:
    wjson(f"{DATA}/loot_tables/blocks/{n}.json", {"type": "minecraft:block", "pools": [{
        "rolls": 1.0, "bonus_rolls": 0.0, "entries": [{"type": "minecraft:item", "name": f"umbralis:{n}"}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

wjson(f"{DATA}/recipes/umbral_key.json", {
    "type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["SOS", "ONO", "SOS"],
    "key": {"S": {"item": "minecraft:shulker_shell"}, "O": {"item": "minecraft:obsidian"},
            "N": {"item": "minecraft:nether_star"}},
    "result": {"item": "umbralis:umbral_key"}})

wjson(f"{MC}/tags/blocks/mineable/pickaxe.json", {"replace": False, "values": ["umbralis:umbral_stone", "umbralis:void_crystal", "umbralis:ether_moss"]})
wjson(f"{MC}/tags/blocks/needs_diamond_tool.json", {"replace": False, "values": ["umbralis:umbral_stone"]})

# ------------------------------------------------------------ worldgen ----
wjson(f"{DATA}/dimension_type/umbral.json", {
    "ultrawarm": False, "natural": False, "coordinate_scale": 1.0, "has_skylight": False, "has_ceiling": False,
    "ambient_light": 0.12, "fixed_time": 18000, "monster_spawn_light_level": 0, "monster_spawn_block_light_limit": 0,
    "piglin_safe": False, "bed_works": False, "respawn_anchor_works": False, "has_raids": False,
    "logical_height": 256, "min_y": 0, "height": 256, "infiniburn": "#minecraft:infiniburn_end",
    "effects": "minecraft:the_end"})

for n, fo, amps in [("terrain", -7, [1.0, 1.0, 1.0, 0.5, 0.5]), ("strata", -6, [1.0, 1.0, 0.5]),
                    ("biome_temp", -7, [1.0, 1.0]), ("biome_humidity", -7, [1.0, 1.0])]:
    wjson(f"{DATA}/worldgen/noise/{n}.json", {"firstOctave": fo, "amplitudes": amps})

def dnoise(n, xz, y): return {"type": "minecraft:flat_cache", "argument": {
    "type": "minecraft:noise", "noise": f"umbralis:{n}", "xz_scale": xz, "y_scale": y}}

# reglas de superficie
def rb(name): return {"type": "minecraft:block", "result_state": {"Name": name}}
def cond(c, t): return {"type": "minecraft:condition", "if_true": c, "then_run": t}
def seq(*r): return {"type": "minecraft:sequence", "sequence": list(r)}
def in_biome(b): return {"type": "minecraft:biome", "biome_is": ["umbralis:" + b]}
def nthr(lo, hi): return {"type": "minecraft:noise_threshold", "noise": "umbralis:strata", "min_threshold": lo, "max_threshold": hi}
def sdepth(add): return {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor", "add_surface_depth": add, "secondary_depth_range": 0}
def yabove(y): return {"type": "minecraft:y_above", "anchor": {"absolute": y}, "surface_depth_multiplier": 0, "add_stone_depth": False}
def mix(a, b, lo, hi): return seq(cond(nthr(lo, hi), rb(a)), rb(b))
def surf(top, sub): return seq(cond(sdepth(False), top), cond(sdepth(True), sub))

P, O, CO = "minecraft:purpur_block", "minecraft:obsidian", "minecraft:crying_obsidian"
US, EM = "umbralis:umbral_stone", "umbralis:ether_moss"
surface = {
    "purpur_spires": surf(mix(P, O, -2, 0.3), rb(P)),
    "crystal_forest": surf(mix("minecraft:amethyst_block", US, 0.45, 2), rb(US)),
    "obsidian_wastes": surf(mix(CO, O, 0.5, 2), rb(O)),
    "ethereal_gardens": surf(rb(EM), mix(P, US, -2, 0)),
    "crying_canyons": surf(mix("minecraft:magma_block", CO, 0.55, 2), rb(O)),
    "heart_of_umbral": surf(mix(CO, O, -2, -0.1), rb(P)),
}
bedrock = cond({"type": "minecraft:vertical_gradient", "random_name": "umbralis:bedrock_floor",
                "true_at_and_below": {"above_bottom": 0}, "false_at_and_above": {"above_bottom": 5}}, rb("minecraft:bedrock"))
deep = seq(cond(yabove(100), mix(P, O, 0.35, 2)), cond(yabove(60), mix(P, O, -0.15, 0.15)),
           mix("minecraft:purpur_pillar", O, -2, -0.4))
surface_rule = seq(bedrock, *[cond(in_biome(b), r) for b, r in surface.items()], deep)

wjson(f"{DATA}/worldgen/noise_settings/umbral.json", {
    "sea_level": 0, "disable_mob_generation": False, "aquifers_enabled": False, "ore_veins_enabled": False,
    "legacy_random_source": False,
    "default_block": {"Name": "minecraft:obsidian"}, "default_fluid": {"Name": "minecraft:water"},
    "noise": {"min_y": 0, "height": 256, "size_horizontal": 1, "size_vertical": 2},
    "noise_router": {
        "barrier": 0, "fluid_level_floodedness": 0, "fluid_level_spread": 0, "lava": 0,
        "temperature": dnoise("biome_temp", 1.0, 0.0), "vegetation": dnoise("biome_humidity", 1.0, 0.0),
        "continents": 0, "erosion": 0, "depth": 0, "ridges": 0, "initial_density_without_jaggedness": 0,
        "final_density": {"type": "minecraft:interpolated", "argument": {
            "type": "minecraft:add",
            "argument1": {"type": "minecraft:y_clamped_gradient", "from_y": 0, "to_y": 170, "from_value": 1.4, "to_value": -1.6},
            "argument2": {"type": "minecraft:mul", "argument1": 1.1,
                          "argument2": {"type": "minecraft:noise", "noise": "umbralis:terrain", "xz_scale": 1.0, "y_scale": 0.6}}}},
        "vein_toggle": 0, "vein_ridged": 0, "vein_gap": 0},
    "spawn_target": [], "surface_rule": surface_rule})

def climate(t, h):
    return {"temperature": t, "humidity": h, "continentalness": 0, "erosion": 0, "weirdness": 0, "depth": 0, "offset": 0}
wjson(f"{DATA}/dimension/umbral.json", {"type": "umbralis:umbral", "generator": {
    "type": "minecraft:noise", "settings": "umbralis:umbral",
    "biome_source": {"type": "minecraft:multi_noise", "biomes": [
        {"biome": "umbralis:purpur_spires", "parameters": climate([-2, -0.15], [-2, 0])},
        {"biome": "umbralis:crystal_forest", "parameters": climate([-2, -0.15], [0, 2])},
        {"biome": "umbralis:obsidian_wastes", "parameters": climate([-0.15, 0.15], [-2, 0])},
        {"biome": "umbralis:ethereal_gardens", "parameters": climate([-0.15, 0.15], [0, 2])},
        {"biome": "umbralis:crying_canyons", "parameters": climate([0.15, 0.45], [-2, 2])},
        {"biome": "umbralis:heart_of_umbral", "parameters": climate([0.45, 2], [-2, 2])}]}}})

# features
def placed(name, feature, *placement):
    wjson(f"{DATA}/worldgen/placed_feature/{name}.json", {"feature": f"umbralis:{feature}", "placement": list(placement)})
count = lambda n: {"type": "minecraft:count", "count": n}
rarity = lambda n: {"type": "minecraft:rarity_filter", "chance": n}
SQ = {"type": "minecraft:in_square"}
HM = {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}
BI = {"type": "minecraft:biome"}

for n, t in [("spire", "umbralis:spire"), ("crystal_tree", "umbralis:crystal_tree"), ("boss_arena", "umbralis:boss_arena")]:
    wjson(f"{DATA}/worldgen/configured_feature/{n}.json", {"type": t, "config": {}})
wjson(f"{DATA}/worldgen/configured_feature/obsidian_rock.json", {"type": "minecraft:forest_rock", "config": {"state": {"Name": O}}})
wjson(f"{DATA}/worldgen/configured_feature/crying_rock.json", {"type": "minecraft:forest_rock", "config": {"state": {"Name": CO}}})
wjson(f"{DATA}/worldgen/configured_feature/ether_flower_patch.json", {"type": "minecraft:random_patch", "config": {
    "tries": 48, "xz_spread": 7, "y_spread": 3, "feature": {
        "feature": {"type": "minecraft:simple_block", "config": {"to_place": {
            "type": "minecraft:simple_state_provider", "state": {"Name": "umbralis:ether_flower"}}}},
        "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {
            "type": "minecraft:matching_blocks", "blocks": "minecraft:air"}}]}}})

placed("spire_dense", "spire", count(5), SQ, HM, BI)
placed("spire_sparse", "spire", rarity(2), SQ, HM, BI)
placed("crystal_tree_dense", "crystal_tree", count(5), SQ, HM, BI)
placed("crystal_tree_sparse", "crystal_tree", rarity(3), SQ, HM, BI)
placed("obsidian_rock", "obsidian_rock", rarity(2), SQ, HM, BI)
placed("crying_rock", "crying_rock", rarity(2), SQ, HM, BI)
placed("ether_flower_patch", "ether_flower_patch", count(3), SQ, HM, BI)
placed("boss_arena", "boss_arena", rarity(5), SQ, HM, BI)

def spawn(t, w, lo, hi): return {"type": f"umbralis:{t}", "weight": w, "minCount": lo, "maxCount": hi}
def biome(name, sky, fog, water, monsters, creatures, feats, particle, ambient, temp):
    veg = [f"umbralis:{f}" for f in feats]
    features = [[] for _ in range(11)]
    features[9] = veg
    wjson(f"{DATA}/worldgen/biome/{name}.json", {
        "has_precipitation": False, "temperature": temp, "downfall": 0.0,
        "creature_spawn_probability": 0.2 if creatures else 0.0,
        "effects": {"sky_color": sky, "fog_color": fog, "water_color": water, "water_fog_color": fog,
                    "particle": {"options": {"type": particle}, "probability": 0.004},
                    "ambient_sound": ambient},
        "spawners": {"monster": monsters, "creature": creatures, "ambient": [], "axolotls": [],
                     "underground_water_creature": [], "water_creature": [], "water_ambient": [], "misc": []},
        "spawn_costs": {}, "carvers": {}, "features": features})

biome("purpur_spires", 0x2a1048, 0x3a1a60, 0x8a50c0,
      [spawn("umbral_stalker", 8, 1, 2), spawn("purpur_crawler", 10, 2, 3)], [],
      ["spire_dense", "obsidian_rock"], "minecraft:reverse_portal", "minecraft:ambient.soul_sand_valley.loop", 0.3)
biome("crystal_forest", 0x1a2a58, 0x2a3a78, 0x60a0e0,
      [spawn("umbral_wraith", 6, 1, 1), spawn("purpur_crawler", 8, 1, 2)],
      [spawn("crystal_pig", 8, 2, 4), spawn("umbral_bison", 4, 2, 3)],
      ["crystal_tree_dense", "ether_flower_patch", "spire_sparse"], "minecraft:glow", "minecraft:ambient.warped_forest.loop", 0.4)
biome("obsidian_wastes", 0x10081c, 0x1a0c2c, 0x6040a0,
      [spawn("umbral_stalker", 12, 1, 2), spawn("umbral_wraith", 6, 1, 1), spawn("purpur_crawler", 8, 2, 3)], [],
      ["obsidian_rock", "spire_sparse", "crying_rock"], "minecraft:portal", "minecraft:ambient.basalt_deltas.loop", 0.5)
biome("ethereal_gardens", 0x203a5c, 0x305a7c, 0x70d0e0,
      [spawn("purpur_crawler", 1, 1, 1)],
      [spawn("umbral_bison", 10, 2, 4), spawn("crystal_pig", 10, 2, 4), spawn("pilgrim", 3, 1, 1)],
      ["ether_flower_patch", "crystal_tree_sparse"], "minecraft:end_rod", "minecraft:ambient.warped_forest.loop", 0.6)
biome("crying_canyons", 0x301048, 0x4a1a5a, 0x9040b0,
      [spawn("umbral_wraith", 10, 1, 2), spawn("umbral_stalker", 8, 1, 2)], [],
      ["crying_rock", "spire_sparse"], "minecraft:dripping_obsidian_tear", "minecraft:ambient.crimson_forest.loop", 0.7)
biome("heart_of_umbral", 0x080410, 0x140820, 0xa060ff,
      [spawn("umbral_stalker", 10, 1, 2), spawn("umbral_wraith", 8, 1, 2), spawn("purpur_crawler", 8, 2, 3)], [],
      ["boss_arena", "spire_sparse", "crying_rock"], "minecraft:reverse_portal", "minecraft:ambient.soul_sand_valley.loop", 0.9)

print("Recursos generados.")
