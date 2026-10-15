# NexoAddon
Backward compability and new features for Nexo plugin!

[![CodeFactor](https://www.codefactor.io/repository/github/naimadx123/nexoaddon/badge)](https://www.codefactor.io/repository/github/naimadx123/nexoaddon)

---

### 📕 [Documentation](https://nexoaddon.gitbook.io/nexoaddon-docs)

### 📥 Download
- [Polymart](https://polymart.org/r/6950)
- [Spigot](https://www.spigotmc.org/resources/nexoaddon.121241)

### 📞 Contact
- [Discord](https://discord.com/invite/aSRYxqSjVJ)
---
### ✍ Description
NexoAddon is a powerful expansion for the Nexo plugin, bringing high-performance gameplay mechanics, block behaviors, and data-driven customization to your Minecraft server using Nexo-managed assets.

Designed as a feature-rich addon for servers using Nexo, NexoAddon enhances your content with intuitive mechanics.

### ✨ Core Features:

🔧 **Block & Item Mechanics**
* Aura / BlockAura – Area-based effects for entities or surrounding blocks.
* BigMining – Mine multiple blocks with one action.
* BedrockBreak – Add logic to break normally unbreakable blocks.
* BottledExp – Bottle and retrieve experience.
* Decay – Leaves-style decay for CustomBlocks.
* Infested – Silverfish-like infested block behavior.
* KillMessage – Custom messages on entity death.
* MiningTools – Restrict breaking to specific tools.
* Repair – Custom repair logic using defined items.
* ShiftBlock – Swap blocks & furniture.
* Signal – Redstone-style logic system for CustomBlocks.
* SpawnerBreak – Custom spawner-breaking conditions.
* Stackable / Unstackable – Toggle stacking on items.

⚙️ **Utility Components**
* equippable – Control equippable item logic.
* fertilizer – Trigger effects using bonemeal.
* jukebox_playable – Play custom items in jukeboxes.
* skull_value – Register & handle custom skulls.

🗿 **Totem Animation**
* Play custom totem animation

🌱 **World Populators**
* Modify terrain generation with ores, decorations, or block overrides.
* Choose automatic populators or the `NexoAddon` world generator, using the same rules.

⚒️ **Crafting Enhancements**
* Smithing Recipe Support.
* CustomCrafting – Slot-based recipes inside your own menu, opened from a CustomBlock or Furniture.

🆕 **And more coming soon...**

🛠️ Built for Performance:
* Lightweight, optimized, and fully modular.
* Easily configurable via `config.yml`.
* Seamless compatibility with Nexo item system.

### World Generation

By default, NexoAddon attaches its populators to worlds without the `NexoAddon` generator. To create a world using the generator with Multiverse-Core:

```text
/mv create custom_world normal --generator NexoAddon
```

The generator keeps vanilla terrain and applies block replacement rules directly to chunk data before vanilla decorations. Placement rules using `place_on`, `place_below` or `place_side`, furniture and saplings run on the owning region after the chunk is generated. Biome changes also run after decoration, preserving vanilla trees and plants. Both modes use `populators/block_populator.yml`, `populators/blocks/*.yml` and the biome populator files. Set the rule's `worlds` list to include the new world.

To use only explicitly selected generator worlds, disable automatic populators in `config.yml`:

```yaml
populators:
  enabled: false
```

The generator remains available with this setting. Select it when creating or loading a world after NexoAddon is enabled. Existing chunks are unaffected; new chunks use the selected mode. NexoAddon does not attach its populator to worlds using its generator.

### 📥 **Downloads & Docs:**  
Available on [Polymart/voxel.shop](<https://voxel.shop/product/6950/nexoaddon>), [Spigot](<https://www.spigotmc.org/resources/nexoaddon.121241/>), and [GitHub](<https://github.com/Naimadx123/NexoAddon>).  
[Join our Discord](https://discord.gg/aSRYxqSjVJ) for support, tutorials, and feature previews!
[Documentation](<https://nexoaddon.gitbook.io/docs>)

🔗 Expand your Nexo-powered server with deep custom interactions using **NexoAddon**.

---
### 👥 Contributors
<a href="https://github.com/Naimadx123/NexoAddon/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=Naimadx123/NexoAddon&max=30" alt="Contributors"/>
</a>
