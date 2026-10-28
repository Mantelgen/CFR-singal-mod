# CFR visual proportions

The renderer uses one controller block entity and repeats static mast and ladder models; the visible signal is not a multiblock stack of block entities.

## World scale

- Modern signals render at least five one-block mast segments. Mechanical signals add one segment and render at least six.
- The modern head origin is 4.5 blocks above a default controller. After the shared 1.2 scale, the two-light head reaches about 5.66 blocks and the three-light head about 5.89 blocks, so both read as roughly six-block signals while remaining inside the renderer's existing bounds.
- The mast's widest reinforcing ribs span 3 model units (0.1875 blocks). The modern housing renders 8.4 model units wide (0.525 blocks), keeping a clear tall-mast/head relationship without making the head disappear at gameplay distance.
- Lenses render 2.4 model units across (0.15 blocks). The backing housing therefore remains substantially wider than each lamp.

## Family proportions

The two-light housing remains a compact 15-model-unit body with lamp centers separated by 6 units. The three-light family has its own 19.5-model-unit body and three evenly spaced lamp centers at Y=4, 9, and 14. It no longer inherits and crowds the two-light shell.

Both families use the same authored 7-unit housing width and the same lens diameter, then receive one shared renderer scale. This preserves a recognizable CFR family while giving the three-light head the extra vertical length it needs.

## Secondary components

- Ladder rails are 0.45 model units thick and 4.5 units across, with smaller rungs and close mast standoffs.
- The electrical cabinet is 3.5 units wide and 7 units tall, with a separate door, handle, hinges, and mast strap.
- The concrete foundation remains broad at 13 units so the tall, slender assembly still has a clear structural footing.
- Every modern housing is moved 3.5 model units toward its visible front. Its rear casing stops just ahead of the pole while the longer mounting spine reaches back into the pole hardware.
- Side mounting uses a centralized 3.5-model-unit lateral offset and a physical bracket reshaped to bridge the new forward position. Center alignment remains the modern default.

## Geometry budget

The heads remain cuboid-based and Blockbench-editable. Rounded silhouettes are made with stepped/chamfered cuboids, and the rear shell, spine, deep visors, open recess rings, and lenses remain separate static models. No geometry is created per frame.
