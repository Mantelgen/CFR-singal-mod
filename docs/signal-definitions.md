# Signal definitions

CFR signal visual metadata is loaded from server data resources under
`data/<namespace>/cfr_signal_definitions/*.json`. Railway occupancy, aspect
resolution, networking, and semaphore animation remain implemented in Java.

Each definition contains:

- an optional `id` matching its resource location;
- `family`: `two_light`, `three_light`, `shunting`, `repeater`, `tmv`,
  `emergency`, `legacy_dwarf_entry`, or `mechanical_semaphore`;
- `head` and `mast` visual resource identifiers;
- the ordered physical `lights`;
- supported `attachments`;
- `default_mast_height` between 2 and 8;
- visual `default_alignment`: `left`, `center`, or `right`;
- a `translation_key` for display metadata.

The loader validates every resource independently. Invalid resources produce a
warning and are skipped. Java fallback definitions for all eight built-in
families are always retained, so a malformed resource cannot remove the stock
signals or crash a server reload.

Legacy mounting and finish fields remain readable for save compatibility.
Every family supports the physical base/pole/head mast assembly. Luminous
families can hang below a physical signal bridge, while mechanical semaphores
stand above its beam. Compatible families may also replace an intermediate pole
segment so that the mast continues inline above the head; compatible electric
families additionally support a head fixed directly to a solid wall. Each head
represents one independently linked signal.
Auxiliary display panels can show speed, next-speed, direction,
track, short-distance, and left-track indications. This composition means any
compatible head can be reused on the shared assembly without duplicating definitions.

The definitions deliberately contain no Create graph rules, occupancy logic,
packet behavior, or animation timing. Model resources referenced by custom
definitions still need to be supplied by a matching client resource pack.
