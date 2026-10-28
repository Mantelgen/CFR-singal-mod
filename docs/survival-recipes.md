# Survival recipes

Minecraft 1.21.1 loads recipes from the singular `data/<namespace>/recipe`
directory. CFR Signals registers separate base, pole, and signal-head items so
survival construction matches the in-world modular assembly.

## Signal base

The base uses smooth stone, iron ingots, and redstone. It contains the mandatory
electrical cabinet and forms the bottom of every valid signal.

## Signal pole

White concrete, red dye, and iron bars yield three pole blocks. Pole blocks
alternate red and white automatically when stacked and each includes a ladder
section.

## Signal head

The shaped recipe uses four iron sheets, two glass panes, one redstone torch,
and one Create electron tube. It yields one CFR signal head; its family,
alignment, indicator, and identification plate are selected after placement.

## Signal bridge beam

Two rows of three iron ingots around a middle row of three iron bars yield six
bridge-beam blocks. Place the first beam on a signal pole to form its post
connector, extend one side for an L-shaped cantilever or both sides for a
T-shaped bridge. Put luminous heads directly below the beam and mechanical
semaphores directly above it.

## Configuration wrench

Use Create's native wrench on a placed CFR signal head to open its configuration.
There is no separate CFR wrench recipe. The former CFR wrench stays registered
but hidden only so upgrading an existing world does not delete old inventory stacks.

These are initial survival costs and can be rebalanced later without changing
the registered items or saved signal data.
