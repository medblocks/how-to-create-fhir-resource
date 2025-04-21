# FHIR Demo Project

A simple Java project demonstrating how to create and manage FHIR resources using the HAPI FHIR library.

## Quick Start

1. Make sure you have Java 11+ and Maven 3.6+ installed
2. Build and run the project:

```bash
mvn clean install
java -cp target/fhir-demo-1.0-SNAPSHOT.jar com.medblocks.FhirDemo
```

## What it does

This demo project:

- Creates a Patient resource with basic information
- Creates an Observation resource linked to the Patient
- Validates the resources against FHIR R4 specifications
- Demonstrates how to create resources on a FHIR server
- Shows how to bundle multiple resources in a transaction

The code connects to a FHIR server at `https://fhir-bootcamp.medblocks.com/fhir` by default. You can modify this in the `main` method of `FhirDemo.java` if needed.

## Features

- FHIR R4 compliant
- Resource validation
- Transaction bundle support
- OAuth2/Bearer token authentication support

## Dependencies

- HAPI FHIR Client (v7.0.0)
- HAPI FHIR Validation (v7.0.0)
- JUnit 5 (for testing)

## Configuration

Update the FHIR server URL in the `main` method of `FhirDemo` class to point to your FHIR server.

For authentication, use the `setBearerToken` method:

```java
demo.setBearerToken("your-token-here");
```
