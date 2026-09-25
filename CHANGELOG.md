# 2.1.0
- Lateral texture overlay sections are now rendered below corners

# 2.0.3
- Fixed icon corner type not working properly on Floating and Badge tooltip layouts

# 2.0.2
- Added category buttons inside custom frames screens for better user experience (control+re/avg cycles sections)
- Shadow corner type now matches the background's corner type

# 2.0.1
- Added a new type of corner type: Full Thick
- Added a config option to change the icon border style: Style 1 (default) and Style 2
- Added a preview button inside the global config screen
- Rating text is now translatable using any translation key, and it is now previewed within its input box
- Added some critical cache limits to prevent memory leaks
- Fixed preview panel "cut" and "notch" corners adding extra pixels

# 2.0.0
- Added 3 brand-new tooltip styles that change the visual layout of the tooltip: Classic, Badge, Floating and Compact
- Added ingame editing for every active mod/resourcepack custom frames file
- Added a new effect and vignette ingame editor screens.
- Several tooltips can now be pinned to the screen at once (3 by default, configurable) through a configurable keybind (unbinded by default), with independent dragging, scrolling and closing
- Polished both ingame config and custom frames screens, with new animations, polished colors and better visual harmony
- Most effects have been reworked with a whole new polished look: Bubbles, Cinder, Crystals, Fireflies, Echo, Galaxy, Magic Orbs, Speed Lines, Spiral, Metal Shining, Rim Light, Ripples, Sonar, Stars
- Added 21 new effects: Astral, Fluorite, Aurora, Opal, Floating Stars, Steel Shining, Fireflies II, Snowfall, Prism, Comets, Storm, Sunbeams, Fireworks, Eruption, Searchlights, Blasts, Firebreath, Shield, Lasers, Missiles, Wisps
- Added 4 new divider line types: Dashed, Dotted, Ornament, Gradient Ornament
- Reworked the custom frames preview with a centered item and automatic fitting, no longer using a random mosaic placement
- Added reusable templates to custom frames json files which, once applied to certain items, inherit the same (overrideable) values defined in the template itself
- Added conditional custom frame rules based on remaining durability, custom item names, certain enchantments and specific tags/components
- Effects are now configurable (per intensity, deformation, saturation, colors, density, speed, etc.)
- Added a configurable keybind (unbinded by default) for fast accessing TO's ingame config screen
- Custom frames json file entries can now be dragged and reordered through the ingame config screen
- Custom frames json file entries can now specify their priority over other entries
- Custom frame entries are now updated automatically on mod/resourcepack updates only while the whole entry is unmodified
- Some config options (like icon animations) are now selectable instead of forcing the user to write them down
- Added frame editor validation, automatic backups, control+S support and draft support
- Added a browser for items, enchantments and frames, with a search bar, preview and multiple item selection support
- Added a configurable tooltip appear delay and an option to keep the tooltip settled when moving straight from one item to another, so sweeping across the inventory doesn't flash animations
- Added config options to cap the tooltip height and width as a percentage of the screen
- Added a config option to render effects behind the text, icon and frame
- Added a config option to show the item registry name (for example minecraft:oak_planks) without having to press F3+H
- Added a config option to show the item durability without having to press F3+H
- Added custom icons to the ingame custom frames config screen and others
- Added language localization to config entries and descriptions
- Icon background color and border color are now configurable
- The whole tooltip is now drawn above vanilla toasts (advancements, recipe unlocks, etc.), which used to slip between the text and the background (the depth is now configurable)
- Custom frames tooltip preview is now automatically updated once a config option is changed
- Long text tooltips now auto-scroll once, for players that might not know this functionality exists
- Internal refactor of the effect building pattern into a common canvas for much faster and better effect creation process, so expect more effects soon
- Split the tooltip animation into separate appear and disappear animations
- Replaced clickable cycling options with popup selection
- Custom frame editor changes are automatically recovered from a separate draft without applying them until Save, so no changes should be lost by mistake
- Updated the whole modpage and wiki
- Updated silver and amethyst frame overlay textures
- Updated some default config entries & added more custom frames entries by default
- Fixed tooltips appending the mod name even if it was already stated in the description
- Fixed the tooltip appear animation and icon animations replaying on every tick for items that update their NBT while hovered
- Fixed tooltips disappearing when a mod renders a second tooltip in the same frame
- Fixed custom tooltip components that render past their declared width (like Celestisynth's weapon ability box) being cut by the inner frame and divider line
- Fixed cinder effect not adapting to the whole tooltip proportions
- Fixed the tooltip shadow config option not covering the preview panel
- Fixed the custom frame overlay texture ignoring its own transparency on fade animations
- Fixed metal_shining effect ending too early on big tooltips
- Fixed two tooltips drawn in the same frame at the same spot overlapping each other, that happened for example when using the mod JEED, after hovering over status effect icons
- Fixed brighter corner and edge pixels on the inner frame while the tooltip fades in or out
- Fixed EMI recipe screens of mods whose id contains "tag" (Create: Vin"tag"e Improvements...) losing the hovered item
- Fixed tooltips not replaying animations on the second hover of the same item

# 1.5.2
- Updated localization files
- Added a global config option to always enable the equipment comparison functionality
- Now the mod isn't loaded when present on a dedicated server to preserve modpack parity
- Fixed a case where some appear animations (that made the icon transparent over the time) would crash the game when rendering items with a foil texture

# 1.5.1
- Added a global config option to always enable the equipment comparison functionality
- Now the mod isn't loaded when present on a dedicated server to preserve modpack parity
- Fixed a case where some appear animations (that made the icon transparent over the time) would crash the game when rendering items with a foil texture

# 1.5.0
- Reworked the in-game config screen
- Added a visual editor for the Tooltip Overhaul's custom_frames.json file (open it from the config screen), where you can also see a live tooltip preview that reacts as you type
- Config entries are now grouped in categories inside the TOML file, and existing values are migrated automatically
- Tweaked a bit the default color palettes, to make them more visually appeal
- Now the global color palettes can be changed without needing to restart the game
- Added 3 new effects: `fireflies`, `bubbles` and `crystals`
- Added tooltip appear/disappear hover animations, with 11 options: fade, pop, rise, unfold, zoom, slide, swing, emerge, squash, card and shake
- Added weapon/tool compare to the existing equipment compare functionality
- Added two config options to choose the tooltip corner style separately for the inner frame (`default`, `rounded`, `bevel`, `inner`, `cut`, `thick`, `bracket`, `block`, `notch`, `weld`, `gem`) and the outer background border (`default`, `square`, `rounded`, `notch`)
- Added Bedrock-like tooltip centering. When the tooltip fits on neither side of the cursor, it is centered horizontally and placed above/below the cursor instead of covering the hovered item
- Added a `rarity` option to custom_frames.json entries (a string or list of strings) to apply a frame to every item of the given rarity, including rarities added by other mods. More specific entries take precedence, in the following sequence -> item > tag > namespace > rarity
- Added a `CUSTOM_RARITY_PALETTE_COLORS` config entry, and is now the default palette for non-vanilla rarities (previously using the legendary palette), added along with a matching `custom_rarity` gradientType option for custom_frames.json
- Added a config option to append the name of the mod that adds the hovered item as the last tooltip line, in both survival and creative (previously only visible on creative mode)
- Added some micro-optimizations to some caching functions
- Fixed the thirst droplet icons (along with other icons) from the mod Tough As Nails not showing in tooltips
- Fixed FTB Quests tooltips rendering in the stackless style, now the hovered reward item is properly detected
- Fixed the icon flipping 180 degrees once the `rotate` / `rotate_zoom` appear animation ends
- Fixed a crash when a malformed color is set in any config entry, now it falls back to white and logs a warning instead
- Fixed the mod ItemZoom not working with tooltipoverhaul present
- Fixed tooltips extending off screen in Packed Up UIs
- Fixed shields being compared against the equipped leggings when holding the compare key

# 1.4.3
- Fixed a crash that could occur when rendering an armor stand in any tooltip, caused by yyzsbackpack

# 1.4.2
- Attempt to fix mouse scroll getting stuck on certain cases

# 1.4.1
- Fixed vignette style not working when applied by custom_frames.json files
- Fixed text renderer treating the first line as mandatory text line (fixing some Origins tooltips not showing up)
- Fixed typo in the inner overlay type config entry comment
- Fixed forge not detecting the EMI hovered stack if the mod was present

# 1.4.0

- Thanks for the million downloads!

- Complete internal rewrite. The mod is now structured in a more modular and maintainable way.
- Fixed all alignment issues, so the tooltip components now correctly adapt to the tooltip’s proportions when elements are moved, removed, or disabled.
- Fixed scrolling misalignment and now the text is properly clipped while scrolling (instead of whole lines disappearing).
- The entire code base has been optimized, reducing computational power.
- Added a new style: `vignette` (2 variants: "circular" and "circular_hole").
- Added `/tooltipoverhaul reload` command to reload `custom_frames.json` files without restarting the game.
- Added an in-game config screen. On Fabric, ModMenu is required to open it, but it’s not required to run the mod.
- Added 7 new effects: `magic_orbs`, `echo`, `speed_lines`, `galaxy`, `nebula`, `spiral`, `white_dust`.
- Added 12 new frames: `draconic_fire_frame.png`, `medieval_crossroads_frame.png`, `plasma_lamp_frame.png`, `neon_fire_frame.png`, `garden_frame.png`, `construction_punk_frame.png`, `cavemen_frame.png`, `nuclear_alarm_frame.png`, `gummy_bear_frame.png`, `greek_temple_frame.png`, `pure_alchemy_frame.png`, `skyland_frame.png`.
- Added 2 new divider line types: `static` and `linear` (divider type is now configurable).
- Added a config option to change the default position of the main tooltip.
- Added a new icon background type: `glow` (dynamic, it matches the inner frame color).
- Added config options to change padding and background color for empty tooltips (normal tooltips without an item stack).
- Added a config option to change the divider line’s top and bottom padding.
- Added a config option to change the preview panel (second panel) size.
- Added a config option to change the main panel position.
- Added a config option to enable tooltip drop shadow.
- Added a config option to change the preview model from a couple options: armor_stand or player_skin.
- Added a config option to enable whether the skin of the preview player skin model should be the local player skin or a dummy skin.
- Effects can now be set globally via config.
- Effects can now be chained together.
- The preview panel now automatically repositions itself when there isn’t enough space to the left (configurable).
- Preview panel renderers now auto-scale to the panel bounds and always adapt to the preview panel size.
- `ripples` effect now spawns ripples continuously. Their lifetime scales with the tooltip size.
- Enhanced the color approximator algorithm when using the "auto_" inner frame color predicate
- The color approximator algorithm now computes colors per frame instead of the whole frame file
- Now config entries that no longer exist are automatically purged
- Enhanced the line wrapper feature.
- Fixed a case where the line wrapping utility would double indent certain wrapped lines. 
- Fixed the preview panel renderer rotation anchor point not correctly positioned.
- Fixed tooltip position issues when moving the cursor, including size miscalculations with the equipment comparison feature.
- Fixed overlay textures not scaling correctly when the padding is modified.
- Fixed background color parsing, now uses hex strings (e.g. `"#FFFFFF"`) instead of numeric values (e.g. `-684233`).
- Fixed GL state error log spam after unbinding the equipment comparison keybind.
- Fixed inner frame colors not updating correctly based on the hovered stack information.
- Fixed non-working `colorItemRating` entry in `custom_frames.json`.
- Fixed “stack bleeding” on Forge where raw tooltips inherited the hovered stack info.
- Fixed `stars` effect anchoring to the screen instead of the tooltip position.
- Fixed incoherent speed of the cinders in the `cinder` effect.
- Fixed divider line color not adapting correctly to the inner frame
- Fixed a case where the title of some items was vertically misaligned
- Changed `guns_and_wires.png` overlay name to `guns_and_wires_frame.png`
- Removed: `titlePositionX`, `titlePositionY`, `ratingPositionX`, `ratingPositionY`, `tooltipDescriptionPositionX`, `tooltipDescriptionY` (not needed for the common use-case. Ask me if you need them back).
- Removed: `secondPanelRendererSize` (preview renderers now scale to the preview panel. Ask me if you need this entry back).
- Temporarily disabled `iconSize` config option (will be re-added in a future version).

+ IN UPCOMING UPDATES:
+ Visual rework of the tooltips
+ New config entry to apply custom frames to specific rarities
+ Weapon/Tool comparison
+ Templates for custom_frames.json files, in order to predefine styles and reduce redundancy

# 1.3.0
- Added an experimental equipment compare feature. The key mapping can be changed through the in-game controls menu.
- Fixed divider line not adapting correctly its color with certain items with a custom frame defined
- Fixed rating text adapting to the cursor position instead of the global size of the tooltip

# 1.2.0
- Now the custom_frames.json from tooltipoverhaul is located under config/tooltipoverhaul/custom_frames.json for better customization, without needing to create a texture pack to change the default custom frames. Any other mod/texture pack needs to create a texture pack including the custom_frames.json file under a certain modid, the same way as it has been done before.
- Now the color approximator algorithm handles colors in a better way
- Added a config option to disable the divider line
- Added a config option to change the default icon background
- Added 3 new icon backgrounds: void, slot, focus. slot_border is the default one
- Now the match_inner_frame_color config option matches the color even if the stack has a custom frame definition
- Added the cataclysm frame to some cataclysm items

# 1.1.2
- Now the tooltipoverhaul.toml config file will be inside config/tooltipoverhaul/tooltipoverhaul.toml instead of config/tooltipoverhaul.toml
- Added a config option to change the padding of the main tooltip
- Fixed a crash when loading the forge version
- Fixed icon animations not applying on items with a custom frame
- Fixed text position not correctly aligned when the rating is enabled
- Fixed padding auto alignment

# 1.1.1
- Added a config option to set custom frames to certain namespaces, apart from specific items and tags
- Enhanced config descriptions
- Added a config option to add a frame overlay texture to every single item (as a global config option)
- Added 8 new translation packages for the default rarities
- Fixed wrong rating translation key for items with a custom rarity
- Fixed rating color not applying correctly for custom frame itemstacks
- Fixed alexscaves rarities not translated correctly

# 1.1.0
IT IS RECOMMENDED TO DELETE THE CONFIG FILE (tooltipoverhaul.toml)

- Added 5 new overlay frames: cataclysm_frame.png, flower_frame.png, gear_frame.png, zombie_brain_frame.png and guns_and_wires.png
- Decreased the default size and corrected the position of the icon.
- Redesign of the following overlay frames: silver, amber, diamond and amethyst
- Now rating is disabled by default
- Optimized glint inner frame rendering so now performance should be increased slightly
- Optimized divider line rendering so now performance should be increased slightly
- Default rarity is now a translatable component, thus matching the current language package
- Added 16 brand new icon appear animations: zoom, rotate, rotate_fast, rotate_zoom, zoom_snap, skew, vibration, tilt_wave, flip, pendulum, bounce, go_down, pulse, fan_in, hover_pop and barrel_roll
- Config is now hotreloaded, which means you don’t need to reload the game in order to update the config options
- Decreased default tooltip padding
- Fixed EMI recipe categories not detecting the hovered stack correctly
- Fixed the tooltip position blocking the view on certain scenarios
- Fixed custom frames not working with item-tags only (thanks Elenterius)
- Fixed a bug where the second panel wasn’t disappearing completely after disabling it
- Fixed wrong icon rotation (now present in rotate and rotate_zoom animations)
- Fixed icon not scaled correctly on certain scenarios
- Fixed non-stack tooltip rendered with a wrong text position
- Fixed FTB Quests GUIs showing a previously hovered itemstack
- Fixed some items (icon) being larger than the tooltip itself or going outside the margins
- Fixed a hard crash when loading some Lethality item tooltips inside JEI categories
- Fixed color parser algorithm not approximating the color correctly
- Now this mod overrides modernfix and flerovium item model culling just on the hovered stack
- [Fabric only] Fixed an internal bug where tooltipoverhaul could crash if knightlib (although there is no direct link between both mods) was present in the same modpack, caused by a wrong shadowing in the package methodology
- Enhanced compatibility with apotheosis
- Celestisynth weapons now have scrolling state disabled by default
- Now using gradient as the default inner frame, instead of glint
- Default color palette is now darker
- Default background color is now black instead of dark blue
- Default icon background has been changed
- Certain overlay frames are now darker in order to reduce the visual noise due to big differences in the color schema
- Added a config option to change the color of the divider line. You can now select if it should match the inner frame color, the item name color or if you want to use a custom color
- Added a config option to modify the padding of the main panel
- Added a config option to modify the aligment of the title text and the rating text (left, middle or right)
- Added a config option to disable the item rating
- Added a config option to disable the icon renderer
- Added a config option to change the title, rating and content positions (x and y)
- Added a config option to modify the size of the icon
- Added a config option to modify the size of the second panel renderer
- Added a config option to modify the speed of the second panel renderer
- Added a config option to modify the second panel position (x and y)
- Added a config option to modify the default tooltip background color
- Added a config option to disable tooltip scrolling