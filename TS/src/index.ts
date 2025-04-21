import axios from "axios";
import type {
	Patient,
	Observation,
	HumanName,
	CodeableConcept,
	CodeSystem,
	Coding,
	Reference,
	Quantity,
	Identifier,
} from "fhir/r4";
import { Fhir } from "fhir/fhir.js";

// FHIR server base URL
const BASE_URL = "https://fhir-bootcamp.medblocks.com/fhir";

// Initialize FHIR validator
const fhir = new Fhir();

// Function to validate FHIR resource
function validateResource(resource: any) {
	if (!resource.resourceType) {
		throw new Error("Resource must have a resourceType");
	}

	const result = fhir.validate(resource);
	if (!result.valid) {
		throw new Error(
			`Invalid ${resource.resourceType}: ${result.messages.join(", ")}`
		);
	}
	return true;
}

// Create a Patient resource

const name: HumanName = {
	use: "official",
	text: "Jim Chalmers",
	family: "Chalmers",
	given: ["Jim"],
};

const identifier: Identifier = {
	system: "http://examplehosptal.com/patient",
	value: "123456",
};

const patient: Patient = {
	resourceType: "Patient",
	identifier: [identifier],
	active: true,
	name: [name],
	gender: "male",
	birthDate: "1974-12-25",
};

// Function to create a FHIR resource
async function createResource<T extends { resourceType: string }>(
	resource: T
): Promise<T & { id: string }> {
	try {
		// Validate before sending
		validateResource(resource);

		const response = await axios.post(
			`${BASE_URL}/${resource.resourceType}`,
			resource,
			{
				headers: {
					"Content-Type": "application/fhir+json",
				},
			}
		);
		return response.data;
	} catch (error) {
		if (axios.isAxiosError(error)) {
			console.error(
				"Error creating resource:",
				error.response?.data || error.message
			);
		}
		throw error;
	}
}

// Main async function to create resources
async function main() {
	try {
		// Create patient first
		console.log("Creating patient...");
		const createdPatient = await createResource(patient);
		console.log("Patient created with ID:", createdPatient.id);

		// Create observation that references the patient

		const coding: Coding = {
			system: "http://loinc.org",
			code: "29463-7",
			display: "Body weight",
		};

		const code: CodeableConcept = {
			coding: [coding],
			text: "Body weight Measured",
		};

		const subject: Reference = {
			reference: `Patient/${createdPatient.id}`, // Reference to the created patient
		};

		const valueQuantity: Quantity = {
			value: 72.3,
			unit: "kg",
			system: "http://unitsofmeasure.org",
			code: "kg",
		};

		const observation: Observation = {
			resourceType: "Observation",
			status: "final",
			code: code,
			subject: subject,
			effectiveDateTime: new Date().toISOString(),
			valueQuantity: valueQuantity,
		};

		console.log("Creating observation...");
		const createdObservation = await createResource(observation);
		console.log("Observation created with ID:", createdObservation.id);
	} catch (error) {
		console.error("Error in main:", error);
	}
}

// Run the main function
main();
