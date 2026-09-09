# Erwartete Mapping-Ergebnisse

## Zweck

Die Datei `expected-output.json` definiert die vor der Implementierung
festgelegten Soll-Ergebnisse für die Evaluation der Mapping-Komponente.

Die drei Quellschemata Q1, Q2 und Q3 repräsentieren dieselben fachlichen
Informationen in unterschiedlichen strukturellen Ausprägungen. Daher wird
für alle drei Quellschemata dieselbe Sollreferenz verwendet.

Die Sollwerte werden vor der Implementierung festgelegt und während der
Evaluation nicht an die tatsächlich erzeugten Ergebnisse angepasst.

## Mappingfälle

### M1 – Direkte Zuordnung
- Quellinformation: Körpergewicht
- PIO-Element: 2.11.1.6.2 Wert
- FHIR-Ziel: `valueQuantity.value`
- Erwarteter Wert: `82`
- Untersuchte Eigenschaft: direkte Übernahme eines Wertes

### M2 – Verschachtelter Datenpfad
- Quellinformation: Diagnosecode
- PIO-Element: 2.1.2.2.1.1.1 Diagnosecode
- FHIR-Ziel: `code.coding:ICD-10-GM.code`
- Erwarteter Wert: `I10`
- Untersuchte Eigenschaft: Auflösung eines verschachtelten Quellpfades

### M3 – Werttransformation
- Quellinformation: Medikationsstatus
- PIO-Element: 2.9.1 Information zur Einnahme von Medikamenten
- FHIR-Ziel: `valueCodeableConcept`
- Erwarteter Wert: `Medikation liegt vor`
- Untersuchte Eigenschaft: Transformation eines systemspezifischen
  Quellwertes in einen definierten Zielwert

### M4 – Bedingte Zuordnung
- Quellinformation: Vorliegen einer Schmerzsymptomatik
- PIO-Element: 2.3.1 Vorliegen einer Schmerzsymptomatik
- FHIR-Ziel: `valueCodeableConcept`
- Erwarteter Wert: `22253000`
- Untersuchte Eigenschaft: bedingte Erzeugung eines Zielwertes

### M5 – Wiederholung
- Quellinformation: Allergien/Unverträglichkeiten
- PIO-Element: 2.8.2.2.2.2 Freitext Anzeigename
- FHIR-Ziel: `code.text`
- Erwartete Werte: `Penicillin`, `Latex`
- Untersuchte Eigenschaft: Verarbeitung mehrerer gleichartiger Elemente

## Evaluationsprinzip

Für jedes Quellschema wird die erzeugte Ausgabe mit `expected-output.json`
verglichen. Eine Abweichung zwischen Soll- und Ist-Ergebnis wird als
nicht vollständig erfolgreiches Mapping dokumentiert.

Die Ground Truth wird nicht nachträglich an das Verhalten der
Mapping-Komponente angepasst.

## Festlegung der Sollreferenz

Die Sollreferenz wurde vor der Implementierung der Mapping-Komponente
festgelegt. Die Auswahl der PIO-Elemente und FHIR-Ziele orientiert sich
am Informationsmodell und den FHIR-Mappings des PIO-Überleitungsbogens
Version 1.0.0 der KBV.

Die in `expected-output.json` enthaltenen konkreten Testwerte wie
Körpergewicht 82, Diagnosecode I10 sowie die Allergiebezeichnungen
Penicillin und Latex sind synthetische Testdaten. Sie stellen keine
inhaltlichen Vorgaben der KBV dar.

Terminologische Harmonisierung ist nicht Gegenstand des Prototyps.
Soweit standardisierte Codes für die ausgewählten Mappingfälle verwendet
werden, werden diese als festgelegte Zielwerte übernommen.

Die Sollreferenz wird nach Beginn der Implementierung nicht an das
Verhalten der Mapping-Komponente angepasst. Abweichungen zwischen
Soll- und Ist-Ergebnis werden als Evaluationsergebnis dokumentiert.