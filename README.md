# Tinactory

A minecraft mod that ports gregtech to 1.18.2 and more.

## Requirements

- JDK 17

## Setup

- Clone the repository.
- Import the repository into IntelliJ.
- Download the following file: <https://www.shsts.org/m2/extra/tinactory_extra_resources_v3.zip>, put the file in
  `libs`.
- Run the Gradle task `runData` to generate data and asset files.
- Run the Gradle task `runClient` to start the testing client.
- Copy `libs/tinactory_extra_resources_v3.zip` to `run/client/resourcepacks`.
- Restart the client. Load the resource pack `Tinactory extra texture resources`.

## Code Structure

- **mod**: Main java classes and resources of the mod, including game tests
- **datagen**: java and kotlin classes fo data generator. Those classes will not be packaged into the final
  mod.

### TinyCoreLib

This is a different mod providing core functionality to Minecraft.

See: <https://github.com/gyf1214/TinyCoreLib>.

### Code Style

Run gradle task `checkstyle` to check style.

An Idea IntelliJ code style setting can be downloaded here: <https://www.shsts.org/m2/codestyle.xml>.
