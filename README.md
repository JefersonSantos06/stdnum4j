# java-stdnum

A Java library to parse, validate and reformat standard numbers and codes —
tax numbers, personal identifiers, bank and payment numbers, product codes.
**273 number types across 89 countries**, with no dependencies.

Inspired by [python-stdnum](https://github.com/arthurdejong/python-stdnum);
implemented from scratch from the public specifications.

> **Status: early development.** The public API may still change, and nothing
> is published to Maven Central yet. Build it locally with `mvn install`.

## Using it

```java
Cpf.INSTANCE.isValid("390.533.447-05");   // true
Cpf.INSTANCE.validate("390.533.447-05");  // "39053344705"  — the compact form
Cpf.INSTANCE.format("39053344705");       // "390.533.447-05"
```

`validate` returns the compact form and throws a `ValidationException` subtype
when the number is wrong. When an invalid number is an expected outcome rather
than an error — a form, an import, a batch job — use `check`, which returns a
sealed result you can `switch` on exhaustively:

```java
switch (Cnpj.INSTANCE.check(input)) {
    case Check.Valid v   -> store(v.compact());
    case Check.Invalid i -> reject(i.reason());
}
```

Types are also discoverable at runtime, without importing them:

```java
StdNums.byId("br.cpf");        // Optional<StdNum>
StdNums.byCountry("ES");       // every Spanish number type on the classpath
StdNums.byTag(Tag.VAT);        // every VAT number
```

### Rejections in the user's language

`reason()` and `getMessage()` are English, which is what belongs in a log. To
show a failure to a person, hand it to a `Messages` for their locale — the
library never picks one, holds no global state, and never reads
`Locale.getDefault()`:

```java
Messages pt = Messages.forLocale(Locale.forLanguageTag("pt-BR"));

switch (Cpf.INSTANCE.check(input)) {
    case Check.Valid v   -> store(v.compact());
    case Check.Invalid i -> reject(pt.render(i));
    // "Um CPF formado por um único dígito repetido não é válido."
}
```

Every reason the library gives is translated into Portuguese, and the contract
test proves it for every type. A translation is never all-or-nothing: an
untranslated reason falls back to a less specific sentence in the same
language, not to English. [How it works →](docs/ARCHITECTURE.md#saying-why-in-a-language)

## Modules

Take only the regions you need. Every module depends on `stdnum-core` alone,
except `stdnum-eu`, which also depends on `stdnum-international` because a
national IBAN is an IBAN with a national rule on top.

| Module | Types | Content |
|---|---:|---|
| `stdnum-core` | — | SPI, exceptions, check digit algorithms (Luhn, Damm, Verhoeff, ISO 7064, Mod 97-10, weighted mod 11, CRC-16), the `NumDb` prefix database and the registry. Zero dependencies. |
| `stdnum-tck` | — | Reusable JUnit 5 contract tests for `StdNum` implementations. |
| `stdnum-international` | 30 | Country-independent formats: IBAN, ISBN, ISSN, ISMN, ISNI, EAN/GTIN, ISIN, CUSIP, SEDOL, FIGI, BIC, IMEI, MAC, LEI, IMO, CAS RN, ISO 6346, ISO 11649, GRid, ISAN, ISRC, MEID, UPI, CFI, IMSI, the GS1-128 element string, the Bitcoin address and the VATIN and EU VAT dispatchers. |
| `stdnum-br` | 37 | Brazil: CPF, CNPJ (including the 2026 alphanumeric format), PIS/PASEP, CNS, título de eleitor, RENAVAM, NF-e access key, the FEBRABAN payment slip, the Pix BR Code and the state tax registrations of all 27 federative units. |
| `stdnum-eu` | 128 | Europe: 44 countries, plus the EU-wide SEPA creditor identifier, One Stop Shop, EIC, NACE, EC number and banknote serial. |
| `stdnum-latam` | 23 | Latin America: AR, CL, CO, CR, CU, DO, EC, GT, MX, PE, PY, SV, UY, VE. |
| `stdnum-na` | 10 | North America: the US SSN, ITIN, EIN, PTIN, ATIN, the TIN that is whichever of them a taxpayer holds, and the routing number; the Canadian SIN, business number and British Columbia health number. |
| `stdnum-apac` | 33 | Asia-Pacific and beyond: AU, CN, ID, IL, IN, JP, KR, MY, NZ, OM, PK, RU, SG, TH, TR, TW, VN. |
| `stdnum-africa` | 12 | Africa: DZ, EG, GH, GN, KE, MA, MU, MZ, SN, TN, ZA. |
| `stdnum-all` | — | Aggregator depending on every module, and the home of the cross-module tests. |

The full inventory, type by type, is in **[docs/NUMBERS.md](docs/NUMBERS.md)**.
It covers 234 of the 235 number types python-stdnum ships, and adds 39 it does
not.

## Data files

Fourteen types need a prefix database — the IBAN and ISBN registries, the
Austrian postcodes, the Czech, Belgian and New Zealand bank registers, the ISO
10962 CFI classification, the mobile country and network codes, the IEEE MAC
registry, the Chinese administrative divisions, the GS1 application
identifiers, the NACE classification, the Austrian tax offices and the
Indonesian regions.

They are **generated from their upstream registries and never hand-edited**.
The generators live in [`tools/`](tools/README.md), which documents how to
rebuild each file; they have no dependencies, and the two sources published as
spreadsheets are read with the JDK alone, an xlsx being a zip of XML.

## Build

```bash
mvn verify
```

Requires JDK 17+. Runs 19,776 tests.

## Documentation

| | |
|---|---|
| **[Architecture](docs/ARCHITECTURE.md)** | The SPI, the three pillars, how failures are reported and translated, the module graph, what is deliberately absent. |
| **[Testing](docs/TESTING.md)** | How 19,776 tests come from one contract and 15,610 fixture lines, and what the contract guarantees. |
| **[Contributing](docs/CONTRIBUTING.md)** | Adding a number type, step by step — and how the data files, reference links and inventory are maintained. |
| **[Numbers](docs/NUMBERS.md)** | Every type, by module and country. |
| **[Data generators](tools/README.md)** | How to rebuild each `.dat` file from its registry. |
