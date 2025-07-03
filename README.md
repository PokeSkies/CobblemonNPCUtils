# CobblemonNPCUtils
<img height="50" src="https://camo.githubusercontent.com/a94064bebbf15dfed1fddf70437ea2ac3521ce55ac85650e35137db9de12979d/68747470733a2f2f692e696d6775722e636f6d2f6331444839564c2e706e67" alt="Requires Fabric Kotlin"/>

A Fabric server-sided mod that adds useful utility features for working with Cobblemon's NPCs!

More information on configuration can be found on the [Wiki](https://github.com/PokeSkies/CobblemonNPCUtils/wiki)!

## Features
- Item Presets and 4 related Molang Functions
- Location Presets and 1 related Molang Functions
- 4 Economy Molang Functions

## Installation
1. Download the latest version of the mod from [Modrinth](https://modrinth.com/mod/cobblemonnpcutils).
2. Download all required dependencies:
    - [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
    - [Fabric Permissions API](https://github.com/PokeSkies/fabric-permissions-api)
    - [Cobblemon](https://modrinth.com/mod/cobblemon)
3. Install the mod and dependencies into your server's `mods` folder.
4. Configure the config files in the `./config/cobblemonnpcutils/` folders


## Commands/Permissions
| Command                             | Description                                                                 | Permission                         |
|-------------------------------------|-----------------------------------------------------------------------------|------------------------------------|
| /npcutils reload                    | Reload the Mod                                                              | cobblemonnpcutils.command.reload   |
| /npcutils location create \<id>     | Creates a location preset using the provided ID at your current location    | cobblemonnpcutils.command.location |
| /npcutils location delete \<id>     | Deletes a location preset using the provided ID                             | cobblemonnpcutils.command.location |
| /npcutils location teleport \<id>   | Teleports to the provided location preset                                   | cobblemonnpcutils.command.location |
| /npcutils item create \<id>         | Creates a item preset using the provided ID and the item in your hand       | cobblemonnpcutils.command.item     |
| /npcutils item delete \<id>         | Deletes a item preset using the provided ID                                 | cobblemonnpcutils.command.item     |
| /npcutils item give \<id>           | (Debugging) Give yourself the specified item preset                         | cobblemonnpcutils.command.item     |
| /npcutils item take \<id> \[amount] | (Debugging) Take an amount of the specified item preset from your inventory | cobblemonnpcutils.command.item     |
| /npcutils item count \<id>          | (Debugging) Count the amount of the specified item preset in your inventory | cobblemonnpcutils.command.item     |

## Planned Features
- **Please submit suggestions!**

## Donations
This mod was developed as part of the Skies Development goal of **providing free, high quality, and open sourced mods** for the Cobblemon and Fabric communities! If you are able to support this mission, **please consider making a one-time donation or becoming a Member** on [Ko-fi](https://ko-fi.com/stampede2011). Being a member gives you early access to all new mods as well as helping decide on the development direction.

During this mods development, the following people supported Skies Development through an active Membership: **Jephon, Vince, Zephyr, iMystxc, Mango, GriffinFluff, Guga, AllieDragon, Frost, SPG, Ezequiel, SPG, Mashndrow, and Liopoxys.**  Thank you for your generosity! ❤️

## Support
A community support Discord has been opened up for all Skies Development related projects! Feel free to join and ask questions or leave suggestions :)

<a class="discord-widget" href="https://discord.gg/cgBww275Fg" title="Join us on Discord"><img src="https://discordapp.com/api/guilds/1158447623989116980/embed.png?style=banner2"></a>
