# Data file generators

The library ships two prefix databases that are **generated, never
hand-edited**. Each one is produced by a single-file Java program here, so
the data can always be traced back to its source and rebuilt from it.

Run them with `java <file>` — Java 11 and later compile a single source
file on the fly, so there is nothing to build first.

## isbn.dat — registration group and publisher ranges

Source: the official range message published by ISBN International.

```bash
curl -L -o RangeMessage.xml https://www.isbn-international.org/export_rangemessage.xml
java tools/GenerateIsbnDat.java RangeMessage.xml \
  > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/isbn.dat
```

The message serial and date are copied into the file header, so the
version of the data in the repository is always identifiable.

## iban.dat — country registry and BBAN structures

The authoritative source is the SWIFT IBAN Registry, whose download is
behind a bot wall. The generator therefore reads the Wikipedia article
that mirrors it, and cross-checks every entry: a record whose parsed
structure does not add up to the length the table declares is reported and
dropped rather than emitted.

```bash
curl -L -A "Mozilla/5.0" -o iban.html \
  https://en.wikipedia.org/wiki/International_Bank_Account_Number
java tools/GenerateIbanDat.java iban.html \
  > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/iban.dat
```

If the SWIFT registry ever becomes fetchable, replace this generator with
one that reads it directly — the output format is what matters, not the
source.

## Why these are not Maven modules

They run once when a registry publishes new data, not on every build, and
they have no dependencies. Keeping them out of the reactor avoids a module
that produces no artifact. CI still compiles them on every push, so a
refactor cannot break them unnoticed.
