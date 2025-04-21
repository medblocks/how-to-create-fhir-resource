package com.medblocks;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;

public class FhirDemo {
    private static final Logger logger = LoggerFactory.getLogger(FhirDemo.class);
    private final FhirContext ctx;
    private final IGenericClient client;

    public FhirDemo(String fhirServerUrl) {
        // Create FHIR context and client
        ctx = FhirContext.forR4();
        client = ctx.newRestfulGenericClient(fhirServerUrl);
        logger.info("Initialized FHIR client with server URL: {}", fhirServerUrl);
    }

    public Patient createMinimalPatient() {
        Patient patient = new Patient();
        patient.addIdentifier()
            .setSystem("http://examplehospital.com/patients")
            .setValue("123456");
        patient.addName()
            .setFamily("Doe")
            .addGiven("John")
            .addGiven("Robert");
        patient.setGender(AdministrativeGender.MALE)
            .setBirthDate(Date.from(LocalDate.of(1990, 5, 16)
                .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        
        logger.debug("Created Patient resource:\n{}", ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient));
        return patient;
    }

    public Observation createMinimalObservation(String patientId) {
        Observation observation = new Observation()
            .setStatus(ObservationStatus.FINAL)
            .setCategory(Collections.singletonList(
                new CodeableConcept().addCoding(
                    new Coding("http://terminology.hl7.org/CodeSystem/observation-category",
                             "vital-signs", "Vital Signs"))))
            .setCode(new CodeableConcept().addCoding(
                new Coding("http://loinc.org","29463-7","Body weight")))
            .setValue(new Quantity().setValue(75).setUnit("kg").setSystem("http://unitsofmeasure.org"))
            .setEffective(new DateTimeType(LocalDateTime.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
            .setSubject(new Reference("Patient/" + patientId));
        
        logger.debug("Created Observation resource:\n{}", ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(observation));
        return observation;
    }

    public MethodOutcome createPatient(Patient patient) {
        logger.info("Creating Patient resource...");
        MethodOutcome outcome = client.create()
            .resource(patient)
            .prettyPrint()
            .encodedJson()
            .execute();
        
        logger.info("Patient created successfully. ID: {}", outcome.getId().getIdPart());
        logger.debug("Patient creation response:\n{}", ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
        return outcome;
    }

    public MethodOutcome createObservation(Observation observation) {
        logger.info("Creating Observation resource...");
        MethodOutcome outcome = client.create()
            .resource(observation)
            .execute();
        
        logger.info("Observation created successfully. ID: {}", outcome.getId().getIdPart());
        logger.debug("Observation creation response:\n{}", ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(outcome.getResource()));
        return outcome;
    }

    /** 
     * Validates a FHIR resource against the FHIR server's validation rules.
     * @param resource The FHIR resource to validate.
     * @return ValidationResult containing the validation results.
     */

    public ValidationResult validateResource(Resource resource) {
        logger.info("Validating resource of type: {}", resource.getClass().getSimpleName());
        FhirValidator validator = ctx.newValidator();
        ValidationResult result = validator.validateWithResult(resource);
        
        if (!result.isSuccessful()) {
            logger.warn("Validation failed for resource:");
            result.getMessages().forEach(m -> 
                logger.warn("{}: {}", m.getSeverity(), m.getMessage()));
        } else {
            logger.info("Resource validation successful");
        }
        return result;
    }


    /** IMPOERTANT NOTE:
     * Creates a transaction bundle containing a Patient and an Observation resource.
     * The Patient resource is added with a temporary ID, and the Observation resource
     * references this temporary ID.
     * Once the transaction is executed, the server will assign a permanent ID to the Patient resource.
     * The Observation resource will then reference the permanent ID of the Patient.
     */

    public Bundle createTransactionBundle(Patient patient, Observation observation) {
        logger.info("Creating transaction bundle...");
        Bundle tx = new Bundle().setType(BundleType.TRANSACTION);
        
        // Add Patient to bundle
        tx.addEntry()
          .setFullUrl("urn:uuid:pat-temp")
          .setResource(patient)
          .getRequest().setMethod(HTTPVerb.POST).setUrl("Patient");

        // Link Observation to temporary Patient reference
        observation.setSubject(new Reference("urn:uuid:pat-temp"));

        // Add Observation to bundle
        tx.addEntry()
          .setResource(observation)
          .getRequest().setMethod(HTTPVerb.POST).setUrl("Observation");

        logger.debug("Transaction bundle created:\n{}", ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(tx));
        return tx;
    }

    public Bundle executeTransaction(Bundle transactionBundle) {
        logger.info("Executing transaction bundle...");
        Bundle response = client.transaction().withBundle(transactionBundle).execute();
        
        logger.info("Transaction completed successfully");
        logger.debug("Transaction response:\n{}", ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(response));
        
        // Log the IDs of created resources
        response.getEntry().forEach(entry -> {
            if (entry.getResponse().getStatus().startsWith("201")) {
                String location = entry.getResponse().getLocation();
                logger.info("Created resource: {}", location);
            }
        });
        
        return response;
    }

    // Uncomment and implement this method if you need to set a Bearer token for authentication

    // public void setBearerToken(String token) {
    //     client.registerInterceptor(new BearerTokenAuthInterceptor(token));
    // }

    public static void main(String[] args) {
        // Example usage
        FhirDemo demo = new FhirDemo("https://fhir-bootcamp.medblocks.com/fhir");
        
        // Create and validate a patient
        Patient patient = demo.createMinimalPatient();
        ValidationResult patientValidation = demo.validateResource(patient);
        if (!patientValidation.isSuccessful()) {
            logger.error("Patient validation failed");
            return;
        }

        // Create patient on server
        MethodOutcome patientOutcome = demo.createPatient(patient);
        String patientId = patientOutcome.getId().getIdPart();

        // Create and validate observation
        Observation observation = demo.createMinimalObservation(patientId);
        ValidationResult observationValidation = demo.validateResource(observation);
        if (!observationValidation.isSuccessful()) {
            logger.error("Observation validation failed");
            return;
        }

        // Create and validate transaction bundle
        Bundle transactionBundle = demo.createTransactionBundle(patient, observation);
        ValidationResult bundleValidation = demo.validateResource(transactionBundle);
        if (!bundleValidation.isSuccessful()) {
            logger.error("Bundle validation failed");
            return;
        }

        // Execute transaction if all validations pass
        Bundle response = demo.executeTransaction(transactionBundle);
        
        // Log the IDs of created resources
        response.getEntry().forEach(entry -> {
            if (entry.getResponse().getStatus().startsWith("201")) {
                String location = entry.getResponse().getLocation();
                logger.info("Created resource: {}", location);
            }
        });
    }
} 