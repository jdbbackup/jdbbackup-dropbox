![Maven Central](https://img.shields.io/maven-central/v/com.fathzer/jdbbackupp-dropbox)
![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jdbbackup_jdbbackupp-dropbox&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=jdbbackupp_jdbbackupp-dropbox)
[![javadoc](https://javadoc.io/badge2/com.fathzer/jdbbackupp-dropbox/javadoc.svg)](https://javadoc.io/doc/com.fathzer/jdbbackupp-dropbox)

# jdbbackup-dropbox
A [jdbbackupp](https://github.com/jdbbackupp) DestinationManager implementation that uses Dropbox as underlying storage provider.

It requires java 11+.

## How to get a Dropbox Token
This library jar contains a main class that will provide you with a token. It requires the [jdbbackup-cli](https://github.com/jdbbackup/jdbbackup-cli) jar to run.

Here is how to launch it (on windows, replace : by ;):  
```
java -cp path/jdbbackup-dropbox-1.0.0.jar:path/jdbbackup-cli-1.0.0.jar com.fathzer.jdbbackup.managers.dropbox.DropboxTokenCmd
```

Use the -h argument to know available command arguments.