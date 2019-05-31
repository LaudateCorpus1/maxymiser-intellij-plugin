# Oracle Maxymiser plugin for IntelliJ IDEA platform

Enables editing of Variants, Campaign Scripts and Campaign Actions from within IntelliJ IDEA or WebStorm. 

## Clone

```
git clone https://github.com/oracle/maxymiser-intellij-plugin
cd maxymiser-intellij-plugin
```

## Building

We use gradle to build the plugin. It comes with a wrapper script (`gradlew` or `gradlew.bat` in
the root of the repository) which downloads appropriate version of gradle
automatically as long as you have JDK installed.

Common tasks are

  - `./gradlew buildPlugin` — fully build plugin and create an archive at
    `build/distributions` which can be installed into IntelliJ IDEA via `Install
    plugin from disk` action found in **File | Settings | Plugins**.

  - `./gradlew runIde` — run a development IDE with the plugin installed.

  - `./gradlew test` — run all tests.
  
## Installation
Launch IntelliJ IDEA or WebStorm.

Open **File | Settings | Plugins**.

Click **Install Plugin from Disk...** and select ZIP archive from `build/distributions`

Restart IDE if asked.

## Configuration
Open **File | Settings | Oracle Maxymiser** and set the following configuration parameters

| Parameter     | Description   |
| ------------- | ------------- |
| Region        | **US** or **EMEA**  |
| Client ID     | Client ID, can be obtained at **Admin \| Rest API** in Maxymiser UI  |
| Client Secret | Client Secret, can be obtained at **Admin \| Rest API** in Maxymiser UI   |
| Login         | Your Maxymiser UI Login  |
| Password      | Your Maxymiser UI Password  |

## Usage
Plugin adds 2 actions that are available from file's context menu in **Project View**:

* **Pull** - Pulls selected campaign from Maxymiser UI for editing.
* **Push** - Pushes all changes to Maxymiser.