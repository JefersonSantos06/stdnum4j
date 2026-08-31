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
| `stdnum-core` | SPI, exceptions, check digit algorithms (Luhn, Damm, Verhoeff, ISO 7064, weighted mod 11), registry. Zero dependencies. |
| `stdnum-tck`  | Reusable JUnit 5 contract tests for `StdNum` implementations. |

Country modules (`stdnum-br` first) will follow.

## Build

```
mvn verify
```

Requires JDK 17+.
