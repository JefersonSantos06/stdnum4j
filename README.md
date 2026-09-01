# java-stdnum

A Java library to parse, validate and reformat standard numbers and codes
(tax numbers, personal identifiers, bank numbers, product codes, ...).
Inspired by [python-stdnum](https://github.com/arthurdejong/python-stdnum);
implemented from scratch from the public specifications.

> Status: early development. The public API may still change.

## Design

Every number type implements a single small interface:

```java
public interface StdNum {
    Descriptor descriptor();
    String compact(String number);   // minimal representation
    String validate(String number);  // validates AND returns the compact form
    default boolean isValid(String number);
    default Check check(String number);  // exception-free result: Valid | Invalid
    default String format(String number);
}
```

`validate()` throws a `ValidationException` subtype (`InvalidFormatException`,
`InvalidLengthException`, `InvalidChecksumException`, `InvalidComponentException`)
when the number is invalid. The exceptions carry no stack trace: they are cheap
signalling, not error reporting.

Implementations register through `StdNumProvider` (Java `ServiceLoader`) and are
discoverable via the registry:

```java
StdNums.byId("br.cpf");
StdNums.byCountry("BR");
StdNums.byTag(Tag.TAX);
```

## Modules

| Module | Content |
|---|---|
| `stdnum-core` | SPI, exceptions, check digit algorithms (Luhn, Damm, Verhoeff, ISO 7064, Mod 97-10, weighted mod 11), the NumDb prefix database and the registry. Zero dependencies. |
| `stdnum-tck`  | Reusable JUnit 5 contract tests for `StdNum` implementations. |
| `stdnum-international` | Country-independent formats: IBAN, ISBN, ISSN, ISMN, EAN/GTIN, ISIN, CUSIP, BIC, IMEI, LEI, IMO, CAS RN and the VATIN dispatcher. |
| `stdnum-br` | Brazil: CPF, CNPJ (including the 2026 alphanumeric format), PIS/PASEP, CNS, titulo de eleitor, RENAVAM, NF-e access key and the state tax registrations of all 27 federative units. |
| `stdnum-eu` | Europe: 48 types across AT, BE, BG, CH, CY, CZ, DE, DK, EE, ES, FI, FR, GB, GR, HR, HU, IE, IS, IT, LT, LU, LV, MT, NL, NO, PL, PT, RO, SE, SI and SK. |
| `stdnum-latam` | Latin America: AR, CL, CO, CU, EC, GT, PE, PY, UY and VE. |
| `stdnum-na` | North America: the US SSN, ITIN, EIN and routing number, and the Canadian SIN and business number. |
| `stdnum-apac` | Asia-Pacific and beyond: AU, CN, IL, IN, JP, NZ, RU and TR. |
| `stdnum-africa` | Africa: the South African ID number and TIN. |
| `stdnum-all` | Aggregator depending on every module, with a registry-wide contract sweep. |

## Build

```
mvn verify
```

Requires JDK 17+.
