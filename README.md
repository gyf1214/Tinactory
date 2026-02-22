# Tinactory

A minecraft mod that ports gregtech to 1.18.2 and more.

## Requirements

- JDK 21
- idea IntelliJ is recommended. I have not tested on other IDEs.

## Setup

- Clone the repository.
- Import the repository into IntelliJ.
- Download the following file: <https://www.shsts.moe/m2/extra/tinactory_extra_resources_v1.zip>, put the file in
  `libs`.
- Run gradle task `genIntellijRuns`. This will generate 3 tasks: `runClient`, `runServer`, and `runData`.
- Run the task `runData` to generate data and asset files.
- Run the task `runClient` to start the testing client.
- Copy `libs/tinactory_extra_resources_v1.zip` to `run/client/resourcepacks`.
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

An Idea IntelliJ code style setting can be downloaded here: <https://www.shsts.moe/m2/codestyle.xml>.
