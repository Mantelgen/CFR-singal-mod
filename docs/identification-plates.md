# Identification plates

Milestones 23 and 24 use the existing persisted and synchronized identification fields:

- `identificationPlateEnabled` controls the complete plate module;
- `identificationText` stores the server-validated text;
- text is limited to eight letters, digits, spaces, or hyphens.

The plate is an ordinary static attachment model. A dark structural body and mast arm hold a slightly inset warm off-white face, leaving a visible frame. The renderer places dark text immediately in front of that face with polygon offset, so it reads as printed paint rather than a nametag.

Text layout is centered on both axes. Short values use the normal scale, while longer values shrink to a fixed maximum face width through `CFRSignalVisualLayout.identificationTextScale`. No per-value texture is generated and no unrestricted position is accepted from the client.

The attachment is placed below the modern head and below the mechanical mechanism. Its two rear bracket pieces physically reach the mast. Configuration persistence, block-entity update synchronization, and server packet validation use the existing `CFRSignalConfiguration` and networking path.

Milestone 25 adds `IdentificationPlatePosition` with `HIGH`, `NORMAL`, and `LOW`. These are predefined, family-aware vertical offsets; the client cannot submit arbitrary coordinates. Older version-4 configurations migrate to `NORMAL`. The selected position is persisted, synchronized, validated by enum range, and exposed as a cycle control in the configuration screen.
