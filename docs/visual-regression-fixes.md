# Visual regression fixes before Milestone 28

Three reported rendering defects are corrected before performance work begins.

- The block entity now uses a configuration-independent render box with a three-block horizontal radius and enough vertical space for the tallest supported mast. This contains the full 2.3-block semaphore arm throughout its rotation and prevents frustum culling when the controller block itself leaves the camera view.
- Identification plates render at 1.6 times their former size around the mast center. The complete plate, mounting bracket, and dynamic text scale together, while the text receives a larger forward separation from the painted face to avoid z-fighting.
- Mechanical models use the already loaded CFR mast red/white, signal metal, and black housing textures. The mechanical-only PNGs remain source assets, but rendered models no longer depend on those additional atlas entries, preventing missing-texture magenta on semaphore parts.

These are visual changes only. Signal configuration, Create railway state, networking, aspect resolution, and semaphore animation targets are unchanged.

## Screenshot-driven correction

A later in-game screenshot exposed three remaining presentation issues. Plate lettering now rotates 180 degrees to face the same negative-Z side as the visible CFR lenses. The counterweight and pivot cap use neutral dark metal instead of colored paint, preventing red hardware from reading as pink under low biome/night lighting. Texture integrity tests reject not only bright fallback magenta but also darker purple/magenta-family pixels.

Close-range testing exposed two additional renderer-specific causes. Standalone component geometry now uses a two-sided cutout buffer, so edge-on and mirrored faces are not removed by GPU back-face culling. Black head, hood, and mechanical-housing surfaces use strictly grayscale charcoal textures. Normal scene lighting and ambient occlusion remain active so the materials retain depth instead of looking flat.

The three-light housing's stepped cap extends above vanilla model coordinate 16. Its former implicit face UVs sampled beyond the charcoal sprite and into a neighboring atlas region, which produced the remaining purple patch. Every face on that out-of-bounds housing geometry now has explicit in-sprite UV coordinates, backed by an asset regression test.

For very close oblique views, the modern head shells now have at least 4.375 model units of depth and the mechanical housing has 5 model units. This keeps a side or rear surface in front of the near plane longer as the camera approaches, while the two-sided render layer keeps those surfaces visible. The red and charcoal material sources also received a modest brightness and contrast lift without relaxing the no-magenta checks.

The renderer now opts out of vanilla block-entity frustum rejection. A CFR signal is a several-block assembly rendered by one controller block, and edge-on camera angles could reject the controller's renderer even with an expanded box. View-distance rejection remains active, but nearby signal geometry is now submitted consistently at every viewing angle.
