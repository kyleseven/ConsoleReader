# ConsoleReader

![CI with Gradle](https://github.com/kyleseven/ConsoleReader/workflows/CI%20with%20Gradle/badge.svg)
[![GitHub tag](https://img.shields.io/github/tag/kyleseven/ConsoleReader.svg)](https://GitHub.com/kyleseven/ConsoleReader/tags/)

Spigot plugin that allows players to access the console in game.

## Disclaimer
This plugin is in ALPHA stage, so you may experience bugs.
Things like the config.yml, commands, and permission nodes are subject to change.

## Compatibility
ConsoleReader is built on Spigot API version 1.18.

It is compatible with versions 1.12 to 1.18.

## Building

ConsoleReader uses Gradle to handle dependencies and building.

### Compiling from source

    git clone https://github.com/kyleseven/ConsoleReader.git
    cd ConsoleReader/
    ./gradlew clean build
    
The jars can be found in the `build/libs` directory.

## Installation

Place the `ConsoleReader.jar` file into your `plugins/` directory and start the server.

## Configuration

- `config.yml`
    - `prefix`: The prefix that most plugin messages will have.
    - `log_color`: The color of the console in the chat. Supports the colors listed [here](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/ChatColor.html).
    - `forbidden_commands`: A list of commands that will be blocked if used with `/cr exec`
    - `filters`: A list of regular expressions that if matched with a log message, the message will not be shown.

## Usage

- `/cr help` displays a list of commands.
- `/cr read [player]` starts/stop showing the console in chat for yourself or a specified player.
- `/cr execute <command>` executes a command as console.
    - `/cexec <command>` alternatively.
- `/cr list` shows a list of players currently monitoring the console.
- `/cr reload` reloads the plugin configuration.
- `/cr version` shows the plugin version.

## Permissions

- `consolereader.read` allows access to `read`, `list`, `help`, and `version` subcommands.
- `consolereader.read.others` allows for toggling console reading for other players.
- `consolereader.execute` allows access to the `execute` subcommand.
    - WARNING! Only give this permission to people you trust 100%!
- `consolereader.reload` allows access to the `reload` subcommand.
