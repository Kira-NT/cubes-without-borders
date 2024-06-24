# Cubes Without Borders

[![GitHub Build Status](https://img.shields.io/github/actions/workflow/status/Kir-Antipov/cubes-without-borders/build-artifacts.yml?style=flat&logo=github&cacheSeconds=3600)](https://github.com/Kir-Antipov/cubes-without-borders/actions/workflows/build-artifacts.yml)
[![Version](https://img.shields.io/github/v/release/Kir-Antipov/cubes-without-borders?sort=date&style=flat&label=version&cacheSeconds=3600)](https://github.com/Kir-Antipov/cubes-without-borders/releases/latest)
[![Modrinth](https://img.shields.io/badge/dynamic/json?color=00AF5C&label=Modrinth&query=title&url=https://api.modrinth.com/v2/project/cubes-without-borders&style=flat&cacheSeconds=3600&logo=modrinth)](https://modrinth.com/mod/cubes-without-borders)
[![CurseForge](https://img.shields.io/badge/dynamic/json?color=F16436&label=CurseForge&query=title&url=https://api.cfwidget.com/975120&cacheSeconds=3600&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/cubes-without-borders)
[![License](https://img.shields.io/github/license/Kir-Antipov/cubes-without-borders?style=flat&cacheSeconds=36000)](https://github.com/Kir-Antipov/cubes-without-borders/blob/HEAD/LICENSE.md)

<img alt="Cubes Without Borders Icon" src="https://raw.githubusercontent.com/Kir-Antipov/cubes-without-borders/HEAD/media/icon.png" width="128">

A mod that allows you to play Minecraft in a borderless fullscreen window. Whether you're using Linux, macOS, or Windows, you can have the game open on one monitor, while interacting with other applications on a different monitor, without consistently causing Minecraft to minimize.

----

## Usage

The mod doesn't introduce any additional configuration screens. Instead, it utilizes the existing `Video Settings` tab, where you would expect to find an option of this kind, and extends the pre-existing `Fullscreen` setting, adding a third option to it: `Borderless`.

So, depending on whether you have Sodium installed, you may find the option in question here:

| ![Video Setting](https://raw.githubusercontent.com/Kir-Antipov/cubes-without-borders/HEAD/media/video-settings.png) | ![Video Setting (Sodium)](https://raw.githubusercontent.com/Kir-Antipov/cubes-without-borders/HEAD/media/video-settings-sodium.png) |
| - | - |

Simply choose the new `Borderless` option, click `Apply` and/or `Done`, and you are good to go.

Additionally, the mod introduces a `--borderless` startup flag for those interested, which works the same way as the existing `--fullscreen` one, but forces the game to start as a borderless fullscreen window instead.

----

## Notes

### KDE Plasma

In case you want to open a Picture-in-Picture video on top of your Minecraft gameplay, while you painstakingly mine obsidian pillars in The End for a new mega-project, you may notice that it doesn't quite work as you would expect on KDE Plasma - Minecraft simply renders on top of the supposedly always-on-top window.

Unfortunately, there's nothing I can do on my side, since a borderless fullscreen window is still a fullscreen window, and this behavior is explicitly defined by [the FreeDesktop spec](https://specifications.freedesktop.org/wm-spec/wm-spec-1.3.html#STACKINGORDER), which was written long before it became common for people to display PiP windows on top of their fullscreen games. GNOME users don't suffer from this problem, because GNOME simply broke the specification without even attempting to start a discussion around it and change it for everyone's benefit, which is a very GNOME thing to do.

However, you can define a simple window rule to change the PiP's layer to something that renders on top of fullscreen windows, such as `OSD` or `Overlay`. With this in place, you will be able to put PiP windows on top of Minecraft, other games, and any other fullscreen apps for that matter. Here's an example for Firefox users:

<img alt="KDE Plasma - Window Rules" width="886" src="https://raw.githubusercontent.com/Kir-Antipov/cubes-without-borders/HEAD/media/kde-plasma-window-rules.png">

----

## Installation

Requirements:

 - Minecraft `1.20.x`
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

```bash
git clone https://github.com/Kir-Antipov/cubes-without-borders
cd cubes-without-borders

./gradlew build
cd build/libs
```

----

## License

Licensed under the terms of the [MIT License](https://github.com/Kir-Antipov/cubes-without-borders/blob/HEAD/LICENSE.md).
