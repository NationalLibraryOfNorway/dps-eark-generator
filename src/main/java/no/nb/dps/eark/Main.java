package no.nb.dps.eark;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip.utils.METSEnums;
import org.roda_project.commons_ip2.cli.model.enums.WriteStrategyEnum;
import org.roda_project.commons_ip2.model.IPAgent;
import org.roda_project.commons_ip2.model.IPAgentNoteTypeEnum;
import org.roda_project.commons_ip2.model.IPConstants;
import org.roda_project.commons_ip2.model.IPContentInformationType;
import org.roda_project.commons_ip2.model.IPContentType;

public class Main {
    public static void main(String[] args) {

        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        String action = args[0];
        if ("generate".equalsIgnoreCase(action)) {
            if (args.length < 5) { // action + 4 args for generate
                System.err.println("Error: Missing arguments for 'generate' action.");
                printUsage();
                System.exit(1);
            }
            String rootPathStr = args[1];
            String outputFolder = args[2];
            String description = args[3];
            String submissionAgreement = args[4];

            generateSip(rootPathStr, outputFolder, description, submissionAgreement);

        } else if ("validate".equalsIgnoreCase(action)) {
            if (args.length < 3) { // action + 2 args for validate
                System.err.println("Error: Missing arguments for 'validate' action.");
                printUsage();
                System.exit(1);
            }
            String sipLocationStr = args[1];
            String reportPathStr = args[2];

            validateSip(sipLocationStr, reportPathStr);

        } else {
            System.err.println("Error: Unknown action '" + action + "'.");
            printUsage();
            System.exit(1);
        }
    }

    private static void generateSip(String rootPathStr, String outputFolder, String description, String submissionAgreement) {
        IPContentType contentType = new IPContentType(IPContentType.IPContentTypeEnum.PHOTOGRAPHS_DIGITAL);
        IPContentInformationType contentInformationType = new IPContentInformationType(IPContentInformationType.IPContentInformationTypeEnum.OTHER);
        contentInformationType.setOtherType("https://digitalpreservation.no/nb/docs/dps/sip/1.0/profiles/images/");

        IPAgent creatorAgent = new IPAgent("Stiftelsen Helgeland Museum", "CREATOR", null, METSEnums.CreatorType.ORGANIZATION, null, "Organisasjonsnummer:987654321", IPAgentNoteTypeEnum.IDENTIFICATIONCODE);
        IPAgent submitterAgent = new IPAgent("KulturIT AS", "OTHER", "SUBMITTER", METSEnums.CreatorType.ORGANIZATION, null, "Organisasjonsnummer:123456789", IPAgentNoteTypeEnum.IDENTIFICATIONCODE);
        String checksumAlgorithm = IPConstants.CHECKSUM_MD5_ALGORITHM;
        WriteStrategyEnum writeStrategy = WriteStrategyEnum.FOLDER;

        EarkSIPGenerator generator = new EarkSIPGenerator();
        try {
            generator.createSip(Paths.get(rootPathStr), description, outputFolder, contentType, contentInformationType, creatorAgent, submitterAgent, submissionAgreement, checksumAlgorithm, writeStrategy);
            System.out.println("SIP created successfully at: " + outputFolder);
        } catch (IPException e) {
            System.err.println("Error creating SIP: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.err.println("Process was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new RuntimeException(e);
        }
    }

    private static void validateSip(String sipLocationStr, String reportPathStr) {
        Path sipLocation = Paths.get(sipLocationStr);
        Path reportPath = Paths.get(reportPathStr);

        EarkSIPValidator validator = new EarkSIPValidator();
        try {
            System.out.println("Starting validation for SIP at: " + sipLocation);
            boolean isValid = validator.validate(sipLocation, reportPath);
            if (isValid) {
                System.out.println("Validation successful. Report generated at: " + reportPath);
            } else {
                System.err.println("Validation failed. Report generated at: " + reportPath);
            }
        } catch (Exception e) {
            System.err.println("Error during SIP validation: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar target/dps-eark-generator-1.0-SNAPSHOT-jar-with-dependencies.jar <action> [options]");
        System.out.println("\\nActions:");
        System.out.println("  generate <rootPathStr> <outputFolder> <description> <submissionAgreement>");
        System.out.println("    Example: java -jar your-jar.jar generate \\\"~/e-ark/digifoto_20171115_00295_NB_MIT_ENR_00169\\\" \\\"~/output\\\" \\\"My Collection\\\" \\\"SA-123\\\"");
        System.out.println("  validate <pathToSip> <pathToReportOutput>");
        System.out.println("    Example: java -jar your-jar.jar validate \\\"~/output/digifoto_20171115_00295_NB_MIT_ENR_00169\\\" \\\"./validation_report.json\\\"");
        System.out.println("    Example (for folder based SIP): java -jar your-jar.jar validate \\\"~/output/my_sip_folder\\\" \\\"./validation_report.json\\\"");
    }
}