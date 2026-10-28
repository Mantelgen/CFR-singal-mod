# Ponder integration review

Milestone 16 inspected the exact Ponder API bundled with Create 6.0.10:

- Ponder version: `1.0.82+mc1.21.1`;
- plugins implement `net.createmod.ponder.api.registration.PonderPlugin`;
- client registration uses `PonderIndex.addPlugin(...)`;
- scenes register through `PonderSceneRegistrationHelper` and
  `MultiSceneBuilder.addStoryBoard(...)`;
- scene programs receive the current `SceneBuilder` and `SceneBuildingUtil`;
- each storyboard expects a compressed structure template at
  `assets/<namespace>/ponder/<scene-path>.nbt`.

No CFR Ponder plugin is registered yet. The repository does not contain CFR
scene structure templates, and reusing Create's own train-signal templates
would display the wrong block and would not demonstrate the CFR controller's
track-targeting workflow. Adding empty or borrowed scenes would therefore be
misleading rather than useful documentation.

The scene implementation is intentionally deferred until the alignment,
attachment, and final model passes are stable. At that point the following
templates should be authored in-game and exported as Ponder NBT structures:

1. controller placement and Create track targeting;
2. red/green section operation;
3. configuration wrench and attachments;
4. two-light and three-light family selection;
5. mechanical semaphore stop/proceed movement.

This deferral introduces no server dependency or client registration hook and
does not affect normal gameplay. The verified API entry points above should be
rechecked only if the pinned Create/Ponder versions change.
