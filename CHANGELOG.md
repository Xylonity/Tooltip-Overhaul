# 1.4.0
- Complete internal rewrite. The mod is now structured in a more modular and maintainable way.
- Fixed all alignment issues, so the tooltip components now correctly adapt to the tooltip’s proportions when elements are moved, removed, or disabled.
- Fixed scrolling misalignment and now the text is properly clipped while scrolling (instead of whole lines disappearing).
- The entire code base has been optimized, reducing computational power.
- Added a new style: `vignette` (2 variants: "circular" and "circular_hole").
- Added `/tooltipoverhaul reload` command to reload `custom_frames.json` files without restarting the game.
- Added an in-game config screen. On Fabric, ModMenu is required to open it, but it’s not required to run the mod.
- Added 7 new effects: `magic_orbs`, `echo`, `speed_lines`, `galaxy`, `nebula`, `spiral`, `white_dust`.
- Added 8 new frames: `draconic_fire_frame.png`, `medieval_crossroads_frame.png`, `plasma_lamp_frame.png`, `neon_fire_frame.png`, `garden_frame.png`, `construction_punk_frame.png`, `cavemen_frame.png`, `nuclear_alarm_frame.png`.
- Added 2 new divider line types: `static` and `linear` (divider type is now configurable).
- Added a config option to change the default position of the main tooltip.
- Added a new icon background type: `glow` (dynamic, it matches the inner frame color).
- Added config options to change padding and background color for empty tooltips (normal tooltips without an item stack).
- Added a config option to change the divider line’s top and bottom padding.
- Added a config option to change the preview panel (second panel) size.
- Added a config option to change the main panel position.
- Added a config option to enable tooltip drop shadow.
- Effects can now be set globally via config.
- Effects can now be chained together.
- The preview panel now automatically repositions itself when there isn’t enough space to the left (configurable).
- Preview panel renderers now auto-scale to the panel bounds and always adapt to the preview panel size.
- `ripples` effect now spawns ripples continuously. Their lifetime scales with the tooltip size.
- Enhanced the color approximator algorithm when using the "auto_" inner frame color predicate
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
- Changed `guns_and_wires.png` overlay name to `guns_and_wires_frame.png`
- Removed: `titlePositionX`, `titlePositionY`, `ratingPositionX`, `ratingPositionY`, `tooltipDescriptionPositionX`, `tooltipDescriptionY` (not needed for the common use-case. Ask me if you need them back).
- Removed: `secondPanelRendererSize` (preview renderers now scale to the preview panel. Ask me if you need this entry back).
- Temporarily disabled `iconSize` config option (will be re-added in a future version).

+ IN UPCOMING UPDATES:
+ New config entry to apply custom frames to specific rarities
+ Weapon/Tool comparison

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