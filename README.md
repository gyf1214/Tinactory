# Tinactory

A minecraft mod that ports gregtech to 1.18.2 and more.

## Requirements

- JDK 17
- idea IntelliJ is recommended. I have not tested on other IDEs.

## Setup

- Clone the repository.
- Import the repository into IntelliJ.
- Download the following file: <https://www.shsts.org/m2/tinactory_extra_resources_v0.zip>, put the file in
  `libs`.
- Run gradle task `genIntellijRuns`. This will generate 3 tasks: `runClient`, `runServer`, and `runData`.
- Run the task `runData` to generate data and asset files.
- Run the task `runClient` to start the testing client.
- Copy `libs/tinactory_extra_resources_v0.zip` to `run/client/resourcepacks`.
- Restart the client. Load the resource pack `Tinactory extra texture resources`.

## Code Structure

- **src/main/java**: java classes of the mod.
- **src/test/java**: java and kotlin classes for datagen and testing. Those classes will not be packaged into the final
  mod.
- **src/generated/resources**: generated resources from datagen.

### TinyCoreLib

This is a different mod providing core functionality to Minecraft.

See: <https://github.com/gyf1214/TinyCoreLib>.

### Code Style

Run gradle task `checkstyle` to check style.

An Idea IntelliJ code style setting can be downloaded here: <https://www.shsts.org/m2/codestyle.xml>.
