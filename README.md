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
| `stdnum-core` | SPI, exceptions, check digit algorithms (Luhn, Damm, Verhoeff, ISO 7064, Mod 97-10, weighted mod 11, CRC-16), the NumDb prefix database and the registry. Zero dependencies. |
| `stdnum-tck`  | Reusable JUnit 5 contract tests for `StdNum` implementations. |
| `stdnum-international` | Country-independent formats: IBAN, ISBN, ISSN, ISMN, ISNI, EAN/GTIN, ISIN, CUSIP, SEDOL, FIGI, BIC, IMEI, MAC, LEI, IMO, CAS RN, ISO 6346, ISO 11649 and the VATIN dispatcher. |
| `stdnum-br` | Brazil: CPF, CNPJ (including the 2026 alphanumeric format), PIS/PASEP, CNS, titulo de eleitor, RENAVAM, NF-e access key, the FEBRABAN payment slip, the Pix BR Code and the state tax registrations of all 27 federative units. |
| `stdnum-eu` | Europe: 58 types across AD, AL, AT, AZ, BE, BG, CH, CY, CZ, DE, DK, EE, ES, FI, FO, FR, GB, GR, HR, HU, IE, IS, IT, LI, LT, LU, LV, MD, ME, MK, MT, NL, NO, PL, PT, RO, RS, SE, SI, SK and UA. |
| `stdnum-latam` | Latin America: AR, CL, CO, CR, CU, DO, EC, GT, MX, PE, PY, SV, UY and VE. |
| `stdnum-na` | North America: the US SSN, ITIN, EIN and routing number, and the Canadian SIN and business number. |
| `stdnum-apac` | Asia-Pacific and beyond: AU, CN, ID, IL, IN, JP, KR, MY, NZ, OM, PK, RU, SG, TH, TR, TW and VN. |
| `stdnum-africa` | Africa: DZ, EG, GH, KE, MA, MU, MZ, SN and ZA. |
| `stdnum-all` | Aggregator depending on every module, with a registry-wide contract sweep. |

## Build

```
mvn verify
```

Requires JDK 17+.
