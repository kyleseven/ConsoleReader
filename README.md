# ConsoleReader

![CI with Maven](https://github.com/kyleseven/ConsoleReader/workflows/CI%20with%20Maven/badge.svg)
[![GitHub tag](https://img.shields.io/github/tag/kyleseven/ConsoleReader.svg)](https://GitHub.com/kyleseven/ConsoleReader/tags/)
[![SpigotMC](https://pluginbadges.glitch.me/api/v1/dl/View%20ConsoleReader%20on%20SpigotMC-limegreen.svg?spigot=consolereader.78041&style=flat)](https://www.spigotmc.org/resources/consolereader.78041/)

Spigot plugin that allows players to access the console in game.

## Disclaimer
This plugin is in ALPHA stage, so you may experience bugs.
Things like the config.yml, commands, and permission nodes are subject to change.

## Compatibility
ConsoleReader is built on Spigot API version 1.15.

It is compatible with 1.12, 1.13, 1.14, and 1.15.

## Building

ConsoleReader uses Maven to handle dependencies and building.

### Compiling from source

    git clone https://github.com/kyleseven/ConsoleReader.git
    cd ConsoleReader/
    mvn clean package
    
The jars can be found in the `target` directory.

## Installation

Place the `ConsoleReader.jar` file into your `plugins/` directory and start the server.

## Configuration

- `config.yml`
    - `prefix`: The prefix that most plugin messages will have.
    - `log_color`: The color of the console in the chat. Supports the colors listed [here](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/ChatColor.html).
    - `show_chat`: Whether to show chat in /cr read.
    - `show_own_commands`: Whether you want to see your own commands in /cr read.
    - `forbidden_commands`: A list of commands that will be blocked if used with `/cr exec`

## Usage

- `/cr help` displays a list of commands.
- `/cr read [player]` starts/stop showing the console in chat for yourself or a specified player.
- `/cr execute <command>` executes a command as console.
    - `/cexec <command>` alternatively.
- `/cr version` shows the plugin version.

## Permissions

- `consolereader.read` allows access to `read`, `help`, and `version` subcommands.
- `consolereader.execute` allows access to the `execute` subcommand.
    - WARNING! Only give this permission to people you trust 100%!
- `consolereader.reload` allows access to the `reload` subcommand.
