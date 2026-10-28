# Configuration UI

Milestone 27 keeps the wrench workflow and server-authoritative apply packet while improving the client editor. Configuration is opened and authorized with Create's native wrench.

The screen groups controls into Signal, Attachments, and Identification sections. Hover tooltips explain which settings are visual-only, plate input shows its validated eight-character limit, Enter applies, and Escape cancels. Draft values are stored independently of the widgets so a window resize or GUI-scale rebuild does not silently restore the values that were present when the screen opened.

Two-light and three-light families expose LEFT, CENTER, and RIGHT head alignment. Mechanical semaphores use their fixed arm mounting, so the alignment control becomes inactive when that family is selected. Its saved alignment value remains unchanged and can be used again after selecting a modern family.

Disabling the identification plate also disables its text and predefined position controls. The values remain in the draft and are not discarded merely because the plate is temporarily hidden.

The client still submits one bounded payload only when Apply is used. The server continues to validate family, alignment, plate position, text length/content, target block, player distance, and ownership of Create's wrench before changing the block entity. No railway graph or Create link is modified by the editor.
