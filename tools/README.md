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

## cz-banks.dat — Czech payment system codes

```bash
curl -L -o kody_bank_CR.csv   https://www.cnb.cz/cs/platebni-styk/.galleries/ucty_kody_bank/download/kody_bank_CR.csv
java tools/GenerateCzBanksDat.java kody_bank_CR.csv   > stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/cz-banks.dat
```

## at-postleitzahl.dat — Austrian postcodes

Source: the regulator's open data API. Only the codes marked addressable are
emitted; the rest are post office box and internal codes. The version stamp
the API carries is copied into the file header.

```bash
curl -L -o plz.json https://data.rtr.at/api/v1/tables/plz.json
java tools/GenerateAtPostleitzahlDat.java plz.json   > stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/at-postleitzahl.dat
```

## be-banks.dat and nz-banks.dat — from spreadsheets

Both registers are published as xlsx. `Xlsx.java` reads one with nothing but
the JDK — an xlsx is a zip of XML — so these generators stay dependency-free
like the rest. Compile it alongside the generator:

```bash
javac -d tools/classes tools/Xlsx.java tools/GenerateBeBanksDat.java
curl -L -o grouped_list_current.xlsx   https://www.nbb.be/doc/be/be/protocol/grouped_list_current.xlsx
java -cp tools/classes GenerateBeBanksDat grouped_list_current.xlsx   > stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/be-banks.dat
```

```bash
javac -d tools/classes tools/Xlsx.java tools/GenerateNzBanksDat.java
curl -L -o BankBranchRegister.xlsx   https://www.paymentsnz.co.nz/resources/industry-registers/bank-branch-register/download/xlsx/
java -cp tools/classes GenerateNzBanksDat BankBranchRegister.xlsx   > stdnum-apac/src/main/resources/io/github/jefersonsantos06/stdnum/apac/nz-banks.dat
```

## cfi.dat — the ISO 10962 classification

The download link is on the SIX group's data standards page and matches
`.*/cfi/.*xlsx`; find it there rather than hard-coding a dated filename.

```bash
javac -d tools/classes tools/Xlsx.java tools/GenerateCfiDat.java
java -cp tools/classes GenerateCfiDat cfi.xlsx   > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/cfi.dat
```

## imsi.dat — mobile country and network codes

Seven Wikipedia pages, which mirror the ITU list. Fetch the raw wikitext of
each and pass them all:

```bash
for p in "Mobile_country_code"          "Mobile_network_codes_in_ITU_region_2xx_(Europe)"          "Mobile_network_codes_in_ITU_region_3xx_(North_America)"          "Mobile_network_codes_in_ITU_region_4xx_(Asia)"          "Mobile_network_codes_in_ITU_region_5xx_(Oceania)"          "Mobile_network_codes_in_ITU_region_6xx_(Africa)"          "Mobile_network_codes_in_ITU_region_7xx_(South_America)"; do
  curl -L -o "$p.wiki" "https://en.wikipedia.org/w/index.php?title=$p&action=raw"
done
java tools/GenerateImsiDat.java *.wiki   > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/imsi.dat
```

## cn-loc.dat — Chinese administrative division codes

Eight Chinese Wikipedia pages, one per numbering region. A county that
existed only for a stretch of years is written with that stretch in front of
it, so a number can be read against its holder's year of birth.

```bash
for i in 1 2 3 4 5 6 7 8; do
  curl -L -o "region$i.wiki"     "https://zh.wikipedia.org/w/index.php?title=$(python -c "import urllib.parse,sys;print(urllib.parse.quote('中华人民共和国行政区划代码_(%s区)' % sys.argv[1]))" $i)&action=raw"
done
java tools/GenerateCnLocDat.java region*.wiki   > stdnum-apac/src/main/resources/io/github/jefersonsantos06/stdnum/apac/cn-loc.dat
```

## gs1-ai.dat — GS1 application identifiers

The identifiers are published as a JSON-LD block inside the reference page.
Consecutive ones that agree on everything but their own number are written as
a single range, which is how the decimal-place identifiers such as 3100 to
3105 are held.

```bash
curl -L -o ai.html https://ref.gs1.org/ai/
java tools/GenerateGs1AiDat.java ai.html   > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/gs1-ai.dat
```

## eu-nace20.dat and eu-nace21.dat — the NACE classification

Eurostat publishes the classification itself through its SDMX API, which is a
better source than a rendering of it. Rev. 2 is `NACE_R2` and Rev. 2.1 is
`NACE_R2_1`.

```bash
base=https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1/codelist/ESTAT
curl -L -o nace21.xml "$base/NACE_R2_1"
java tools/GenerateEuNaceDat.java nace21.xml "Rev. 2.1"   > stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/eu-nace21.dat
curl -L -o nace20.xml "$base/NACE_R2"
java tools/GenerateEuNaceDat.java nace20.xml "Rev. 2"   > stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/eu-nace20.dat
```

## at-fa.dat — Austrian tax office numbers

```bash
curl -L -o abgabenkontonummer.wiki   "https://de.wikipedia.org/w/index.php?title=Abgabenkontonummer&action=raw"
java tools/GenerateAtFaDat.java abgabenkontonummer.wiki   > stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/at-fa.dat
```

## id-loc.dat — Indonesian administrative regions

The bridging service of Badan Pusat Statistik publishes both its own codes
and the Kemendagri ones. A NIK carries the Kemendagri code, which is the one
taken — and the two disagree over Papua, so the distinction matters.

```bash
base=https://sig.bps.go.id/rest-bridging/getwilayah
curl -L -o provinsi.json "$base?level=provinsi"
for p in $(grep -o '"kode_bps":"[0-9]*"' provinsi.json | cut -d'"' -f4); do
  curl -L -o "kab-$p.json" "$base?level=kabupaten&parent=$p"
done
java tools/GenerateIdLocDat.java provinsi.json kab-*.json   > stdnum-apac/src/main/resources/io/github/jefersonsantos06/stdnum/apac/id-loc.dat
```

## Why these are not Maven modules

They run once when a registry publishes new data, not on every build, and
they have no dependencies. Keeping them out of the reactor avoids a module
that produces no artifact. CI still compiles them on every push, so a
refactor cannot break them unnoticed.
