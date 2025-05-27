package no.nb.dps.eark;

import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip.utils.METSEnums;
import org.roda_project.commons_ip2.cli.model.enums.WriteStrategyEnum;
import org.roda_project.commons_ip2.model.*;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {

        if (args.length < 4) {
            System.out.println("Usage: java -jar EarkGenerator-1.0-SNAPSHOT.jar <rootPathStr> <outputFolder> <description> <summissionAgreement>");
            System.exit(1);
        }

//        String rootPathStr = "/home/thomase/temp/e-ark/digifoto_20171115_00295_NB_MIT_ENR_00169"; // Replace with your actual root path
//        String outputFolder = "/home/thomase/temp/e-ark/output"; // Replace with your actual output folder
//        String description = "Sample description for the SIP";
//        String summissionAgreement = "ABCD1234"

        String rootPathStr = args[0];
        String outputFolder = args[1];
        String description = args[2];
        String summissionAgreement = args[3];

        IPContentType contentType = new IPContentType(IPContentType.IPContentTypeEnum.PHOTOGRAPHS_DIGITAL);
        IPContentInformationType contentInformationType = new IPContentInformationType(IPContentInformationType.IPContentInformationTypeEnum.OTHER);
        if (contentInformationType.getType().equals(IPContentInformationType.IPContentInformationTypeEnum.OTHER)) {
            contentInformationType.setOtherType("https://digitalpreservation.no/nb/docs/dps/sip/1.0/profiles/images/");
        }

        IPAgent creatorAgent = new IPAgent("Stiftelsen Helgeland Museum", "CREATOR", null, METSEnums.CreatorType.ORGANIZATION, null, "Organisasjonsnummer:987654321", IPAgentNoteTypeEnum.IDENTIFICATIONCODE);
        IPAgent submitterAgent = new IPAgent("KulturIT AS", "OTHER", "SUBMITTER", METSEnums.CreatorType.ORGANIZATION, null, "Organisasjonsnummer:123456789", IPAgentNoteTypeEnum.IDENTIFICATIONCODE);
        String checksumAlgorithm = IPConstants.CHECKSUM_MD5_ALGORITHM;
        WriteStrategyEnum writeStrategy = WriteStrategyEnum.FOLDER;


        EarkSIPGenerator generator = new EarkSIPGenerator();
        try {
            generator.createSip(Paths.get(rootPathStr), description, outputFolder, contentType, contentInformationType, creatorAgent, submitterAgent, summissionAgreement, checksumAlgorithm, writeStrategy);
            System.out.println("SIP created successfully at: " + outputFolder);
        } catch (IPException e) {
            System.out.println("Error creating SIP: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.out.println("Process was interrupted: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }
}