Hypixel Guild Assistant
=======================

Hypixel Guild Assistant is a Discord bot created for the [Hypixel Network](https://hypixel.net/), primarily the custom Skyblock gamemode they have created, the bots main focuses is to help guilds on Hypixel manage their Discord server by automatically synchronizing guild ranks, allowing users to verify themselves through the bot, and get stats about themselves or other people for the Skyblock gamemode.

## Table of Content

 - [Prerequisites](#prerequisites)
 - [Installing Hypixel Guild Assistant](#installing-hypixel-guild-assistant)
 - [Configuration](#configuration)
 
### Prerequisites

 * Java >= 8
 * Gradle >= 4
 * Git

### Installing Hypixel Guild Assistant

To get started, clone down the repository using:

    git clone https://github.com/Senither/Hypixel-Skyblock-Assistant.git

Next go into the `Hypixel-Skyblock-Assistant` folder to build the project using Gradle.

    ./gradlew build

If the build ran successfully there should now be a jar file called `HypixelSkyblockAssistant.jar` in the root of the project, this will be used to run the actual bot, you can now run the bot once to generate the `config.json` file.

    java -jar HypixelSkyblockAssistant.jar

After you're finished editing the config with your personal details, run the bot again, if it starts up without any errors it should now work, and you can invite it to Discord servers.

### Configuration

#### Discord Token

The Discord token is used to login into Discords web services, allowing the bot to go online and respond to commands. If you don't already have a Discord bot application setup you can easily create one by going to [https://discordapp.com/developers/applications/me](https://discordapp.com/developers/applications/me), create a new application, give it some name, then click on "Create a Bot User", and you're good to go!

#### Hypixel Token

The Hypixel token is used to communicate with Hypixels API, allowing to bot to get player, profile, and guild information for the Skyblock gamemode. If you don't already have a Hypixel API token you can get one by logging into the server using `mc.hypixel.net` and running `/api`.

#### Database

The database properties are used to connect to the database that the bot should use, the database is required for the bot to function correctly since UUIDs, guild, player, and profile data is cached for long periods of time in the database, as-well as verification so the bot remembers which Discord account is linked with what Minecraft username.

> **Note** The MySQL user must be able to create, delete, and modify tables for the database that is used, since the bot uses a migration system to automatically roll out database changes between updates.

## Third Party Licenses

Hypixel Guild Assistant relies on the following projects:

 Name | License  |
|:---|:---|
| [Hypixel API](https://github.com/HypixelDev/PublicAPI) | [MIT](https://github.com/HypixelDev/PublicAPI/blob/master/LICENSE) |
| [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA) | [Apache License 2.0](https://github.com/DV8FromTheWorld/JDA/blob/master/LICENSE) |
| [google-gson](https://github.com/google/gson) | [Apache License 2.0](https://github.com/google/gson/blob/master/LICENSE) |
| [Guava](https://github.com/google/guava) | [Apache License 2.0](https://github.com/google/guava/blob/master/COPYING) |
| [logback-classic](https://github.com/qos-ch/logback/tree/master/logback-classic) | [Eclipse Public License v1.0](https://github.com/qos-ch/logback/blob/master/LICENSE.txt) and<br>[GNU Lesser General Public License version 2.1](https://github.com/qos-ch/logback/blob/master/LICENSE.txt) |
| [MySQL Connector](https://dev.mysql.com/doc/connector-j/8.0/en/) | [GNU General Public License Version 2](https://github.com/mysql/mysql-connector-j/blob/release/8.0/LICENSE) |

## License

Hypixel Guild Assistant is open-sourced software licensed under the [GNU General Public License v3.0](http://www.gnu.org/licenses/gpl.html).
