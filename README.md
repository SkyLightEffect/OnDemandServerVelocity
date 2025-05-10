# OnDemandServerVelocity

OnDemandServerVelocity is a Velocity proxy plugin that automatically starts backend Minecraft servers on demand when players attempt to connect. It provides a seamless experience by launching server processes, notifying players of startup progress, and redirecting them once the server is ready.

---

## Features

* **On-Demand Server Startup**: Automatically runs a start script when a player connects to an offline server.
* **Player Notifications**: Sends customizable messages via chat or disconnect messages with countdown timers.
* **Configurable Scripts Location**: Define where start scripts are stored for each server.
* **Startup Statistics**: Records and averages start durations to estimate remaining time.
* **Robust Error Handling**: Gracefully handles startup failures and notifies players.

---

## Requirements

* Java 11+
* Velocity Proxy 3.x
* Unix-like environment for shell scripts (Linux/macOS; for Windows, adapt the start script accordingly)

---

## Installation

1. **Build the Plugin**

   ```bash
   mvn clean package
   ```

2. **Deploy**
   Copy the generated JAR file to your Velocity proxy’s `plugins/` directory.

3. **Restart Velocity**
   Start or reload your Velocity proxy to generate the default configuration files under the `OnDemandServerVelocity/` data folder.

---

## Configuration

Configuration files are located in the `OnDemandServerVelocity/` directory inside your proxy’s data folder:

* **config.yml** — Main plugin settings
* **messages.yml** — All user-facing text and messages
* **stats.yml** — Recent startup durations for each server (auto-managed)

### Example `config.yml`

```yaml
# Path where server start scripts live
start_scripts_path: '/opt/minecraft/servers'

# max startup time in milliseconds
max_startup_time: 180000
```

* `start_scripts_path`: Directory containing one subfolder per server. Each subfolder must include an executable `start.sh` script.
* `max_startup_time`: If a server takes longer than this value, it is considered failed (in ms).

---

## Setting Up Start Scripts

OnDemandServerVelocity relies on shell scripts to launch each backend server. Follow these steps:

1. **Directory Structure**
   Create a directory for each server under the `start_scripts_path` defined in `config.yml`. For example, if your path is `/opt/minecraft/servers` and you have a server named `lobby`:

   ```
   /opt/minecraft/servers/lobby/
   ```

2. **Create `start.sh`**
   Inside each server folder, place an executable `start.sh` script. Example:

   ```bash
   #!/usr/bin/env bash
   cd /home/mc/lobby
   java -Xms512M -Xmx2G -jar server.jar nogui
   ```

   Make sure to give execute permissions:

   ```bash
   chmod +x /opt/minecraft/servers/lobby/start.sh
   ```

3. **Verify Permissions**
   The Velocity process user must have execute permission on `start.sh` and read/write in the server directory.

4. **Test Manually**
   Before relying on the plugin, run the script manually to confirm:

   ```bash
   /opt/minecraft/servers/lobby/start.sh
   ```

---

## Usage

* When a player connects to an offline server, the plugin will:

  1. Deny immediate connection and send a startup message.
  2. Launch the configured `start.sh` asynchronously.
  3. Send countdown updates every second.
  4. Redirect the player to the server once it is online.

* If the startup fails or exceeds `max_startup_time`, players will receive a failure notice.

---

## Support & Contributions

Feel free to open issues or submit pull requests on the GitHub repository.

---

## License

This project is licensed under the MIT License. See `LICENSE` for details.
