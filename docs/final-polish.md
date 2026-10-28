# Final polish

Optional Milestone 30 improves the player-facing parts of the existing workflow without adding another configuration path.

## Items and creative tab

The CFR Signal Controller tooltip explains that the block is placed beside railway track, targets a Create signal edge, and is visually configured with Create's native wrench.

The CFR Signals creative tab exposes the physical signal components and controller. The internal test block and legacy CFR wrench remain registered for development/old-world compatibility but are not shown to normal players in the tab.

Create's actual wrench now opens the editor and is rechecked by the server when configuration is applied. This uses Create's own item model and hand transforms instead of duplicating them on a custom item.

## Localization

`en_us` and `ro_ro` contain identical key sets covering item names, definitions, aspects, diagnostics, the complete configuration screen, tooltips, alignment, plate position, and state labels. `LocalizationIntegrityTest` rejects missing or blank values in either language.

## Existing polish retained

- Mechanical STOP/PROCEED transitions use one local iron mechanism clunk when the target changes, not per-frame packets or sounds.
- Modern signals default to centered heads.
- New configurations enable the supported ladder, cabinet, and identification plate with `X4` text.
- Mechanical animation remains smoothly client-interpolated.

## Deliberately deferred

No placeholder ambient loop or copied sound asset is introduced. Ponder scene registration remains deferred until dedicated structure templates are authored, as recorded in `ponder-integration.md`. These choices avoid shipping brittle demonstrations or inappropriate third-party assets solely to fill an optional checklist.
