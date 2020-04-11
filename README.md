# ConsoleReader
Spigot plugin that allows players to access the console in game.

## Disclaimer
This plugin is in ALPHA stage, and you most likely will experience bugs.
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

## Usage

- `/cr help` displays a list of commands.
- `/cr monitor` starts/stop showing the console in chat.
- `/cr execute <command>` executes a command as console.
    - `/cexec <command>` alternatively.
- `/cr version` shows the plugin version.

## Permissions

- `consolereader.use` allows access to `help` and `version` subcommands.
- `consolereader.read` allows access to `enable` and `disable` subcommands.
- `consolereader.execute` allows access to the `execute` subcommand.
    - WARNING! Only give this permission to people you trust 100%!
- `consolereader.reload` allows access to the `reload` subcommand.