# ConsoleReader
Spigot plugin that allows players to access the console in game. [View on SpigotMC](https://www.spigotmc.org/resources/consolereader.78041/)

## Disclaimer
This plugin is in ALPHA stage, so you may experience bugs.
Things like the config.yml, commands, and permission nodes are subject to change.

## Building

ConsoleReader uses Maven to handle dependencies and building.

### Compiling from source

    git clone https://github.com/kyleseven/ConsoleReader.git
    cd ConsoleReader/
    mvn install
    
The jars can be found in the `target` directory.

## Installation

Place the `ConsoleReader.jar` file into your `plugins/` directory and start the server.

## Configuration

- `config.yml`
    - `prefix`: The prefix that most plugin messages will have.
    - `log_color`: The color of the console in the chat. Accepts a Minecraft color code 0-9 or a-f.
    - `forbidden_commands`: A list of commands that will be blocked if used with `/cr exec`

## Usage

- `/cr help` displays a list of commands.
- `/cr read` starts/stop showing the console in chat.
- `/cr execute <command>` executes a command as console.
    - `/cexec <command>` alternatively.
- `/cr version` shows the plugin version.

## Permissions

- `consolereader.read` allows access to `read`, `help`, and `version` subcommands.
- `consolereader.execute` allows access to the `execute` subcommand.
    - WARNING! Only give this permission to people you trust 100%!
- `consolereader.reload` allows access to the `reload` subcommand.