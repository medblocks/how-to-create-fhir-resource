# FHIR Client Example

This project demonstrates how to create and post FHIR resources (Patient and Observation) to a FHIR server using C# and the Firely SDK.

## Prerequisites

### Installing .NET SDK

1. **macOS (using Homebrew)**:

   ```bash
   brew install dotnet
   ```

   After installation, set the DOTNET_ROOT environment variable:

   ```bash
   export DOTNET_ROOT="/opt/homebrew/opt/dotnet/libexec"
   ```

   Add this line to your `~/.zshrc` or `~/.bash_profile` to make it permanent.

2. **Windows**:

   - Download and install .NET SDK from [Microsoft's .NET download page](https://dotnet.microsoft.com/download)

3. **Linux (Ubuntu/Debian)**:
   ```bash
   wget https://packages.microsoft.com/config/ubuntu/20.04/packages-microsoft-prod.deb
   sudo dpkg -i packages-microsoft-prod.deb
   sudo apt-get update
   sudo apt-get install -y dotnet-sdk-9.0
   ```

## Project Setup

1. **Create a new console project**:

   ```bash
   dotnet new console
   ```

   This command creates:

   - A new C# console project
   - Basic Program.cs file
   - Project file (C#.csproj)

2. **Configure the project file (C#.csproj)**:
   Add the following NuGet package references:

   ```xml
   <ItemGroup>
     <PackageReference Include="Firely.Fhir.Validation.R4" Version="2.6.5" />
     <PackageReference Include="Hl7.Fhir.R4" Version="5.11.4" />
   </ItemGroup>
   ```

3. **Install dependencies**:
   ```bash
   dotnet restore
   ```

## Running the Application

To run the application:

```bash
dotnet run
```

This will:

1. Compile the program
2. Create a Patient resource on the FHIR server
3. Create an Observation resource linked to the created Patient
4. Display the IDs of the created resources

## Project Structure

- `Program.cs`: Contains the main FHIR client implementation
- `C#.csproj`: Project configuration and dependencies
- `.gitignore`: Git ignore patterns for C# projects

## Expected Output

When run successfully, you should see output similar to:

```
Patient created with ID: [generated-uuid]
Observation created with ID: [generated-uuid]
```

## Error Handling

The program includes comprehensive error handling for:

- FHIR operation errors
- Server connection issues
- Resource validation failures

## Notes

- The FHIR server endpoint is set to: `https://fhir-bootcamp.medblocks.com/fhir`
- The program creates a Patient with basic demographics and a heart rate Observation
- All FHIR resources are validated server-side before creation
