# Cubes Without Borders

[![GitHub Build Status](https://img.shields.io/github/actions/workflow/status/Kir-Antipov/cubes-without-borders/build-artifacts.yml?style=flat&logo=github&cacheSeconds=3600)](https://github.com/Kir-Antipov/cubes-without-borders/actions/workflows/build-artifacts.yml)
[![Version](https://img.shields.io/github/v/release/Kir-Antipov/cubes-without-borders?sort=date&style=flat&label=version&cacheSeconds=3600)](https://github.com/Kir-Antipov/cubes-without-borders/releases/latest)
[![Modrinth](https://img.shields.io/badge/dynamic/json?color=00AF5C&label=Modrinth&query=title&url=https://api.modrinth.com/v2/project/cubes-without-borders&style=flat&cacheSeconds=3600&logo=modrinth)](https://modrinth.com/mod/cubes-without-borders)
[![CurseForge](https://img.shields.io/badge/dynamic/json?color=F16436&label=CurseForge&query=title&url=https://api.cfwidget.com/975120&cacheSeconds=3600&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/cubes-without-borders)
[![License](https://img.shields.io/github/license/Kir-Antipov/cubes-without-borders?style=flat&cacheSeconds=36000)](https://github.com/Kir-Antipov/cubes-without-borders/blob/HEAD/LICENSE.md)

A mod that allows you to play Minecraft in a borderless fullscreen window. This way, you can have the game open on one monitor, while interacting with other applications on a different monitor, without consistently causing Minecraft to minimize.

----

## Installation

Requirements:

 - Minecraft `1.19.x`
 - Fabric Loader `>=0.15.0`

You can download the mod from:

 - [GitHub Releases](https://github.com/Kir-Antipov/cubes-without-borders/releases/latest)
 - [Modrinth](https://modrinth.com/mod/cubes-without-borders)
 - [CurseForge](https://www.curseforge.com/minecraft/mc-mods/cubes-without-borders)
 - [GitHub Actions](https://github.com/Kir-Antipov/cubes-without-borders/actions/workflows/build-artifacts.yml) *(these builds may be unstable, but they represent the actual state of the development)*

----

## Build

Requirements:

 - JDK `17`

### Linux/MacOS

```cmd
git clone https://github.com/Kir-Antipov/cubes-without-borders.git
cd cubes-without-borders

chmod +x ./gradlew
./gradlew build
cd build/libs
```

### Windows

```cmd
git clone https://github.com/Kir-Antipov/cubes-without-borders.git
cd cubes-without-borders

gradlew build
cd build/libs
```

----

## License

Licensed under the terms of the [MIT License](https://github.com/Kir-Antipov/cubes-without-borders/blob/HEAD/LICENSE.md).
