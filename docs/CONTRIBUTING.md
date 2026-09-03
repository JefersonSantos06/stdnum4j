# Adding and maintaining number types

Two halves: [adding a number type](#adding-a-number-type), and
[maintaining what is already here](#maintenance). Read
[ARCHITECTURE.md](ARCHITECTURE.md) first if you have not — this page assumes
the SPI.

---

# Adding a number type

## What a finished type looks like

The CPF is the whole shape in fifty lines. Nothing below is boilerplate you
could have skipped.

```java
public final class Cpf implements StdNum {

    public static final Cpf INSTANCE = new Cpf();

    private static final Descriptor DESCRIPTOR =
            Descriptor.of("br.cpf", "CPF")
                    .country("BR")
                    .title("Cadastro de Pessoas Físicas")
                    .description("Brazilian personal tax identifier: 11 digits with two"
                            + " weighted mod 11 check digits. Numbers consisting of a single"
                            + " repeated digit are rejected.")
                    .tags(Tag.TAX, Tag.PERSON)
                    .references("https://www.gov.br/receitafederal/pt-br/assuntos/meu-cpf",
                            "https://en.wikipedia.org/wiki/CPF_number")
                    .build();

    private static final int[] WEIGHTS_1 = Weighted.descending(10, 9);
    private static final int[] WEIGHTS_2 = Weighted.descending(11, 10);

    private Cpf() {
    }

    @Override
    public Descriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public String compact(String number) {
        return Strings.compact(number, " -.");
    }

    @Override
    public String validate(String number) {
        String n = compact(number);
        if (!Strings.isDigits(n)) {
            throw new InvalidFormatException();
        }
        if (n.length() != 11) {
            throw new InvalidLengthException();
        }
        if (Repeats.allSame(n)) {
            throw new InvalidFormatException(Message.of(Cpf.class, "cpf.repeated",
                    "A CPF consisting of a single repeated digit is not valid."));
        }
        if (!n.endsWith(calcCheckDigits(n.substring(0, 9)))) {
            throw new InvalidChecksumException();
        }
        return n;
    }

    @Override
    public String format(String number) {
        String n = validate(number);
        return n.substring(0, 3) + "." + n.substring(3, 6) + "."
                + n.substring(6, 9) + "-" + n.substring(9);
    }
}
```

## Step 1 — where it goes and what it is called

Pick the module by region: `stdnum-br`, `stdnum-eu`, `stdnum-latam`,
`stdnum-na`, `stdnum-apac`, `stdnum-africa`, or `stdnum-international` for a
number with no country.

The **id** is the registry key and the name of every fixture file. It is the
lower-case ISO 3166-1 alpha-2 country code, a dot, and the number's everyday
short name: `br.cpf`, `es.nif`, `de.vat`. An international number is just the
short name: `iban`, `isbn`, `lei`. It must match
`[a-z0-9]+([._-][a-z0-9]+)*`, which `Descriptor`'s constructor enforces.

The **class name** is the id in PascalCase with the dots removed — `es.nif` →
`EsNif`, `at.uid` → `AtUid`. The Brazilian module uses local names instead
(`Cpf`, `Renavam`, `TituloEleitor`), because there is only one country in it
and the prefix would say nothing.

### If the type is a dispatcher target

`iban`, `vatin` and `eu.excise` live in `stdnum-international` and reach
national rules through the registry, never by importing them. Put the national
rule in its regional module and make it findable — **do not touch the
dispatcher**:

| Dispatcher | Finds the national rule by |
|---|---|
| `iban` | id `<cc>.iban`, and nothing else |
| `vatin`, `eu.vat` | id `<cc>.vat`; failing that, the country's **one** type tagged `Tag.VAT` |
| `eu.excise` | id `<cc>.excise`; failing that, the country's **one** type tagged `Tag.EXCISE` |

The fallback is by *unique* tag. Adding a second `Tag.VAT` type to a country
that has no `<cc>.vat` id makes the lookup ambiguous, and the dispatcher then
rejects every VAT number of that country. Either name one of them `<cc>.vat`,
or do not tag the second one `VAT`.

## Step 2 — write the class

- `public final class`, a `private` constructor, and
  `public static final X INSTANCE`. There is no other way to obtain one.
- The `Descriptor` is a `static final` field, built once at class
  initialisation, never per call.
- `compact` is almost always `Strings.compact(number, "<separators>")`, listing
  every separator the number is written with. `Strings` folds the Unicode
  look-alikes first — dashes, spaces, fullwidth and Arabic-Indic digits — which
  is what makes a number pasted out of a PDF work.
- `validate` **must** return the compact form. Check in order of how basic the
  failure is: character set, then length, then components, then the check
  digit. A caller who sees "invalid checksum" should be able to trust that
  everything before the checksum was fine.
- Use `algo` for the arithmetic — `Luhn`, `Damm`, `Verhoeff`, `Iso7064`,
  `Mod97`, `Weighted`, `Crc16`. If the routine you need is genuinely new, add
  it to `algo` with its own tests; if it is a one-off, keep it private in the
  class. Do not paste a Luhn loop.
- Override `format` **only** if the presentation differs from the compact form.
  The default already validates and returns the compact form, which is the
  correct presentation of a number written without separators.
- Accessors (`getBirthDate`, `toSiren`, `manufacturer`) are `public static`,
  take a single `String`, and **validate first**. A public method that throws
  `NumberFormatException` on a number in our own fixtures is a defect; this has
  happened, and the fixtures exist to catch it.
- Never throw anything but a `ValidationException` subtype out of `validate`,
  `compact` or an accessor. Not on `null`, not on an empty string, not on
  1,024 nines, not on an emoji.

## Step 3 — register it

Add the instance to the module's provider — `BrProvider`, `EuProvider`, and
so on — which is the only class listed in
`META-INF/services/io.github.jefersonsantos06.stdnum.spi.StdNumProvider`. An
unregistered type still validates; it is simply invisible to `StdNums`, to the
dispatchers, and to the registry sweep.

Then **update the counts in `AllRegisteredContractTest`**:

```java
assertEquals(273, StdNums.all().size());
assertEquals(37, StdNums.byCountry("BR").size());
```

Those assertions are deliberate tripwires. A type that is written but not
registered, or registered twice, fails here. Changing the number is a
one-line, on-purpose edit.

## Step 4 — fixtures

In `src/test/resources/fixtures/` of the same module:

| File | Required | Contents |
|---|---|---|
| `<id>.txt` | yes | valid numbers, one per line, as written in the wild |
| `<id>-invalid.txt` | yes | numbers that must be rejected |
| `<id>-format.txt` | only if `format` is overridden | `input<TAB>expected` |
| `<id>-accessor.txt` | only if there are accessors | `method<TAB>input<TAB>expected` |

Keep the masks and separators — `390.533.447-05` and `39053344705` are two
different tests. Record where each sample came from in a `#` comment:

```
# valid CPFs (well-known public test vectors)
390.533.447-05
```

The invalid file should include one number per failure mode the validator has:
too short, wrong characters, a broken check digit, an unknown component. If
you wrote an `if` that throws, there should be a line that reaches it.

## Step 5 — the test class

```java
class EsNifTest extends StdNumContractTest {
    @Override
    protected StdNum subject() {
        return EsNif.INSTANCE;
    }
}
```

That is the whole file for most types. Add `@Test` methods only for what a
generic contract cannot know — a documented presentation, an accessor's edge
case, a rule the specification calls out. See
[TESTING.md](TESTING.md) for what the contract already covers, so you do not
write it twice.

## Step 6 — translations

If you threw a `Message.of(...)` with a code, add that key to
`messages_pt.properties` **in the same package as the anchor class**:

```properties
cpf.repeated = Um CPF formado por um único dígito repetido não é válido.
```

The English stays in the Java source and is never repeated in a properties
file. If the reason is one that other numbers also give — a birth date that is
not a date, an all-zero serial, an unknown province — use the existing
constant from `Reasons` instead of inventing a code, and there is nothing to
translate.

`everyReasonIsTranslated` will fail naming any code you forgot, so this step
is enforced rather than remembered.

## Step 7 — references

`.references(...)` says *where this number is documented*. The link review of
September 2026 replaced 60 of 128 links; these are the rules that came out of it.

**What counts, in order of preference:**

1. the issuing authority's page **about the number** — not its home page, not
   its portal;
2. an official specification, or the OECD TIN sheet for a tax number when the
   authority publishes nothing better;
3. Wikipedia, when the authority publishes only a login wall, a search form or
   a press release. This is not a retreat: for many numbers it is the only
   place the check digit routine is written down.

**What does not count:**

- a ministry or registry **home page** — it names an institution, not a number;
- a **lookup form** — "type a number and we will tell you if it exists"
  verifies one number and documents none;
- a page about a **different number** of the same country. `sk.ico` cited the
  English VAT article, whose Slovak row is IČ DPH, a different number entirely.

**Read the page. Do not just fetch it.** The commonest failure is not a 404;
it is a URL that answers `200` and no longer documents anything, because it
now redirects to a home page. Conversely a `403` or a connection failure is
usually a WAF refusing a scripted fetch, not a dead link — open it in a
browser before replacing it.

## Step 8 — the inventory

Add the type to [NUMBERS.md](NUMBERS.md), in its module's section, under its
country, in id order. That file is maintained by hand and nothing enforces it.

## Checklist

- [ ] class in the right module, `final`, private constructor, `INSTANCE`
- [ ] `Descriptor` with id, country, short name, title, description, tags,
      references
- [ ] `compact` lists every separator
- [ ] `validate` returns the compact form and checks basics first
- [ ] `format` overridden only if the presentation differs
- [ ] registered in the module's provider
- [ ] counts bumped in `AllRegisteredContractTest`
- [ ] `<id>.txt` and `<id>-invalid.txt`, with provenance comments
- [ ] `<id>-format.txt` / `<id>-accessor.txt` if applicable
- [ ] test class extending `StdNumContractTest`
- [ ] every new message code translated in `messages_pt.properties`
- [ ] references read, not merely fetched
- [ ] listed in `docs/NUMBERS.md`
- [ ] `mvn verify` green

---

# Maintenance

## Regenerating a data file

Fourteen types read one of fifteen `.dat` prefix databases. **These files are
generated and never hand-edited** — not to fix one wrong row, not to add one
missing bank. Each has a single-file Java generator in [`tools/`](../tools/README.md) with
the exact `curl` and `java` invocation that produces it, and each file carries
a header naming the source and its version stamp.

When a registry publishes new data:

```bash
curl -L -o RangeMessage.xml https://www.isbn-international.org/export_rangemessage.xml
java tools/GenerateIsbnDat.java RangeMessage.xml \
  > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/isbn.dat
```

then `mvn verify` and read the diff. A regeneration that changes thousands of
rows when the registry announced a small change means the source format moved,
not that the data did.

If a row really is wrong, fix the **generator** — the parser, the filter, the
cross-check — so the next regeneration keeps the fix. `GenerateIbanDat` drops
any entry whose parsed BBAN structure does not add up to the declared length
rather than emitting it; `GenerateBeBanksDat` drops the rows the register
marks as unheld. That is where corrections belong.

The generators are not Maven modules — they run when a registry publishes, not
on every build, and they have no dependencies. CI compiles them on every push
so a refactor cannot break them silently.

## Reviewing reference links

Links rot, and they rot quietly. The rules for what counts as a reference are
in [step 7](#step-7--references) above; the method for a sweep is:

1. fetch all of them and note the status — this finds the outright 404s, which
   are the minority;
2. **read every page that answered**, checking it still documents the number
   the type validates. The failure a status check cannot see is a URL that
   answers 200 and lands on a home page;
3. open by hand anything that returned 403 or nothing at all. That is usually a
   WAF, and the link is fine.

As of the last sweep, **115 of the 273 types cite no reference at all**. That
is a gap, not a break — the natural time to close a bit of it is when you touch
one of those types for another reason.

## Keeping the inventory honest

Two places drift when a type is added:

- `AllRegisteredContractTest`'s hard counts — enforced, the build fails;
- [NUMBERS.md](NUMBERS.md) — not enforced, hand-maintained.

The count in NUMBERS.md's header and the count in `AllRegisteredContractTest`
should always be the same number. If you are ever unsure what is registered,
the registry is the authority:

```java
StdNums.all().forEach(n -> System.out.println(n.descriptor().id()));
```

## Adding a language

Copy `messages_pt.properties` to `messages_<language>.properties` in the same
package, in **every** module that has one — `spi`, `br`, `br/ie`,
`international`, `eu`, `latam`, `na`, `apac`, `africa` — and translate. Then
override `translations()` in the contract test to include the new locale, and
the same coverage check that guards Portuguese will guard it.

Two things to know:

- The four `error.*` keys in `stdnum-core`'s `spi` bundle are the safety net.
  Any invalid number of any type falls back to one of them, so they must be
  true of a "Chave NF-e" and a "Código de barras" alike. Never name a specific
  number in them.
- A country file (`messages_pt_BR.properties`) is **overlaid** on the language
  file, so it should contain only what genuinely differs. Ship the language
  file; add a country file only when a country actually needs different wording.

## Versions and dependencies

`maven.compiler.release` is 17 and CI builds on 17 and 21. The only
dependency is JUnit, declared once in the parent's `dependencyManagement`
through the JUnit BOM. Adding a runtime dependency to any module is a
decision to take deliberately, not a convenience — `stdnum-core` having none
is a feature of the library.

Every module's jar declares an `Automatic-Module-Name` through
`${auto.module.name}`. A new module must set that property, or consumers on
the module path get a name derived from the filename.

## Rules that do not bend

- **python-stdnum is a reference, never a source.** Its behaviour was compared
  against, and its test vectors were imported and re-verified. No code, no data
  file and no wording came from it, and none may.
- **A `.dat` file is generated.** If you find yourself opening one in an
  editor, the answer is in the generator.
- **A validator throws only `ValidationException` subtypes.** Any other
  exception escaping `validate`, `compact` or an accessor is a defect,
  regardless of how strange the input was.
- **`format` refuses what `validate` refuses.** A number is formatted at the
  moment it goes onto an invoice or a screen, which is the worst possible place
  to launder an invalid one.

## Commits

Commit messages here are prose, not conventional-commit prefixes. The subject
says what changed in plain words — *"Sixty references that had stopped pointing
at anything"*, *"Eleven types that need a prefix database, each with its own
generator"* — and the body explains why, including what was found to be wrong
and what was deliberately left undone. A commit that fixes defects names them.
