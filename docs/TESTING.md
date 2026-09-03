# Testing

`mvn verify` runs **19,776 tests**. Almost none of them were written one at a
time. This page explains how that number is reached, so that adding a number
type means adding data rather than adding test code.

## Running them

```bash
mvn verify                            # everything
mvn -pl stdnum-br test                # one module
mvn -pl stdnum-eu test -Dtest=EsNifTest
mvn -pl stdnum-all test               # the cross-module tests only
```

CI runs `mvn -B -ntp verify` on JDK 17 and 21, and then compiles `tools/*.java`
separately — the data file generators are not Maven modules, so this is what
keeps a refactor from breaking them unnoticed.

## The idea: one contract, many types

Every number type answers the same questions, so the assertions are written
once, in `stdnum-tck`, and the types supply data. A test class is usually this
and nothing more:

```java
class EsNifTest extends StdNumContractTest {
    @Override
    protected StdNum subject() {
        return EsNif.INSTANCE;
    }
}
```

That file is 11 lines and runs **22 tests**: the 5 valid and 3 invalid samples
in its fixture files, the 9 garbage inputs every type gets, the translation
check, the two fixed checks, and two skips for the format and accessor files it
does not have. `StdNumContractTest` is mostly `@TestFactory` methods that turn
each sample line into its own `DynamicTest`, named after the sample, so a
failure reads:

```
valid: 39053344705
invalid: 111.111.111-11
garbage: abc😀def
```

`stdnum-tck` is a published artifact, not a test-jar, so anyone writing a
number type outside this repository gets the same contract by depending on it.

## What the contract checks

**For every valid sample**

- `validate(x)` returns a non-empty string and equals `compact(x)`;
- `compact` and `validate` are idempotent — feeding back the compact form
  changes nothing;
- `validate(format(x))` round-trips to the same compact form;
- surrounding whitespace makes no difference (` \t<number>\n ` validates), the
  way a number pasted out of a form or a spreadsheet arrives;
- `isValid` returns true and `check` returns a `Check.Valid` carrying the same
  compact form.

**For every invalid and garbage sample**

- `validate` throws a `ValidationException` subtype — never a
  `NullPointerException`, `IndexOutOfBoundsException`,
  `StringIndexOutOfBoundsException`, `NumberFormatException`,
  `DateTimeParseException` or any other unchecked exception;
- `format` refuses what `validate` refuses, so a presentation is always the
  presentation of a valid number;
- `isValid` is false and `check` returns a `Check.Invalid` with a non-null
  error.

**Always**

- `null` is rejected by `validate` and `compact` with a `ValidationException`,
  not an NPE;
- the `Descriptor` has a non-blank id, short name and title;
- every reason the type gives is translated into every language the library
  ships (see below).

The garbage list is fixed and applied to every type without anyone writing it
out: the empty string, three kinds of whitespace, `!!!`, `%%%`, `----`,
`abc😀def` (a surrogate pair, which is where naive `charAt` loops break), and
1,024 nines (which is where an `int` accumulator overflows). A type that
legitimately accepts one of these overrides `garbageSamples()` and says why in
the override.

## Fixtures

Samples live in `src/test/resources/fixtures/` of the module that owns the
type, named after the `Descriptor` id. One number per line, **kept exactly as
found in the wild** — masks, separators and all. Blank lines and lines
starting with `#` are ignored, and the `#` lines are where a sample's
provenance is recorded.

| File | Holds | Files | Lines |
|---|---|---|---|
| `<id>.txt` | valid numbers | 251 | 13,217 |
| `<id>-invalid.txt` | numbers that must be rejected | 251 | 1,973 |
| `<id>-format.txt` | `input<TAB>expected presentation` | 117 | 186 |
| `<id>-accessor.txt` | `method<TAB>input<TAB>expected` | 52 | 234 |
| | | **671** | **15,610** |

The last two exist because the round-trip check does not pin everything down.
`validate(format(x))` proves `format` produces *something* valid; it does not
prove it produces `16.727.230/0001-97`. And an accessor —
`BeBis.getBirthDate`, `AtUid.calcCheckDigit`, `Mac.manufacturer`,
`FrSiret.toSiren` — is not on the `StdNum` interface at all, so the contract
cannot reach it. An accessor sample names a public static method of the
implementation class taking one string, and compares `String.valueOf(result)`
with the expected text:

```
getBirthDate	75.46.08-980.95	1975-06-08
getGender	85473500193	M
```

A missing fixture file is not a failure — it aborts as a skipped assumption
naming the file it wanted. That is where the 333 skips in a green build come
from, exactly: of the 251 contract test classes, 134 have no format file
(their presentation *is* the compact form) and 199 have no accessor file (they
expose no accessors). Every one of the 251 has both a valid and an invalid
file, which is why neither of those factories ever aborts. A missing **valid**
or **invalid** file is worth noticing; a missing format or accessor file
usually means there is nothing to say.

## Where the samples came from

The corpus is not invented. It is, in order of preference:

1. the worked examples the issuing authority publishes (the SINTEGRA *Roteiro
   de Crítica* pages, Hacienda's cédula documentation, the SWIFT IBAN
   registry's own examples);
2. the vectors python-stdnum tests against, imported mechanically and then
   re-checked here;
3. numbers derived by hand from a published check digit routine, marked as
   such in a `#` comment.

Validation behaviour was compared against python-stdnum number by number over
13,255 vectors and agrees on all of them. `format` agrees on 152 of 169
comparable cases; the divergences are deliberate and each is documented in the
Javadoc of the type that diverges — chiefly that `format` here refuses an
invalid number where the reference regroups it and hands back a well-dressed
string.

That comparison is a one-off oracle, not part of the build: python-stdnum is a
reference, and nothing in this repository depends on it or copies from it.

## The translation check

`everyReasonIsTranslated` walks the invalid and garbage samples, collects the
`Message` each rejection carries, and asserts that a code-bearing message has
a key in the translation file of every language the library ships (today, `pt`).

It reads the `.properties` file directly rather than asking `Messages` how the
sentence came out — and that distinction is the whole point of the test. An
untranslated code falls back to the sentence for its `ValidationError`, which
*is* translated, so rendering can never reveal a missing key. Asking the file
can.

A type that ships translations in another language overrides `translations()`
and the same check covers it.

## Beyond the contract

The contract is the floor. A type with anything interesting about it also gets
hand-written tests, in the same class, for the things a generic contract
cannot know:

```java
@Test
void unicodeSeparatorsAreCleaned() {
    // en dash instead of hyphen, as pasted from formatted documents
    assertEquals("39053344705", Cpf.INSTANCE.validate("390.533.447–05"));
}
```

Two patterns recur:

- **A table test for a family.** `SintegraExamplesTest` holds one worked
  example per Brazilian federative unit, asserts that all 27 are present, and
  mutates each valid example's final check digit to prove it is then rejected.
  This is why the 24 state registrations that have no fixture file of their own
  are still covered.
- **A cross-module test.** Anything needing more than one module lives in
  `stdnum-all`: the IBAN, VATIN, EU VAT and EU excise dispatchers only reach
  their national rules when every regional jar is on the classpath.

## The registry sweep

`AllRegisteredContractTest` in `stdnum-all` iterates the registry without
knowing what is in it, and hits every discovered type with `null` and the
garbage list. A module that registers a fragile validator fails here **even if
it ships no tests of its own**.

It also asserts hard counts — 273 registered types, 37 for Brazil, 10 for
Spain, 7 for France — and that the international types carry no country. Those
numbers are a tripwire: adding a type without registering it, or registering
one twice, fails here. Adding a type therefore means updating this test on
purpose. See
[CONTRIBUTING.md](CONTRIBUTING.md#adding-a-number-type).

## Where the tests are

| Module | Tests | Skipped |
|---|---:|---:|
| `stdnum-core` | 80 | 0 |
| `stdnum-tck` | 28 | 2 |
| `stdnum-br` | 423 | 25 |
| `stdnum-international` | 2,715 | 31 |
| `stdnum-eu` | 6,702 | 180 |
| `stdnum-latam` | 3,677 | 27 |
| `stdnum-na` | 353 | 13 |
| `stdnum-apac` | 3,069 | 35 |
| `stdnum-africa` | 1,269 | 13 |
| `stdnum-all` | 1,460 | 7 |
| **Total** | **19,776** | **333** |

`stdnum-tck` tests itself against a `DummyNumber` that exists only to prove
the contract catches what it claims to, and `RegistryIntegrationTest` proves a
type registered through `ServiceLoader` is found by `StdNums`.
