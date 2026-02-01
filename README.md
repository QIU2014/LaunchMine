# LaunchMine - Minecraft Launcher

[![Maven Build](https://github.com/QIU2014/LaunchMine/actions/workflows/maven.yml/badge.svg)](https://github.com/QIU2014/LaunchMine/actions/workflows/maven.yml)

## Overview
LaunchMine is a modern, feature-rich Minecraft launcher built with Java Swing that provides a clean and intuitive interface for managing and launching Minecraft instances.

## Features
### 🎮 Minecraft Management
- **Multiple Instance** Support: Create and manage multiple Minecraft versions
- **Version Browser**: Browse and download official Minecraft versions (release, snapshot, beta, alpha)
- **Automatic Downloads**: Automatically downloads required game files and libraries
- **Asset Management**: Downloads and validates game assets (textures, sounds, etc.)
### 🖥️ User Interface
- **Modern Design**
- **Cross-Platform**: Native look and feel on Windows, macOS, and Linux
- **Instance List**: Visual list with status indicators (ready, missing files)
- **Version Type Icons**: Different shapes/colors for release/snapshot/beta/alpha versions
### ⚙️ Configuration
- **Launch Options**: Customizable memory allocation, window size, player name
- **Java Settings**: Specify custom Java installation path
- **Preferences**: Auto-update checks, default settings
### 🔧 Technical Features
- Progress Tracking: Real-time download progress with file validation
- Log Window: Integrated Minecraft log viewer with copy/clear functions
- File Validation: SHA1 checksum verification for downloaded files
- Background Updates: Automatic version manifest updates
- Memory Monitoring: System memory usage monitoring
## Project Structure
```text
com.eric/
├── Main.java                    # Main application class and window
├── UI.java                      # User interface components
├── Instances.java               # Instance management dialog
├── ui/
│   ├── AboutDialog.java         # About dialog with license info
│   ├── DownloadDialog.java      # Download progress dialog
│   ├── MinecraftLogWindow.java  # Minecraft output log window
│   ├── OptionsDialog.java       # Preferences dialog
│   └── LanguageDialog.java      # Language selection dialog
└── utils/
    ├── InstanceUtils.java       # Minecraft version management
    ├── AssetsUtils.java         # Asset and library downloading
    ├── JsonUtils.java          # JSON parsing utilities
    ├── LaunchUtils.java        # Minecraft launching logic
    ├── NetUtils.java           # HTTP download utilities
    ├── PreferencesUtils.java   # Preference handling
    ├── MacOSAppListener.java   # macOS-specific integrations
    └── I18nUtils.java          # Internationalization utilities
```
## Key Components
1. **Main Application (Main.java)**
    - Application entry point and main window
    - macOS integration with system menu bar
    - Instance management and scanning
    - Memory monitoring and cleanup
2. **User Interface (UI.java)**
    - Modern, styled Swing components
    - Color-coded buttons and indicators
    - Instance list with visual feedback
    - Menu bar with platform-specific implementations
3. **Instance Management (InstanceUtils.java)**
    - Downloads and caches version manifests from Mojang
    - Version filtering by type (release, snapshot, etc.)
    - Version metadata processing
4. **Launch System**
    - Builds launch commands from version JSON
    - Validates and downloads required files
    - Handles JVM arguments and classpath construction
    - Process management with log output
5. **Download System (NetUtils.java)**
    - HTTP download with progress callbacks
    - File validation with SHA1 checks
    - Background downloading with UI feedback
## Installation
### Prerequisites
- Java 21 or later
- Internet connection (for downloading Minecraft files)
### Running the Application
1. Clone the repository
2. Compile:
```bash
mvn clean package
```
3. Run the application:
```bash
java -jar target/launchmine*.jar
```
### Dependencies
- Jackson JSON (databind, core, annotations) for JSON processing
## Usage
### First Launch
1. The application will automatically download the Minecraft version manifest
2. Navigate to File → Instances to browse available versions
3. Select a version and click "Select Version" to download
4. Once downloaded, the version appears in the main instance list
### Launching Minecraft
1. Select an instance from the main list
2. Click the Start button
3. Enter your player name and adjust launch options if needed
4. Click OK to launch
### Managing Instances
- Create New: File → Instances, then select a version
- Edit: View instance details and status
- Open Folder: Access instance files in system explorer
- Delete: Remove instances from the list
### Preferences
- Access via Tools → Options (or Cmd+, on macOS)
- Set default memory allocation
- Configure window size
- Set Java installation path
- Enable/disable auto-update checks
## Design Patterns
### MVC Architecture
- Model: `InstanceInfo`, version data, preferences
- View: `UI.java`, dialog classes
- Controller: `Main.java`, utility classes
### Factory Pattern
- Dialog creation methods
- Button and component styling
### Observer Pattern
- Download progress callbacks
- List selection listeners
### Singleton Pattern
- Main application instance
- Utility class instances
## Platform-Specific Features
### macOS
- System menu bar integration
- Native About and Preferences menu items
- Keyboard shortcuts (Cmd+, , Cmd+Q, etc.)
- Dock icon and application menu
### Windows/Linux
- Standard menu bar
- Custom-styled dialogs
- Cross-platform UI components
## Error Handling
- File Validation: SHA1 checksum verification
- Network Errors: Graceful retry and user notification
- Missing Files: Clear error messages with recovery options
- Memory Management: Automatic cleanup on low memory
## Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request
## License
This project is licensed under the MIT License. See the [LICENSE](https://github.com/QIU2014/LaunchMine) file for details.
## Credits
- Author: qiuerichanru
- UI Design: Modern, responsive Swing interface
- Minecraft Integration: Based on official Mojang launcher specifications
## Support
For issues, questions, or feature requests:
1. Check existing issues on GitHub
2. Create a new issue with detailed information
3. Include system information and error logs
## Future Enhancements
- Mod support (Fabric, Forge, Quilt)
- Resource pack management
- Skin customization
- Server joining improvements
- Offline mode enhancements
- Cloud saves integration

**Enjoy playing Minecraft with LaunchMine! 🎮**