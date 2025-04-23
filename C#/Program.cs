using Hl7.Fhir.Model;
using Hl7.Fhir.Rest;
using System.Threading.Tasks;
using Task = System.Threading.Tasks.Task;

class Program
{
    static async Task Main(string[] args)
    {
        try
        {
            // Initialize FHIR client. Define within the Hl7.Fhir.Rest namespace.
            var client = new FhirClient(
                "https://fhir-bootcamp.medblocks.com/fhir",
                new FhirClientSettings
                {
                    PreferredFormat = ResourceFormat.Json,
                    VerifyFhirVersion = false
                });

            // Create a Patient resource. Define within the Hl7.Fhir.Model namespace. HumanName, Identifier, ContactPoint, AdministrativeGender, and FhirDateTime are also defined within the Hl7.Fhir.Model namespace.
            var patient = new Patient
            {
                Name = new List<HumanName>
                {
                    new HumanName { Family = "Smith", Given = new[] { "John" }.ToList() }
                },
                Identifier = new List<Identifier>
                {
                    new Identifier("http://hospital.medblocks.org/mrn", "MRN12345")
                },
                Telecom = new List<ContactPoint>
                {
                    new ContactPoint
                    {
                        System = ContactPoint.ContactPointSystem.Phone,
                        Value = "+1234567890",
                        Use = ContactPoint.ContactPointUse.Home
                    }
                },
                Gender = AdministrativeGender.Male,
                BirthDate = "1990-01-01"
            };

            // Create patient on the server. The FHIR client does client-side validation, so the resource must be valid before sending it to the server.
            // The FHIR client will throw an exception if the resource is invalid.
            try 
            {
                var createdPatient = await client.CreateAsync(patient);
                Console.WriteLine($"Patient created with ID: {createdPatient?.Id ?? "unknown"}");

                // Create an Observation linked to the patient
                var observation = new Observation
                {
                    Status = ObservationStatus.Final,
                    Code = new CodeableConcept("http://loinc.org", "8867-4", "Heart rate", "Heart rate measurement"),
                    Subject = new ResourceReference($"Patient/{createdPatient?.Id}"),
                    Effective = new FhirDateTime(DateTimeOffset.Now),
                    Value = new Quantity(72, "beats/min", "http://unitsofmeasure.org")
                };

                // Create observation on the server
                var createdObservation = await client.CreateAsync(observation);
                Console.WriteLine($"Observation created with ID: {createdObservation?.Id ?? "unknown"}");
            }
            catch (FhirOperationException ex)
            {
                Console.WriteLine($"FHIR Operation Error: {ex.Message}");
                if (ex.Outcome != null)
                {
                    foreach (var issue in ex.Outcome.Issue)
                    {
                        Console.WriteLine($"- {issue.Details?.Text ?? issue.Diagnostics}");
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error: {ex.Message}");
        }
    }
}
