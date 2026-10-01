# Umbralis: Más Allá del End (Fabric 1.20.1)

## Obtener el .jar
**Opción A – Sin instalar nada (GitHub):** sube esta carpeta a un repositorio nuevo. En la pestaña *Actions* se ejecuta
"Build mod"; al terminar, descarga el artefacto `umbralis-jar` (contiene `umbralis-1.0.0.jar`).

**Opción B – Local:** instala JDK 17 y Gradle 8.5+, y en esta carpeta ejecuta `gradle build`.
El .jar queda en `build/libs/umbralis-1.0.0.jar` (no uses el `-sources.jar`).

Instálalo en `.minecraft/mods` con Fabric Loader 0.15+ y **Fabric API 0.92.2+1.20.1**.

## Cómo entrar al Umbral
1. Derrota al Dragón del End (o usa creativo).
2. Fabrica la **Llave del Umbral**: 4 caparazones de shulker (esquinas), 4 obsidiana (laterales), 1 estrella del Nether (centro).
3. Construye un marco de obsidiana/purpur/obsidiana llorosa (mín. 2x3 de interior) y usa la llave sobre él. Tiene 4 usos.

## Dimensión
Terreno de obsidiana y purpur (con vetas) en 6 biomas: Agujas de Purpur, Bosque de Cristal, Páramos de Obsidiana,
Jardines Etéreos, Cañones Llorosos y Corazón del Umbral (raro; contiene las arenas con los 3 altares).

## Jefes (altares en el Corazón del Umbral)
| Jefe | Altar | Mecánica / punto débil |
|---|---|---|
| Coloso de Obsidiana | libre | Coraza frontal (15% de daño). Daño completo por la ESPALDA. Tras su golpe sísmico (salta para esquivarlo) queda aturdido con el NÚCLEO expuesto (x2.5). Fase 2+: meteoros. |
| Tejedora del Vacío | Corazón de Obsidiana | Invulnerable mientras vivan sus crías (y se cura con ellas). Al matarlas queda expuesta 10 s (x2). Dispara redes. |
| Monarca del Eclipse | Ojo de la Tejedora | Cuerpo casi inmune. Débil a flechas en la cabeza (x2.5) y a críticos al caer. Reflejos falsos, gravedad invertida, agujero negro + supernova (luego aturdido x2.5). |

El Peregrino Silente (amigable) susurra pistas; con un fragmento de amatista te bendice.

## Notas
- Las texturas de entidades se generan por script (`tools/generate_assets.py`) sobre modelos vanilla; puedes reemplazarlas.
- Para regenerar recursos: `python3 tools/generate_assets.py`.
