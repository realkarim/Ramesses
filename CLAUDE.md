# Ramsees — Claude Instructions

## Project References

- **[README.md](README.md)** — what the project does, configuration reference, how to run and backtest
- **[docs/Architecture.md](docs/Architecture.md)** — hexagonal architecture overview, package structure, dependency rules, port/adapter table, and extension guides

Read both before making implementation decisions. They are the source of truth for project intent and structure.

## After Implementing a Feature

At the end of every feature implementation, update the relevant docs:

- **README.md** — if the feature changes how to configure, run, or use the bot (new config keys, new CLI behavior, new strategies)
- **docs/Architecture.md** — if the feature adds or removes ports, adapters, domain classes, or changes the data flow
