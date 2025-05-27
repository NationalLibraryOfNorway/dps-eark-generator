# SIP Generator

## Description
The SIP Generator is a Java application designed to generate Submission Information Packages (SIP) following the E-ARK SIP specification. This project utilizes the [commons-ip library](https://github.com/keeps/commons-ip).

### Requirements
Java 21 or higher is required to run this application. The project uses Maven for dependency management and build automation.

### Usage
To use the SIP Generator, you can run the application with the following command:

```bash
java -jar target/dps-eark-generator-1.0-SNAPSHOT-jar-with-dependencies.jar <rootPathStr> <outputFolder> <description> <summissionAgreement>
```
