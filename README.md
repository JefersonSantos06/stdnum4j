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

### Saying why, in a language

`getMessage()` and `Check.Invalid.reason()` are English, which is what belongs
in a log. To show the failure to a person, hand it to a `Messages` for their
locale — the library never picks one, holds no global state, and never reads
`Locale.getDefault()`:

```java
Messages pt = Messages.forLocale(Locale.forLanguageTag("pt-BR"));

switch (cpf.check(input)) {
    case Check.Valid v   -> store(v.compact());
    case Check.Invalid i -> reject(pt.render(i));   // "Um CPF formado por um
}                                                  //  único dígito repetido
                                                   //  não é válido."
```

A sentence is looked for under the message's own code, in the package of the
class that threw — so a module ships its translations inside its own jar —
then under `error.<NAME>` for the `ValidationError`, four sentences every
translation carries, and finally the English the validator was written with.
A translation is therefore never all-or-nothing: what is not translated yet
costs a less specific sentence, not an English one.

Translations are `messages_<language>[_<COUNTRY>].properties`, read as UTF-8
from the package of the anchor class. There is deliberately no
`messages.properties`: English lives in the Java source, once, next to the
`throw`. On the module path the anchor's package has to be open to
`stdnum-core`; nothing here declares a `module-info`, and automatic modules
are open, so that costs nothing today.

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
| `stdnum-tck` | Reusable JUnit 5 contract tests for `StdNum` implementations. |
| `stdnum-international` | Country-independent formats: IBAN, ISBN, ISSN, ISMN, ISNI, EAN/GTIN, ISIN, CUSIP, SEDOL, FIGI, BIC, IMEI, MAC, LEI, IMO, CAS RN, ISO 6346, ISO 11649, GRid, ISAN, ISRC, MEID, UPI, CFI, IMSI, the GS1-128 element string, the Bitcoin address and the VATIN and EU VAT dispatchers. |
| `stdnum-br` | Brazil: CPF, CNPJ (including the 2026 alphanumeric format), PIS/PASEP, CNS, titulo de eleitor, RENAVAM, NF-e access key, the FEBRABAN payment slip, the Pix BR Code and the state tax registrations of all 27 federative units. |
| `stdnum-eu` | Europe: 128 types across AD, AL, AT, AZ, BE, BG, BY, CH, CY, CZ, DE, DK, EE, ES, FI, FO, FR, GB, GR, HR, HU, IE, IS, IT, LI, LT, LU, LV, MC, MD, ME, MK, MT, NL, NO, PL, PT, RO, RS, SE, SI, SK, SM and UA, plus the EU-wide SEPA creditor identifier and One Stop Shop numbers. |
| `stdnum-latam` | Latin America: AR, CL, CO, CR, CU, DO, EC, GT, MX, PE, PY, SV, UY and VE. |
| `stdnum-na` | North America: the US SSN, ITIN, EIN, PTIN, ATIN, the TIN that is whichever of them a taxpayer holds, and the routing number; and the Canadian SIN, business number and British Columbia health number. |
| `stdnum-apac` | Asia-Pacific and beyond: AU, CN, ID, IL, IN, JP, KR, MY, NZ, OM, PK, RU, SG, TH, TR, TW and VN, including the Chinese identity card and the New Zealand bank account. |
| `stdnum-africa` | Africa: DZ, EG, GH, GN, KE, MA, MU, MZ, SN, TN and ZA. |
| `stdnum-all` | Aggregator depending on every module, with a registry-wide contract sweep. |

Every module depends on `stdnum-core` alone, except `stdnum-eu`, which also
depends on `stdnum-international`: a Spanish, Montenegrin or Norwegian IBAN is
an IBAN with a national rule on top. The dependency runs one way — the
international module reaches country types through the registry, never by
importing them.

## Data files

The prefix databases are generated from their upstream registries, never
hand-edited: the IBAN and ISBN registries, the Austrian postcodes, the
Czech, Belgian and New Zealand bank registers, the ISO 10962 CFI
classification, the mobile country and network codes, the Chinese
administrative division codes, the GS1 application identifiers, the NACE
classification, the Austrian tax offices and the Indonesian regions. The generators live in
[`tools/`](tools/README.md), which documents how to rebuild each file. They
have no dependencies: the two published as spreadsheets are read with the
JDK alone, an xlsx being a zip of XML.

## Build

```
mvn verify
```

Requires JDK 17+.
