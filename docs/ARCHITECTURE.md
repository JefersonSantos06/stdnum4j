# Architecture

The library answers one question — *is this string a valid such-and-such
number, and what does it look like written properly* — for 273 kinds of
number. Everything here exists to keep that one question answered the same
way 273 times.

## One interface, and nothing else to learn

A number type is a class that implements `StdNum`:

```java
public interface StdNum {
    Descriptor descriptor();
    String compact(String number);         // minimal representation
    String validate(String number);        // validates AND returns the compact form
    default boolean isValid(String number);
    default Check check(String number);    // exception-free result: Valid | Invalid
    default String format(String number);  // the presentation people expect
}
```

Two methods are abstract; four have defaults written once in the interface.
`isValid` and `check` are `validate` with its failure caught, and `format`
defaults to `validate` — a type whose canonical presentation is its compact
form gets a correct `format` by writing nothing.

The instance is a stateless singleton, by convention `Xyz.INSTANCE` with a
private constructor. There is no factory, no configuration object and no
builder to learn: `Cpf.INSTANCE.validate(input)` is the whole API.

`validate` returning the compact form rather than `void` or `boolean` is the
one decision the rest follows from. It means the common case — *check this,
then store it* — is a single call, and it is what lets `isValid`, `check` and
`format` be defaults instead of 273 hand-written copies.

## Three pillars

Everything the caller sees is one of three things.

**`Descriptor`** says what the number *is*. A record of id, country, short
name, title, description, tags and reference URLs, built through a small
fluent builder and validated in its compact constructor: the id must match
`[a-z0-9]+([._-][a-z0-9]+)*`, the country must be two letters, the tag set is
copied into an immutable `EnumSet`. It is the registry key, the documentation
and the metadata in one object, and it is built once per type as a `static
final` field.

**`Tag`** says what the number is *for*: `TAX`, `VAT`, `EXCISE`, `PERSON`,
`COMPANY`, `BANK`, `PAYMENT`, `FINANCIAL`, `HEALTH`, `VEHICLE`, `POSTAL`,
`TELECOM`, `MEDIA`, `PRODUCT`, `EDUCATION`, `LOCATION`, `OTHER`. A number
carries several — a personal tax number is both `TAX` and `PERSON` — and the
registry indexes them, so "every VAT number this library knows" is one call.

**`Check`** says what happened. A sealed interface with two records:

```java
public sealed interface Check {
    record Valid(String compact) implements Check {}
    record Invalid(ValidationError error, Message message) implements Check {}
}
```

Sealed, and its two cases are records nested inside it, so a `switch` over a
`Check` is exhaustive and the compiler says so. This is the entry point for
code that treats an invalid number as an expected outcome rather than an
exception — a form, a spreadsheet import, a batch job.

## Failing

`validate` throws when the number is wrong. The hierarchy is four classes
deep and no deeper:

```
ValidationException          (unchecked)
├── InvalidFormatException   the characters are wrong
│   └── InvalidLengthException   the characters are right, the count is not
├── InvalidChecksumException the check digit does not close
└── InvalidComponentException a part of the number names something that
                             does not exist (a province, a bank, a date)
```

`InvalidLengthException` extends `InvalidFormatException` because a caller
who wants to catch "malformed" should not have to name both. Each maps to one
of four `ValidationError` enum constants, which is what `Check.Invalid`
carries.

The exceptions are built with `super(message, null, false, false)`: no
suppression, **no stack trace**. Filling in a stack trace costs more than
every validation in this library put together, and the stack of a validation
failure tells nobody anything — the useful information is the number and the
reason, both of which are already there. They are signalling, not error
reporting. This is why `isValid` can be implemented as a caught exception
without apology.

## Saying why, in a language

`getMessage()` and `Check.Invalid.reason()` are English, which is what belongs
in a log. To show a failure to a person, hand it to a `Messages` for their
locale. The library never picks one, holds no global state, and never reads
`Locale.getDefault()`:

```java
Messages pt = Messages.forLocale(Locale.forLanguageTag("pt-BR"));

switch (cpf.check(input)) {
    case Check.Valid v   -> store(v.compact());
    case Check.Invalid i -> reject(pt.render(i));
}
```

A `Message` is a record of *anchor class*, *code*, *English default text* and
*arguments*. Resolution has three tiers, tried in order:

1. the message's own code, in the translation file beside the anchor class —
   so a module ships its translations inside its own jar;
2. `error.<NAME>` for the `ValidationError` — four sentences that every
   translation carries;
3. the English the validator was written with.

A translation is therefore never all-or-nothing: an untranslated code costs a
less specific sentence, not an English one. Interpolation is `{0}`-style and
hand-written, not `MessageFormat`, because `MessageFormat` eats apostrophes —
and apostrophes are unavoidable in the languages this will be translated into.

Translation files are `messages_<language>[_<COUNTRY>].properties`, read as
UTF-8 through `getResourceAsStream` from the anchor's package. There is
deliberately no `messages.properties`: English lives in the Java source, once,
next to the `throw`. The language file is read first and the country file
overlaid on it, so `pt` serves pt-BR, pt-PT and pt-419, and a country file
only carries what actually differs.

Reasons that more than one number gives — a birth date that is not a date, a
serial of all zeroes, a province code that names no province — live once in
`Reasons` as immutable constants, so they are translated once and read the
same everywhere.

## The registry

Implementations register through `StdNumProvider`, a `ServiceLoader` service.
Each module has exactly one provider class listing its types, declared in
`META-INF/services/io.github.jefersonsantos06.stdnum.spi.StdNumProvider`.
`StdNums` loads them once and indexes by id, country and tag:

```java
StdNums.all();                        // every type on the classpath
StdNums.byId("br.cpf");               // Optional<StdNum>
StdNums.byCountry("BR");              // List<StdNum>
StdNums.byCountry(locale);            // the country of a Locale you already hold
StdNums.byCountry("ES", "nif");       // one type of a country, by short name
StdNums.byTag(Tag.VAT);
```

The registry is what makes the dispatching types possible without a cyclic
dependency: `iban` looks up the national IBAN rule for a country through
`StdNums.byCountry(cc, "iban")`, and `vatin` finds the national VAT type the
same way. Neither imports a country class. Put a different set of modules on
the classpath and the same dispatcher covers a different set of countries.

## Modules

| Module | Depends on | Contains |
|---|---|---|
| `stdnum-core` | *(nothing)* | The SPI, the check digit algorithms, the `NumDb` prefix database, the text helpers, the registry. |
| `stdnum-tck` | core | The reusable contract test. Consumed at `test` scope. |
| `stdnum-international` | core | Country-independent numbers, and the `iban`/`vatin`/`eu.vat`/`eu.excise` dispatchers. |
| `stdnum-br` | core | Brazil. |
| `stdnum-eu` | core, **international** | Europe. |
| `stdnum-latam` | core | Latin America. |
| `stdnum-na` | core | North America. |
| `stdnum-apac` | core | Asia-Pacific. |
| `stdnum-africa` | core | Africa. |
| `stdnum-all` | all of the above | No code. An aggregator, and the home of the tests that need every module at once. |

Every regional module depends on `stdnum-core` alone. The single exception is
`stdnum-eu`, which also depends on `stdnum-international`, because a Spanish,
Montenegrin or Norwegian IBAN *is* an IBAN with a national rule on top and
inherits its structure.

**The dependency runs one way.** The international module never imports a
country class; it reaches country types through the registry. That rule is
what keeps the graph acyclic while letting `iban` behave as if it knew about
Spain.

A consumer takes only the regions it needs. Someone validating Brazilian
documents pulls `stdnum-br` and gets `stdnum-core`, not 128 European types.

Every jar declares an `Automatic-Module-Name`
(`io.github.jefersonsantos06.stdnum`, `.br`, `.eu`, …), so consumers on the
module path get named modules rather than filename-derived ones. Nothing here
declares a `module-info`.

## Inside `stdnum-core`

```
io.github.jefersonsantos06.stdnum          StdNums — the registry
                                    .spi   StdNum, Descriptor, Tag, Check,
                                           the exceptions, Message, Messages,
                                           Reasons, StdNumProvider
                                    .algo  Luhn, Damm, Verhoeff, Iso7064,
                                           Mod97, Weighted, Crc16
                                    .numdb NumDb — the prefix database
                                    .text  Strings, Resources
```

`algo` is the arithmetic, and only the arithmetic: `Luhn.calcCheckDigit`,
`Weighted.mod11CheckDigit`, `Iso7064.MOD_11_2.validate`. No class in `algo`
knows what a CPF is, and a validator is usually three lines of `Strings`
cleaning plus one line of `algo`.

`Strings.compact(number, " -./")` strips the separators a number is written
with and is what `compact` is nearly always implemented as. Before stripping
them it folds the Unicode look-alikes that arrive when a number is pasted out
of a formatted document: the twenty-three dash variants become `-`, sixteen
space variants become `' '`, and fullwidth, Arabic-Indic and mathematical
digits become ASCII digits. An en dash where a hyphen was meant is the single
most common way a real input fails for no real reason.

## Data files

Fourteen types need a prefix database: an IBAN needs the BBAN structure of its
country, an ISBN needs the registration group ranges, a MAC address needs the
IEEE registry to name the manufacturer. These ship as `.dat` files beside the
class that reads them, in the format `NumDb` parses: a prefix, then
`key="value"` properties, one entry per line, indented lines nesting under
their parent.

The rule is absolute: **a `.dat` file is generated from its upstream registry
and never hand-edited.** Each one has a generator in [`tools/`](../tools/README.md),
a single-file Java program with no dependencies, and a header naming the source
and version it came from. When a registry publishes new data you re-run the
generator; you do not patch the file. See
[CONTRIBUTING.md](CONTRIBUTING.md#regenerating-a-data-file).

## What is deliberately absent

- **No dependencies.** `stdnum-core` has none, and no module has one beyond
  core. Nothing is pulled in for JSON, XML, HTTP or logging.
- **No global state and no ambient locale.** Nothing reads
  `Locale.getDefault()`, nothing caches a "current" anything. A `Messages` is
  a value you hold.
- **No reflection on the validation path.** The registry uses `ServiceLoader`
  once at first use; after that everything is a plain virtual call.
- **No `module-info`.** Automatic module names are enough today, and a real
  module descriptor would need `opens` on every package that carries a
  translation file.
- **No annotations, no processors, no runtime configuration.** A number type
  is a class with two methods.
