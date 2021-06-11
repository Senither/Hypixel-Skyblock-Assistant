Hypixel Skyblock Assistant
=======================

Hypixel Skyblock Assistant is a Discord bot created for the [Hypixel Network](https://hypixel.net/), primarily the custom Skyblock gamemode they have created, the bots main focuses is to help guilds on Hypixel manage their Discord server by automatically synchronizing guild ranks, allowing users to verify themselves through the bot, and get stats about themselves or other people for the Skyblock gamemode.

## Features

 * Verification system to verify their IGNs.
 * Hypixel rank assignment(Auto giving MVP++, VIP, etc)
 * Commands to track and see stats for skills, slayers, bank, auction house, talismans, & pets.
 * Guild and Player leaderboards(Includes individual leaderboards for skills and slayer types).
 * Calculators for skills and pet experience.
 * Guild linking with the bot, which provides features like:
 	* Guild XP Leaderbords.
 	* Automatic guild role assignments.
 	* Automatic renaming users to their IGNs.
 	* Defining guild rank requirements through the bot.
 	* Checking what rank a user should have based on the requirements that have been setup.
 	* Scanning the entire guild to calculate guild averages, and to see who meets what rank requirements.
 	* Donation & Splash trackers to see who contribute the most to the guild.
 	* Broadcasting system to announce messages through the bot.
 	* Settings command to configure and customize all the features above.

> **Note:** If you're not looking for a Discord bot but still want a weight calculator you can checkout [LappySheep/hypixel-skyblock-weight](https://github.com/LappySheep/hypixel-skyblock-weight) for a different take on the weight calculation.

<hr>

## Table of Content

 - [Prerequisites](#prerequisites)
 - [Installing Hypixel Skyblock Assistant](#installing-hypixel-skyblock-assistant)
 - [Installing Guild Report Web UI](#installing-guild-report-web-ui)
 - [Using a custom Guild Leaderboard](#using-a-custom-guild-leaderboard)
 - [Configuration](#configuration)
 - [Third Party Licenses](#third-party-licenses)
 - [License](#license)

### Prerequisites

##### App (Bot)

 * Java >= 8
 * Gradle >= 4
 * MySQL/MariaDB Server
 * Git

#### Web UI

 * NodeJS >= 11
 * Yarn >= 1.15
 * Git

### Installing Hypixel Skyblock Assistant

To get started, clone down the repository using:

    git clone https://github.com/Senither/Hypixel-Skyblock-Assistant.git

Next go into the `Hypixel-Skyblock-Assistant/app` folder to build the project using Gradle.

    ./gradlew build

If the build ran successfully there should now be a jar file called `HypixelSkyblockAssistant.jar` inside the `app` directory of the project, this will be used to run the actual bot, you can now run the bot once to generate the `config.json` file.

    java -jar HypixelSkyblockAssistant.jar

After you're finished editing the config with your personal details, run the bot again, if it starts up without any errors it should now work, and you can invite it to Discord servers.

### Installing Guild Report Web UI

To get started, clone down the repository using:

    git clone https://github.com/Senither/Hypixel-Skyblock-Assistant.git

Next go into the `Hypixel-Skyblock-Assistant/web` folder and install all the dependencies using Yarn.

    yarn

While the dependencies are being installed go to the config file to setup the app URL, the config can be found at `web/assets/js/config.js` , once that's done and the installation have finished, you can now build the project.

    yarn prod

Running the app with `prod` will build all the assets for a production environment, you can also use `dev` for a development environment, or `watch` for setting up a watcher that will re-build the project anytime it sees changes being made to the files.

### Using a custom Guild Leaderboard

The guild and player leaderboards are created by the [Hypixel Skyblock Leaderboard](https://github.com/Senither/Hypixel-Skyblock-Leaderboard) project, which provides an API for the bot to pull the leaderboard data from, and a way to create and manage the leaderboards while not really affecting the bot itself.

If you're interested in creating your own leaderboards instead of using the [public leaderboard API](http://hypixel-app-api.senither.com/leaderboard), you can follow the instructions on the [Hypixel Skyblock Leaderboard](https://github.com/Senither/Hypixel-Skyblock-Leaderboard) repository to setup the API with a custom list of guilds to track.

### Configuration

#### Discord Token

The Discord token is used to login into Discords web services, allowing the bot to go online and respond to commands. If you don't already have a Discord bot application setup you can easily create one by going to [https://discordapp.com/developers/applications/me](https://discordapp.com/developers/applications/me), create a new application, give it some name, then click on "Create a Bot User", and you're good to go!

#### Hypixel Token

The Hypixel token is used to communicate with Hypixels API, allowing to bot to get player, profile, and guild information for the Skyblock gamemode. If you don't already have a Hypixel API token you can get one by logging into the server using `mc.hypixel.net` and running `/api` .

#### Leaderboard URI

The leaderboard URI is the URI that the [leaderboard tracker](https://github.com/Senither/Hypixel-Skyblock-Leaderboard) is hosted at, when a custom URI is provided the bot will instead use that API for all guild and player leaderboards, however if the value is left at `null` , the bot will default back to using the public leaderboard API.

#### Database

The database properties are used to connect to the database that the bot should use, the database is required for the bot to function correctly since UUIDs, guild, player, and profile data is cached for long periods of time in the database, as-well as verification so the bot remembers which Discord account is linked with what Minecraft username.

> **Note** The MySQL user must be able to create, delete, and modify tables for the database that is used, since the bot uses a migration system to automatically roll out database changes between updates.

#### Servlet

The web servlet sets up a JSON API that runs within the bot itself, the API is used by the `web` portion of the bot, allowing people to view guild scan reports in an easy to read and understand way.
The `app_url` property is the link to where the `web UI` is hosted, so linked for guild scan reports can be generated correctly.

> **Note:** Make sure the port the web servlet is running on is open and accessible by the web UI.

## Third Party Licenses

Hypixel Skyblock Assistant relies on the following projects:

 Name | License  |
|:---|:---|
| [Hypixel API](https://github.com/HypixelDev/PublicAPI) | [MIT](https://github.com/HypixelDev/PublicAPI/blob/master/LICENSE) |
| [OpenNBT](https://github.com/Steveice10/OpenNBT) | [MIT](https://github.com/Steveice10/OpenNBT/blob/master/LICENSE.txt) |
| [Vue](https://github.com/vuejs/vue) | [MIT](https://github.com/vuejs/vue/blob/dev/LICENSE) |
| [Laravel Mix](https://github.com/JeffreyWay/laravel-mix) | [MIT](https://github.com/JeffreyWay/laravel-mix/blob/master/LICENSE) |
| [Moment](https://github.com/moment/moment) | [MIT](https://github.com/moment/moment/blob/develop/LICENSE) |
| [Axios](https://github.com/axios/axios) | [MIT](https://github.com/axios/axios/blob/master/LICENSE) |
| [Bulma](https://github.com/jgthms/bulma) | [MIT](https://github.com/jgthms/bulma/blob/master/LICENSE) |
| [Bulmaswatch](https://github.com/jenil/bulmaswatch) | [MIT](https://github.com/jenil/bulmaswatch/blob/gh-pages/LICENSE) |
| [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA) | [Apache License 2.0](https://github.com/DV8FromTheWorld/JDA/blob/master/LICENSE) |
| [google-gson](https://github.com/google/gson) | [Apache License 2.0](https://github.com/google/gson/blob/master/LICENSE) |
| [Guava](https://github.com/google/guava) | [Apache License 2.0](https://github.com/google/guava/blob/master/COPYING) |
| [Spark](https://github.com/perwendel/spark) | [Apache License 2.0](https://github.com/perwendel/spark/blob/master/LICENSE) |
| [logback-classic](https://github.com/qos-ch/logback/tree/master/logback-classic) | [Eclipse Public License v1.0](https://github.com/qos-ch/logback/blob/master/LICENSE.txt) and<br>[GNU Lesser General Public License version 2.1](https://github.com/qos-ch/logback/blob/master/LICENSE.txt) |
| [MySQL Connector](https://dev.mysql.com/doc/connector-j/8.0/en/) | [GNU General Public License Version 2](https://github.com/mysql/mysql-connector-j/blob/release/8.0/LICENSE) |

The project uses code from [avaire/avaire](https://github.com/avaire/avaire), primarily the chat functionality built for [JDA](https://github.com/DV8FromTheWorld/JDA), making messaging easier.

## License

Hypixel Skyblock Assistant is open-sourced software licensed under the [GNU General Public License v3.0](http://www.gnu.org/licenses/gpl.html).
