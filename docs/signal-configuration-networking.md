# Signal configuration and networking

The client configuration screen uses Create's native wrench without introducing a container menu or continuous synchronization. The former CFR wrench is retained only as an old-world compatibility item.

## Interaction flow

1. The player uses Create's wrench on a CFR signal controller.
2. The logical server reads the authoritative block-entity configuration and sends one `OpenSignalConfigurationPayload` to that player.
3. The client edits a local snapshot. Changing controls does not send packets.
4. Apply sends one `ApplySignalConfigurationPayload` to the server.
5. The server validates the request and updates the block entity through `setConfiguration`, which persists and synchronizes the result.

The editable settings are the signal family, left/center/right head alignment, ladder, electrical cabinet, identification plate, and plate text. New modern two-light and three-light signals default to CENTER.

## Validation

The server accepts an update only when:

- the sender is a server player with build permission;
- the player is still holding Create's wrench;
- the controller is in a loaded chunk and within eight blocks;
- the target block entity is a CFR signal controller;
- the family and head-alignment identifiers are valid;
- plate text is at most eight characters and contains only letters, digits, spaces, or hyphens.

The string codec also applies the eight-character limit while decoding, preventing an oversized text allocation. Mast height is not present in the client payload and remains unchanged by the editor.

## Client/server separation

Payload records and server validation live in the common `network` package. The screen and clientbound handler registration live under `client`; dedicated servers never load screen classes. Rendering uses the synchronized configuration to position the head and draw the plate text. LEFT and RIGHT use a mirrored side-mount bracket; CENTER keeps the housing's geometric center on the mast centerline.
