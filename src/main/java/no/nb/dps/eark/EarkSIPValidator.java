package no.nb.dps.eark;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.ParserConfigurationException;

import org.roda_project.commons_ip2.validator.EARKSIPValidator;
import org.roda_project.commons_ip2.validator.reporter.ValidationReportOutputJson;
import org.xml.sax.SAXException;

public class EarkSIPValidator {

    public boolean validate(Path sipPath, Path reportPath) {
        
        final String EARK_VERSION = "2.2.0";

        try {
            // Ensure parent directories for the report file exist
            Path parentDir = reportPath.getParent();
            if (parentDir != null) {
                if (!Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                } else if (!Files.isDirectory(parentDir)) {
                    throw new IOException("Parent path for report is not a directory: " + parentDir);
                }
            }

            //create the report file if it does not exist
            if (!Files.exists(reportPath)) {                
                Files.createFile(reportPath);
            }

            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(reportPath.toFile()))) {
                ValidationReportOutputJson reportOutputJson = new ValidationReportOutputJson(sipPath, outputStream);
                EARKSIPValidator earksipValidator = new EARKSIPValidator(reportOutputJson, EARK_VERSION);
                return earksipValidator.validate(EARK_VERSION);
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException("Error during SIP validation or report generation: " + e.getMessage(), e);
        }
    }
}