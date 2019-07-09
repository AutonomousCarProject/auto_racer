Master: [![Build Status](https://travis-ci.com/AutonomousCarProject/auto_racer.svg?branch=master)](https://travis-ci.com/AutonomousCarProject/auto_racer)

### How to set up Run Configs (Intellij IDEA)
* Add new Gradle config
    * Target the auto_racer gradle project
    * Paste `clean build` into tasks (if `build` doesnt work for you try `jar`)
* Java Application config
    * Main class: `autoracer.yourclient.YourMainClass`
    * Classpath of module: `org.avphs.yourclient.main`
    * Under VM options, paste this line:
        * `--module-path mods -m org.avphs.yourclient/org.avphs.yourclient.YourMainClass`
    * Included clients are `carclient` and `traksim`