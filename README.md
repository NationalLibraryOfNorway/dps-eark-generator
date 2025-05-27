# SIP Generator

## Description
The SIP Generator is a Java application designed to generate Submission Information Packages (SIP) following the E-ARK SIP specification. This project utilizes the [commons-ip library](https://github.com/keeps/commons-ip), a Java library that provides core functionalities for creating and manipulating Information Packages, simplifying the development of E-ARK compliant tools.

### Requirements
Java 21 or higher is required to run this application. The project uses Maven for dependency management and build automation.

### Building from Source
To build the SIP Generator from source, ensure you have Maven installed and then run the following command from the project's root directory:

```bash
mvn clean package
```
This will compile the code and create the executable JAR file in the `target` directory.

### Usage
To use the SIP Generator, you can run the application with the following command:

```bash
java -jar target/dps-eark-generator-1.0-SNAPSHOT-jar-with-dependencies.jar <rootPathStr> <outputFolder> <description> <summissionAgreement>
```

For example:
```bash
java -jar target/dps-eark-generator-1.0-SNAPSHOT-jar-with-dependencies.jar "/path/to/your/data" "/path/to/output/sips" "Moose at sunset, Nordland" "ABCD1234"
```

### Input File Structure

The SIP Generator expects a specific directory structure for the input data provided via the `<rootPathStr>` argument. The root directory should be organized as follows:

```
<rootPathStr>/
├── documentation/
│   └── any_documentation_file.pdf
├── metadata/
│   ├── descriptive/
│   │   ├── mods.xml
│   │   └── another_descriptive_metadata.xml
│   ├── preservation/
│   │   └── premis.xml
│   └── other/
│       └── other_package_metadata.xml
└── representations/
    ├── primary_20250325/
    │   ├── data/
    │   │   └── image1.png
    │   └── metadata/
    │       ├── source/
    │       │   └── source_metadata_for_rep1.xml
    │       ├── technical/
    │       │   └── Mediainfo/
    │       │       └── MEDIAINFO_image1.jpg.xml
    │       └── other/
    │           └── other_metadata_for_rep1.xml
    └── access_20250325/
        ├── data/
        │   └── video.mp4
        └── metadata/
            └── technical/
                └── Mediainfo/
                    └── MEDIAINFO_video.mp4.xml            
```

**Explanation of Directories:**

*   **`documentation/`**: Contains any general documentation files for the SIP. Subdirectories are allowed and their structure will be preserved.
*   **`metadata/`**: Holds metadata files that apply to the entire package.
    *   `descriptive/`: For descriptive metadata.
        *   `mods.xml`: If present, treated as MODS metadata.
        *   Files with "mavis" in the name are treated as MAVIS metadata.
    *   `preservation/`: For preservation metadata.
        *   `premis.xml`: If present, treated as PREMIS metadata.
    *   `other/`: For any other package-level metadata files.
*   **`representations/`**: This directory contains one or more subdirectories, where each subdirectory represents a distinct version or "representation" of the archived content (e.g., a master version, an access copy).
    *   **`<representation_name>/`** (e.g., `representation_1`, `master_copy`):
        *   `data/`: Contains the actual digital files for this specific representation. Subdirectories are allowed and their structure will be preserved.
        *   `metadata/`: Contains metadata files specific to this representation.
            *   `source/`: For source metadata (e.g., information about the origin of the files).
            *   `technical/`: For technical metadata. Files with "mediainfo" in their name are specifically recognized as MediaInfo reports.
            *   `other/`: For any other metadata files specific to this representation.

The generator will automatically include necessary XML schemas (MODS, PREMIS, MediaInfo, MAVIS) in the generated SIP if it detects corresponding metadata files within this structure.


### SIP Specifications ###

SIP Specifications for the DPS are described here:
[https://digitalpreservation.no/nb/docs/dps/sip/](https://digitalpreservation.no/nb/docs/dps/sip/)