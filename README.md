# PIO Mapping Prototype

A configuration-based Java mapping component for transforming heterogeneous healthcare data into selected HL7 FHIR-based structures of the German nursing transfer document (PIO-ULB).

## Project Overview

Healthcare information systems often use different data structures, field names and value representations, making automated data exchange and interoperability challenging.

This project investigates how heterogeneous JSON source data can be transformed into standardized PIO/FHIR target structures using a shared mapping configuration without adapting the configuration to individual source schemas.

The prototype was developed as part of my Bachelor's thesis in Software Development at IU International University of Applied Sciences.

## Key Features

- Configuration-driven mapping using external YAML files
- Recursive processing of nested JSON structures
- Schema matching based on field names, structural context and data types
- Rule-based value transformation
- Conditional mapping
- Processing of repeatable data elements
- Generation of selected PIO/FHIR target structures
- Automated unit and integration tests

## Architecture

The architecture separates domain-specific mapping rules from the general matching and transformation logic.

```text
Heterogeneous JSON Source Data
              |
              v
     JSON Field Extraction
              |
              v
       Matching Engine <---- YAML Configuration
              |
              v
      Value Transformation
              |
              v
    PIO/FHIR Target Structures
```

The matching engine identifies relevant source information using three complementary criteria:

- **Linguistic matching:** identification and normalization of field names.
- **Structural matching:** consideration of the structural context of source elements.
- **Data type matching:** verification of compatible data types.

The identified values are subsequently transformed according to the external mapping configuration.

## Technologies

| Technology | Purpose |
|---|---|
| Java 21 | Application development |
| Apache Maven | Build and dependency management |
| Jackson | JSON and YAML processing |
| Apache Commons CSV | CSV data processing |
| JUnit 5 | Automated testing |
| HL7 FHIR | Standardized healthcare target structures |
| Git | Version control |

## Implemented Mapping Cases

| ID | Healthcare Information | Mapping Approach |
|---|---|---|
| M1 | Body weight | Direct field mapping |
| M2 | Diagnosis | Nested data structures |
| M3 | Medication information | Value transformation |
| M4 | Pain information | Conditional mapping |
| M5 | Allergies and intolerances | Repeatable elements |

## Repository Structure

```text
pio-mapping-prototype/
├── config/       # YAML mapping configuration
├── data/         # Source and evaluation data
├── evaluation/   # Evaluation-related files
├── expected/     # Expected mapping results
├── src/
│   ├── main/     # Java implementation
│   └── test/     # Automated tests
└── pom.xml       # Maven configuration
```

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 21
- Apache Maven
- Git

### Clone the Repository

```bash
git clone https://github.com/JohnHerdling/pio-mapping-prototype.git
cd pio-mapping-prototype
```

### Build the Project

```bash
mvn clean package
```

### Run Automated Tests

```bash
mvn test
```

## Evaluation and Results

The prototype was developed and evaluated following the Design Science Research Methodology (DSRM).

The development process included three differently structured prototype JSON schemas (Q1–Q3), synthetic healthcare data from Synthea and clinical data from the eICU Collaborative Research Database Demo (v2.0.1).

The final evaluation was conducted using 129 previously withheld eICU patient cases.

| Evaluation Metric | Result |
|---|---:|
| Evaluated patient cases | 129 |
| Evaluated mapping cases per patient | 4 |
| Total transformations | 516 |
| Correct transformations | 516 |
| Mapping accuracy | 100% |

The mapping configuration remained unchanged throughout the investigation. However, the general processing logic required iterative improvements to handle additional source data representations.

The diagnosis mapping case (M2) was excluded from the external evaluation because the required terminology conversion to ICD-10-GM was outside the implemented scope.

## Limitations

This project is a research prototype and is not intended for clinical or production use.

The evaluation is limited to selected mapping cases and datasets. Full FHIR profile validation, clinical validation and universal compatibility with healthcare information systems are outside the scope of this project.

## Future Improvements

- Full FHIR profile validation
- Extended medical terminology mapping
- Confidence scoring for ambiguous matches
- Evaluation using additional independent healthcare data sources
- Improved error handling and performance testing

## Data Sources and Standards

- [HL7 FHIR](https://hl7.org/fhir/)
- [MIO / PIO – mio42](https://mio.kbv.de/)
- [Synthea](https://synthea.mitre.org/)
- [eICU Collaborative Research Database Demo](https://physionet.org/content/eicu-crd-demo/2.0.1/)

## Author

**John Herdling**

B.Sc. Software Development  
IU International University of Applied Sciences

Bachelor's thesis (2026):

*Konzeption eines konfigurationsbasierten Mapping-Ansatzes zur Integration heterogener Systeme im Gesundheitswesen*
