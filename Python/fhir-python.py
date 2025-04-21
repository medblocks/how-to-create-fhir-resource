#!/usr/bin/env python3

import logging
from datetime import datetime, timezone
import requests
from fhir.resources.patient import Patient
from fhir.resources.humanname import HumanName
from fhir.resources.observation import Observation
from fhir.resources.codeableconcept import CodeableConcept
from fhir.resources.coding import Coding
from fhir.resources.quantity import Quantity
from fhir.resources.reference import Reference

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('fhir_operations.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# FHIR Server Configuration
FHIR_BASE = "https://fhir-bootcamp.medblocks.com/fhir"
HEADERS = {
    "Content-Type": "application/fhir+json",
    "Accept": "application/fhir+json"
}

def create_patient():
    try:
        logger.info("Creating Patient resource...")

        # THIS IS THE COMPACT WAY OF DOING IT

        # patient = Patient(
        #     active=True,
        #     name=[HumanName(use="official",
        #                   family="Doe",
        #                   given=["John"])],
        #     gender="male",
        #     birthDate="1990-04-01"
        # )


        # MORE TUTORIAL FRIENDLY WAY

        patient = Patient()
        patient.active = True
        name = HumanName()
        name.use = "official"
        name.family = "Doe"
        name.given = ["John"]

        patient.name = [name]

        patient.gender ="male"
        patient.birthDate = "1990-04-01"

        logger.debug(f"Patient resource created: {patient.model_dump_json()}")
        
        resp = requests.post(f"{FHIR_BASE}/Patient",
                           headers=HEADERS,
                           data=patient.model_dump_json())
        resp.raise_for_status()
        
        patient_id = resp.json()["id"]
        logger.info(f"Patient created successfully with ID: {patient_id}")
        return patient_id

    except requests.exceptions.RequestException as e:
        logger.error(f"Error creating Patient: {str(e)}")
        raise
    except Exception as e:
        logger.error(f"Unexpected error creating Patient: {str(e)}")
        raise

def create_observation(patient_id):

    try:
        logger.info(f"Creating Observation resource for Patient {patient_id}...")
        
        loinc_code = Coding(
            system="http://loinc.org",
            code="29463-7",
            display="Body weight"
        )

        # COMPACT WAY
        
        # obs = Observation(
        #     status="final",
        #     code=CodeableConcept(
        #         coding=[loinc_code],
        #         text="Body weight"
        #     ),
        #     subject=Reference(reference=f"Patient/{patient_id}"),
        #     effectiveDateTime=datetime.now(timezone.utc).isoformat(),
        #     valueQuantity=Quantity(
        #         value=72.3,
        #         unit="kg",
        #         system="http://unitsofmeasure.org",
        #         code="kg"
        #     )
        # )

        # MORE TUTORIAL FRIENDLY WAY

         # Create and set the code
        code = CodeableConcept()
        code.coding = [loinc_code]
        code.text = "Body weight"

        # IMPORTANT: WHILE INITIALIZING A RESOURCE, ALL MANDATORY FIELDS MUST BE SET. ADDING THEM LATER IS NOT ENOUGH

        obs = Observation(
            code=code,
            status="final"
        )
        
       

        # Set the effective date time
        obs.effectiveDateTime = datetime.now(timezone.utc).isoformat()

        # Create and set the subject reference
        subject = Reference()
        subject.reference = f"Patient/{patient_id}"
        obs.subject = subject

        # Create and set the value quantity
        value = Quantity()
        value.value = 72.3
        value.unit = "kg"
        value.system = "http://unitsofmeasure.org"
        value.code = "kg"
        obs.valueQuantity = value
       

        logger.info(f"Observation resource created: {obs.model_dump_json()}")
        
        resp = requests.post(f"{FHIR_BASE}/Observation",
                           headers=HEADERS,
                           data=obs.model_dump_json())
        resp.raise_for_status()
        
        observation_id = resp.json()["id"]
        logger.info(f"Observation created successfully with ID: {observation_id}")
        return observation_id

    except requests.exceptions.RequestException as e:
        logger.error(f"Error creating Observation: {str(e)}")
        raise
    except Exception as e:
        logger.error(f"Unexpected error creating Observation: {str(e)}")
        raise

def main():
    """Main function to create FHIR resources."""
    try:
        logger.info("Starting FHIR resource creation process...")
        
        # Create Patient
        patient_id = create_patient()
        
        # Create Observation linked to the Patient
        observation_id = create_observation(patient_id)
        
        logger.info("FHIR resource creation completed successfully!")
        logger.info(f"Created Patient ID: {patient_id}")
        logger.info(f"Created Observation ID: {observation_id}")
        
    except Exception as e:
        logger.error(f"Process failed: {str(e)}")
        raise

if __name__ == "__main__":
    main()
