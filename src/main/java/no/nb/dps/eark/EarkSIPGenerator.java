package no.nb.dps.eark;


import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.cli.model.enums.WriteStrategyEnum;
import org.roda_project.commons_ip2.model.*;
import org.roda_project.commons_ip2.model.impl.eark.EARKSIP;
import org.roda_project.commons_ip2.model.impl.eark.out.writers.strategy.FolderWriteStrategy;
import org.roda_project.commons_ip2.model.impl.eark.out.writers.strategy.WriteStrategy;
import org.roda_project.commons_ip2.model.impl.eark.out.writers.strategy.ZipWriteStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


public class EarkSIPGenerator {

    private final String EARK_VERSION = "2.2.0";
    private final String SOFTWARE_VERSION = "1.0.0";
    private final String SOFTWARE_NAME = "EARK SIP Generator";


    public void createSip(Path rootPath, String description, String outputFolder, IPContentType contentType,
                          IPContentInformationType contentInformationType, IPAgent creatorAgent, IPAgent submitterAgent,
                          String submissionAgreement, String checksumAlgorithm, WriteStrategyEnum writeStrategy)
            throws IPException, InterruptedException {

        SIP sip = new EARKSIP(rootPath.getFileName().toString(), contentType, contentInformationType, EARK_VERSION);
        sip.addCreatorSoftwareAgent(SOFTWARE_NAME, SOFTWARE_VERSION);
        sip.addAgent(creatorAgent);
        sip.addAgent(submitterAgent);
        sip.setDescription(description);

        IPAltRecordID ipAltRecordID = new IPAltRecordID();
        ipAltRecordID.setType("SUBMISSIONAGREEMENT");
        ipAltRecordID.setValue(submissionAgreement);


        sip.getHeader().addAltRecordID(ipAltRecordID);

        final Path destinationPath = Paths.get(outputFolder);

        for (Path subPath : getSubDirectories(rootPath)) {
            switch (subPath.getFileName().toString()) {
                case "documentation" -> addDocumentation(sip, subPath);
                case "metadata" -> addPackageMetadata(sip, subPath);
                case "representations" -> addRepresentations(sip, subPath);
                default -> throw new IllegalStateException("Unexpected subfolder in package: " + subPath.getFileName());
            }
        }
        addSchemas(sip);
        sip.setChecksum(checksumAlgorithm);

        WriteStrategy strategy;
        switch (writeStrategy) {
            case ZIP -> {
                strategy = new ZipWriteStrategy();
                strategy.setup(destinationPath);
            }
            case FOLDER -> {
                strategy = new FolderWriteStrategy();
                strategy.setup(destinationPath);
            }
            default -> throw new IllegalArgumentException("Unexpected write strategy: " + writeStrategy);
        }

        sip.build(strategy);
    }


    private static List<String> calculateRelativePath(Path file, Path dir) {
        Path relative = dir.relativize(file.getParent());
        return Arrays.asList(relative.toString().split("/"));
    }

    private static List<Path> getAllRegularFiles(Path directoryPath) {
        if (!Files.isDirectory(directoryPath)) {
            throw new IllegalArgumentException("The provided path is not a directory.");
        }

        try (Stream<Path> pathStream = Files.walk(directoryPath)) {
            return pathStream.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error occurred when traversing directory.", e);
        }
    }

    private static List<Path> getSubDirectories(Path directoryPath) {
        if (!Files.isDirectory(directoryPath)) {
            throw new IllegalArgumentException("The provided path is not a directory.");
        }
        try (Stream<Path> pathStream = Files.list(directoryPath)) {
            return pathStream.filter(Files::isDirectory).toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error occurred when traversing directory.", e);
        }
    }


    private void addDocumentation(SIP sip, Path dir) {
        for (Path f : getAllRegularFiles(dir)) {
            // calculate relative directory path
            Path relative = dir.relativize(f.getParent());
            //splits relative path to a list of directories
            List<String> relativeFolders = Arrays.asList(relative.toString().split("/"));
            sip.addDocumentation(new IPFile(f, relativeFolders));
        }
    }


    private void addPackageMetadata(SIP sip, Path dir) throws IPException {
        for (Path subPath : getSubDirectories(dir)) {
            String directoryName = subPath.getFileName().toString();
            for (Path f : getAllRegularFiles(subPath)) {
                List<String> relativeFolders = calculateRelativePath(f, subPath);
                IPMetadata metadata;

                switch (directoryName) {
                    case "descriptive" -> {
                        if (f.getFileName().toString().equalsIgnoreCase("mods.xml")) {
                            metadata = new IPDescriptiveMetadata(new IPFile(f, relativeFolders), new MetadataType(MetadataType.MetadataTypeEnum.MODS), null);
                        } else if (f.getFileName().toString().toLowerCase().contains("mavis")) {
                            metadata = new IPDescriptiveMetadata(new IPFile(f, relativeFolders), new MetadataType(MetadataType.MetadataTypeEnum.OTHER).setOtherType("MAVIS"), null);
                        } else {
                            metadata = new IPDescriptiveMetadata(new IPFile(f, relativeFolders), new MetadataType(MetadataType.MetadataTypeEnum.OTHER), null);
                        }
                        sip.addDescriptiveMetadata((IPDescriptiveMetadata) metadata);
                    }

                    case "preservation" -> {
                        if (f.getFileName().toString().equalsIgnoreCase("premis.xml")) {
                            metadata = new IPMetadata(new IPFile(f, relativeFolders), new MetadataType(MetadataType.MetadataTypeEnum.PREMIS));
                        } else {
                            metadata = new IPMetadata(new IPFile(f, relativeFolders));
                        }
                        sip.addPreservationMetadata(metadata);
                    }

                    case "other" -> {
                        metadata = new IPMetadata(new IPFile(f, relativeFolders));
                        sip.addOtherMetadata(metadata);
                    }

                    default ->
                            throw new IllegalStateException("Unexpected value for metadata folder: " + directoryName);
                }
            }
        }
    }


    private void addRepresentations(SIP sip, Path representationsDir) throws IPException {

        for (Path repDir : getSubDirectories(representationsDir)) {
            IPRepresentation representation = new IPRepresentation(repDir.getFileName().toString());
            representation.setContentType(sip.getContentType());
            representation.setContentInformationType(sip.getContentInformationType());
            sip.addRepresentation(representation);

            for (Path subDir : getSubDirectories(repDir)) {
                switch (subDir.getFileName().toString()) {
                    case "data" -> addRepresentationData(representation, subDir);
                    case "metadata" -> addRepresentationMetadata(representation, subDir);
                    default ->
                            throw new IllegalStateException("Unexpected value for representation: " + subDir.getFileName());
                }
            }
        }
    }

    private void addRepresentationData(IPRepresentation rep, Path dir) {
        for (Path file : getAllRegularFiles(dir)) {
            // calculate relative directory path
            Path relative = dir.relativize(file.getParent());
            //splits ralative path to a list of directories
            List<String> relativeFolders = Arrays.asList(relative.toString().split("/"));
            IPFile ipFile = new IPFile(file, relativeFolders);
            rep.addFile(ipFile);
        }
    }


    private void addRepresentationMetadata(IPRepresentation rep, Path dir) {
        for (Path subPath : getSubDirectories(dir)) {
            String directoryName = subPath.getFileName().toString();
            for (Path f : getAllRegularFiles(subPath)) {
                List<String> relativeFolders = calculateRelativePath(f, subPath);
                IPMetadata metadata;

                switch (directoryName) {
                    case "source" -> {
                        metadata = new IPMetadata(new IPFile(f, relativeFolders));
                        rep.addSourceMetadata(metadata);
                    }

                    case "technical" -> {
                        if (f.getFileName().toString().toLowerCase().contains("mediainfo")) {
                            metadata = new IPMetadata(new IPFile(f, relativeFolders), new MetadataType(MetadataType.MetadataTypeEnum.OTHER).setOtherType("MEDIAINFO"));
                        } else {
                            metadata = new IPMetadata(new IPFile(f, relativeFolders));
                        }
                        rep.addTechnicalMetadata(metadata);
                    }
                    case "other" -> {
                        metadata = new IPMetadata(new IPFile(f, relativeFolders));
                        rep.addOtherMetadata(metadata);
                    }
                    default ->
                            throw new IllegalStateException("Unexpected value for metadata folder: " + directoryName);
                }
            }
        }
    }


    //TODO: find a better way to add schemas to the SIP
    private void addSchemas(SIP sip) {
        // if mods in sip, add mods schema
        sip.getDescriptiveMetadata().stream()
                .filter(metadata -> metadata.getMetadataType().getType().equals(MetadataType.MetadataTypeEnum.MODS))
                .findFirst().ifPresent(metadata -> addSchemaToSIP(sip, "/schemas/mods-v3-8.xsd", "mods-v3-8.xsd"));

        // if premis in sip, add premis schema
        sip.getPreservationMetadata().stream()
                .filter(metadata -> metadata.getMetadataType().getType().equals(MetadataType.MetadataTypeEnum.PREMIS))
                .findFirst().ifPresent(metadata -> addSchemaToSIP(sip, "/schemas/premis-v3-0.xsd", "premis-v3-0.xsd"));

        // add mediainfo schema, if there is a mediainfo file in the sip.
        sip.getRepresentations().stream()
                .filter(rep -> rep.getTechnicalMetadata().stream()
                        .anyMatch(metadata -> metadata.getMetadataType().getOtherType().equals("MEDIAINFO")))
                .findFirst().ifPresent(rep -> addSchemaToSIP(sip, "/schemas/mediainfo_2_0.xsd", "mediainfo_2_0.xsd"));

        // add mavis schema, if there is a mavis file in the sip.
        sip.getRepresentations().stream()
                .filter(rep -> rep.getSourceMetadata().stream()
                        .anyMatch(metadata -> metadata.getMetadataType().getOtherType().equals("MAVIS")))
                .findFirst().ifPresent(rep -> addSchemaToSIP(sip, "/schemas/mavis-05-03-04-06.xsd", "mavis-05-03-04-06.xsd"));

    }

    //TODO: find a better way to add schemas to the SIP
    private void addSchemaToSIP(SIP sip, String schemaPath, String schemaFileName) {
        Path pathToSchema = Paths.get("/tmp/", schemaFileName);
        if (!Files.exists(pathToSchema)) {
            try (InputStream stream = EarkSIPGenerator.class.getResourceAsStream(schemaPath)) {
                if (stream == null) {
                    throw new IllegalStateException("Schema file not found: " + schemaPath);
                }
                Files.copy(stream, pathToSchema, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // Handle any IO exceptions
                e.printStackTrace();
            }
        }
        // Add the schema to the SIP
        sip.addSchema(new IPFile(pathToSchema));
    }
}