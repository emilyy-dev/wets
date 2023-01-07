## WorldEdit Tick Spreader

Spread WorldEdit operations across multiple ticks.

That's all.

Semi-configurable, single command `/wets` (`/worldedit-tick-spreader`), three possible arguments:
* `/wets (sorted | not-sorted)` - whether blocks will be placed in a sorted arrangement or not. Sorted is default.
* `/wets <blocks-per-tick>` - Changes the amount of blocks that will be placed per tick.
Negative numbers result in a lot of blocks per tick (9223372036854775807). Default is 1.

Sorted placing and block count per tick, affected by commands, are per-player.

## License
Project licensed under the MIT license (also known as Expat license).

The Fabric build of this project redistributes
[lucko/fabric-permissions-api](https://github.com/lucko/fabric-permissions-api) which is licensed under the MIT license.
