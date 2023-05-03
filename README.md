![Maven Central](https://img.shields.io/maven-central/v/com.fathzer/jdbbackup-dropbox)
![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jdbbackup_jdbbackup-dropbox&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=jdbbackup_jdbbackup-dropbox)
[![javadoc](https://javadoc.io/badge2/com.fathzer/jdbbackup-dropbox/javadoc.svg)](https://javadoc.io/doc/com.fathzer/jdbbackup-dropbox)

# jdbbackup-dropbox
A [jdbbackup](https://github.com/jdbbackup/jdbbackup-core) DestinationManager implementation that uses Dropbox as underlying storage provider.

It requires java 11+.

## Destination format
dropbox://*token*/*filePath*

All the patterns detailed [here](https://github.com/jdbbackup/jdbbackup-core) are supported.

## How to get a Dropbox Token
This library jar contains a main class that will provide you with a token.

Here is how to launch it:  
```
java -jar jdbbackup-dropbox-1.0.0.jar
```

Use the -h argument to know available command arguments.