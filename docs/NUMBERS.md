# Números

**451 tipos**, em 183 países, mais 30 formatos independentes de país.

Este inventário é mantido à mão. Ao adicionar um tipo, adicione-o aqui também —
veja [CONTRIBUTING.md](CONTRIBUTING.md#passo-8--o-inventário). Se algum dia ele
divergir do código, quem manda é o código:

```java
StdNums.all().forEach(n -> System.out.println(n.descriptor().id()));
```

## Resumo

| Módulo | Tipos | Países |
|---|---:|---:|
| [`stdnum4j-international`](#independentes-de-país--stdnum4j-international-30) | 30 | — |
| [`stdnum4j-br`](#brasil--stdnum4j-br-37) | 37 | 1 |
| [`stdnum4j-eu`](#europa--stdnum4j-eu-128) | 128 | 44 |
| [`stdnum4j-latam`](#américa-latina--stdnum4j-latam-23) | 23 | 14 |
| [`stdnum4j-na`](#américa-do-norte--stdnum4j-na-10) | 10 | 2 |
| [`stdnum4j-apac`](#ásia-pacífico--stdnum4j-apac-33) | 33 | 17 |
| [`stdnum4j-africa`](#áfrica--stdnum4j-africa-12) | 12 | 11 |
| [`stdnum4j-postal`](#códigos-postais--stdnum4j-postal-178) | 178 | 178 |
| **Total** | **451** | **183** |

Os países do `stdnum4j-postal` se sobrepõem aos dos módulos regionais; distintos,
são 183.

## Diante do python-stdnum

O python-stdnum 2.2 traz **235 tipos de número** (236 módulos, um dos quais,
`iso9362`, é apenas um apelido de `bic`).

- **234 estão implementados aqui.**
- **1 não está:**
  - [ ] `isil` — International Standard Identifier for Libraries. Deixado de
        fora de propósito: é uma sintaxe com um registro de prefixos, não um
        número com dígito verificador, e nada aqui precisa dele.
- **217 tipos daqui não têm equivalente no python-stdnum:** os 178 códigos
  postais gerados, os 35 brasileiros
  (as inscrições estaduais das 27 unidades federativas, o PIS/PASEP, o CNS, o
  título de eleitor, o RENAVAM, a chave de acesso da NF-e, o boleto FEBRABAN
  nas suas duas formas e o Pix BR Code), mais `cz.ico`, `sk.ico`, `om.vat` e
  `upi`.

Onde a referência nomeia o módulo de outro jeito, o id aqui segue a grafia
natural: `in_.pan` → `in.pan`, `is_.kennitala` → `is.kennitala`, `jp.in_` →
`jp.in` (o Python acrescenta um sublinhado a palavras reservadas), `gb.sedol` →
`sedol` (o SEDOL não é um formato só britânico), e `iso9362` é simplesmente
`bic`.

## A lista

Toda caixa abaixo está marcada: é o que a biblioteca valida hoje. A única caixa
desmarcada do projeto é o `isil`, acima.

O nome de cada tipo é o nome real do número no idioma de quem o emite — é assim
que ele aparece no `Descriptor`, e não é traduzido.

### Independentes de país — `stdnum4j-international` (30)

- [x] `bic` — BIC — Business Identifier Code
- [x] `bitcoin` — Bitcoin address
- [x] `casrn` — CAS RN — CAS Registry Number
- [x] `cfi` — CFI — Classification of Financial Instruments code
- [x] `cusip` — CUSIP — CUSIP number
- [x] `ean` — EAN — International Article Number
- [x] `eu.excise` — Excise number — European Union excise number
- [x] `eu.vat` — EU VAT — European Union VAT number
- [x] `figi` — FIGI — Financial Instrument Global Identifier
- [x] `grid` — GRid — Global Release Identifier
- [x] `gs1_128` — GS1-128 — GS1-128 element string
- [x] `iban` — IBAN — International Bank Account Number
- [x] `imei` — IMEI — International Mobile Equipment Identity
- [x] `imo` — IMO — IMO ship identification number
- [x] `imsi` — IMSI — International Mobile Subscriber Identity
- [x] `isan` — ISAN — International Standard Audiovisual Number
- [x] `isbn` — ISBN — International Standard Book Number
- [x] `isin` — ISIN — International Securities Identification Number
- [x] `ismn` — ISMN — International Standard Music Number
- [x] `isni` — ISNI — International Standard Name Identifier
- [x] `iso11649` — Creditor Reference — ISO 11649 structured creditor reference
- [x] `iso6346` — Container code — ISO 6346 container identification
- [x] `isrc` — ISRC — International Standard Recording Code
- [x] `issn` — ISSN — International Standard Serial Number
- [x] `lei` — LEI — Legal Entity Identifier
- [x] `mac` — MAC address — Media access control address
- [x] `meid` — MEID — Mobile Equipment Identifier
- [x] `sedol` — SEDOL — Stock Exchange Daily Official List number
- [x] `upi` — UPI — Unique Product Identifier
- [x] `vatin` — VATIN — VAT identification number

### Brasil — `stdnum4j-br` (37)

- [x] `br.boleto-barras` — Código de barras — Código de barras de cobrança (FEBRABAN)
- [x] `br.boleto-linha` — Linha digitável — Linha digitável de cobrança (FEBRABAN)
- [x] `br.cnpj` — CNPJ — Cadastro Nacional da Pessoa Jurídica
- [x] `br.cns` — CNS — Cartão Nacional de Saúde
- [x] `br.cpf` — CPF — Cadastro de Pessoas Físicas
- [x] `br.ie.ac` — IE AC — Inscrição Estadual - Acre
- [x] `br.ie.al` — IE AL — Inscrição Estadual - Alagoas
- [x] `br.ie.am` — IE AM — Inscrição Estadual - Amazonas
- [x] `br.ie.ap` — IE AP — Inscrição Estadual - Amapá
- [x] `br.ie.ba` — IE BA — Inscrição Estadual - Bahia
- [x] `br.ie.ce` — IE CE — Inscrição Estadual - Ceará
- [x] `br.ie.df` — IE DF — Inscrição Estadual - Distrito Federal
- [x] `br.ie.es` — IE ES — Inscrição Estadual - Espírito Santo
- [x] `br.ie.go` — IE GO — Inscrição Estadual - Goiás
- [x] `br.ie.ma` — IE MA — Inscrição Estadual - Maranhão
- [x] `br.ie.mg` — IE MG — Inscrição Estadual - Minas Gerais
- [x] `br.ie.ms` — IE MS — Inscrição Estadual - Mato Grosso do Sul
- [x] `br.ie.mt` — IE MT — Inscrição Estadual - Mato Grosso
- [x] `br.ie.pa` — IE PA — Inscrição Estadual - Pará
- [x] `br.ie.pb` — IE PB — Inscrição Estadual - Paraíba
- [x] `br.ie.pe` — IE PE — Inscrição Estadual - Pernambuco
- [x] `br.ie.pi` — IE PI — Inscrição Estadual - Piauí
- [x] `br.ie.pr` — IE PR — Inscrição Estadual - Paraná
- [x] `br.ie.rj` — IE RJ — Inscrição Estadual - Rio de Janeiro
- [x] `br.ie.rn` — IE RN — Inscrição Estadual - Rio Grande do Norte
- [x] `br.ie.ro` — IE RO — Inscrição Estadual - Rondônia
- [x] `br.ie.rr` — IE RR — Inscrição Estadual - Roraima
- [x] `br.ie.rs` — IE RS — Inscrição Estadual - Rio Grande do Sul
- [x] `br.ie.sc` — IE SC — Inscrição Estadual - Santa Catarina
- [x] `br.ie.se` — IE SE — Inscrição Estadual - Sergipe
- [x] `br.ie.sp` — IE SP — Inscrição Estadual - São Paulo
- [x] `br.ie.to` — IE TO — Inscrição Estadual - Tocantins
- [x] `br.nfe` — Chave NF-e — Chave de Acesso da NF-e
- [x] `br.pis` — PIS/PASEP — PIS/PASEP - Número de Inscrição do Trabalhador
- [x] `br.pix` — Pix BR Code — Pix BR Code (EMV QRCPS)
- [x] `br.renavam` — RENAVAM — Registro Nacional de Veículos Automotores
- [x] `br.titulo-eleitor` — Título de Eleitor

### Europa — `stdnum4j-eu` (128)

**Sem país definido**

- [x] `eu.at_02` — SEPA Creditor Identifier — SEPA Identifier of the Creditor (AT-02)
- [x] `eu.banknote` — Banknote serial — Euro banknote serial number
- [x] `eu.ecnumber` — EC number — European Community number
- [x] `eu.eic` — EIC — European Energy Identification Code
- [x] `eu.nace` — NACE — Statistical Classification of Economic Activities
- [x] `eu.oss` — OSS — EU One Stop Shop VAT number

**Albânia (AL)**

- [x] `al.nipt` — NIPT — Numri i Identifikimit për Personin e Tatueshëm

**Alemanha (DE)**

- [x] `de.handelsregisternummer` — Handelsregisternummer — Deutsche Handelsregisternummer
- [x] `de.idnr` — IdNr — Steuerliche Identifikationsnummer
- [x] `de.leitweg` — Leitweg-ID — Deutsche Leitweg-ID
- [x] `de.stnr` — Steuernummer — Deutsche Steuernummer
- [x] `de.vat` — USt-IdNr — Umsatzsteuer-Identifikationsnummer
- [x] `de.wkn` — WKN — Wertpapierkennnummer

**Andorra (AD)**

- [x] `ad.nrt` — NRT — Número de Registre Tributari

**Azerbaijão (AZ)**

- [x] `az.voen` — VÖEN — Vergi ödəyicisinin eyniləşdirmə nömrəsi

**Bielorrússia (BY)**

- [x] `by.unp` — UNP — Belarusian taxpayer number

**Bulgária (BG)**

- [x] `bg.egn` — ЕГН — Единен граждански номер
- [x] `bg.pnf` — ЛНЧ — Личен номер на чужденец
- [x] `bg.vat` — ДДС — Идентификационен номер по ДДС

**Bélgica (BE)**

- [x] `be.bis` — BIS-nummer — Belgisch BIS-nummer
- [x] `be.eid` — eID — Belgisch eID-kaartnummer
- [x] `be.iban` — IBAN — Belgisch internationaal bankrekeningnummer
- [x] `be.nn` — Rijksregisternummer — Belgisch Rijksregisternummer
- [x] `be.ogm_vcs` — OGM — Belgisch gestructureerde mededeling
- [x] `be.ssn` — INSZ — Belgisch identificatienummer van de sociale zekerheid
- [x] `be.vat` — Ondernemingsnummer — Ondernemingsnummer (BTW, TVA, NWSt)

**Chipre (CY)**

- [x] `cy.vat` — ΦΠΑ — Αριθμός Εγγραφής Φ.Π.Α.

**Croácia (HR)**

- [x] `hr.oib` — OIB — Osobni identifikacijski broj

**Dinamarca (DK)**

- [x] `dk.cpr` — CPR — CPR-nummer (personnummer)
- [x] `dk.cvr` — CVR — Momsregistreringsnummer (CVR)

**Eslováquia (SK)**

- [x] `sk.dph` — IČ DPH — Identifikačné číslo pre daň z pridanej hodnoty
- [x] `sk.ico` — ICO — Identifikacne cislo organizacie
- [x] `sk.rc` — RČ — Rodné číslo

**Eslovênia (SI)**

- [x] `si.ddv` — ID za DDV — Davčna številka
- [x] `si.emso` — EMSO — Enotna maticna stevilka obcana
- [x] `si.maticna` — Maticna stevilka — Slovenska maticna stevilka

**Espanha (ES)**

- [x] `es.cae` — CAE — Codigo de Actividad y Establecimiento
- [x] `es.ccc` — CCC — Codigo Cuenta Cliente
- [x] `es.cif` — CIF — Código de Identificación Fiscal
- [x] `es.cups` — CUPS — Codigo Unificado de Punto de Suministro
- [x] `es.dni` — DNI — Documento Nacional de Identidad
- [x] `es.iban` — IBAN — Numero de cuenta bancaria internacional espanol
- [x] `es.nie` — NIE — Número de Identificación de Extranjero
- [x] `es.nif` — NIF — Número de Identificación Fiscal
- [x] `es.postal_code` — Codigo postal — Codigo postal espanol
- [x] `es.referenciacatastral` — Referencia catastral — Referencia catastral espanola

**Estônia (EE)**

- [x] `ee.ik` — Isikukood — Eesti isikukood
- [x] `ee.kmkr` — KMKR — Käibemaksukohuslase number
- [x] `ee.registrikood` — Registrikood — Eesti registrikood

**Finlândia (FI)**

- [x] `fi.alv` — ALV nro — Arvonlisäveronumero
- [x] `fi.associationid` — Rekisterinumero — Suomalainen yhdistysrekisterinumero
- [x] `fi.hetu` — HETU — Suomalainen henkilotunnus
- [x] `fi.veronumero` — Veronumero — Suomalainen veronumero
- [x] `fi.ytunnus` — Y-tunnus

**França (FR)**

- [x] `fr.accise` — Numero d'accise — Numero d'accise francais
- [x] `fr.nif` — NIF — Numero fiscal de reference
- [x] `fr.nir` — NIR — Numero d'inscription au repertoire
- [x] `fr.rcs` — RCS — Numero RCS
- [x] `fr.siren` — SIREN — Système d'Identification du Répertoire des Entreprises
- [x] `fr.siret` — SIRET — Système d'Identification du Répertoire des ETablissements
- [x] `fr.tva` — TVA — Numéro d'identification à la taxe sur la valeur ajoutée

**Grécia (GR)**

- [x] `gr.amka` — AMKA — Arithmos Mitroou Koinonikis Asfalisis
- [x] `gr.vat` — ΑΦΜ — Αριθμός Φορολογικού Μητρώου

**Hungria (HU)**

- [x] `hu.anum` — ANUM — Közösségi adószám

**Ilhas Faroé (FO)**

- [x] `fo.vn` — V-number — Vinnutal

**Irlanda (IE)**

- [x] `ie.pps` — PPS No — Personal Public Service Number
- [x] `ie.vat` — VAT — Irish tax reference number

**Islândia (IS)**

- [x] `is.kennitala` — Kennitala
- [x] `is.vsk` — VSK — Virdisaukaskattur

**Itália (IT)**

- [x] `it.aic` — AIC — Autorizzazione allImmissione in Commercio
- [x] `it.codicefiscale` — Codice fiscale — Codice fiscale italiano
- [x] `it.iva` — Partita IVA

**Letônia (LV)**

- [x] `lv.pvn` — PVN — Pievienotās vērtības nodokļa numurs

**Liechtenstein (LI)**

- [x] `li.peid` — PEID — Liechtenstein PEID

**Lituânia (LT)**

- [x] `lt.asmens` — Asmens kodas — Lietuvos asmens kodas
- [x] `lt.pvm` — PVM — Pridėtinės vertės mokestis mokėtojo kodas

**Luxemburgo (LU)**

- [x] `lu.tva` — TVA — Numéro d'identification à la taxe sur la valeur ajoutée

**Macedônia do Norte (MK)**

- [x] `mk.edb` — ЕДБ — Единствен даночен број

**Malta (MT)**

- [x] `mt.vat` — VAT — Maltese VAT number

**Moldávia (MD)**

- [x] `md.idno` — IDNO — Numărul de identificare de stat

**Montenegro (ME)**

- [x] `me.iban` — IBAN — Crnogorski medunarodni broj racuna
- [x] `me.pib` — PIB — Poreski identifikacioni broj

**Mônaco (MC)**

- [x] `mc.tva` — TVA — Numero d'identification a la taxe sur la valeur ajoutee

**Noruega (NO)**

- [x] `no.fodselsnummer` — Fodselsnummer — Norsk fodselsnummer
- [x] `no.iban` — IBAN — Norsk internasjonalt kontonummer
- [x] `no.kontonr` — Kontonummer — Norsk kontonummer
- [x] `no.mva` — MVA — Merverdiavgift
- [x] `no.orgnr` — Orgnr — Organisasjonsnummer

**Países Baixos (NL)**

- [x] `nl.brin` — BRIN — Basisregistratie Instellingen
- [x] `nl.bsn` — BSN — Burgerservicenummer
- [x] `nl.btw` — Btw-nummer — Btw-identificatienummer
- [x] `nl.identiteitskaartnummer` — Documentnummer — Nederlands identiteitskaart- of paspoortnummer
- [x] `nl.onderwijsnummer` — Onderwijsnummer — Nederlands onderwijsnummer
- [x] `nl.postcode` — Postcode — Nederlandse postcode

**Polônia (PL)**

- [x] `pl.nip` — NIP — Numer Identyfikacji Podatkowej
- [x] `pl.pesel` — PESEL — Powszechny Elektroniczny System Ewidencji Ludności
- [x] `pl.regon` — REGON — Rejestr Gospodarki Narodowej

**Portugal (PT)**

- [x] `pt.cc` — CC — Numero de Cartao de Cidadao
- [x] `pt.nif` — NIF — Número de Identificação Fiscal

**Reino Unido (GB)**

- [x] `gb.nhs` — NHS number — United Kingdom National Health Service number
- [x] `gb.upn` — UPN — Unique Pupil Number
- [x] `gb.utr` — UTR — Unique Taxpayer Reference
- [x] `gb.vat` — VAT — United Kingdom VAT registration number

**Romênia (RO)**

- [x] `ro.cf` — CF — Cod de înregistrare în scopuri de TVA
- [x] `ro.cnp` — CNP — Cod Numeric Personal
- [x] `ro.cui` — CUI — Codul Unic de Înregistrare
- [x] `ro.onrc` — ONRC — Numarul de ordine in registrul comertului

**San Marino (SM)**

- [x] `sm.coe` — COE — Codice Operatore Economico

**Suécia (SE)**

- [x] `se.orgnr` — Orgnr — Organisationsnummer
- [x] `se.personnummer` — Personnummer — Svenskt personnummer
- [x] `se.postnummer` — Postnummer — Svenskt postnummer
- [x] `se.vat` — Moms — Momsregistreringsnummer

**Suíça (CH)**

- [x] `ch.esr` — ESR — Einzahlungsschein mit Referenznummer
- [x] `ch.ssn` — AHV-Nr. — Schweizer Sozialversicherungsnummer
- [x] `ch.uid` — UID — Unternehmens-Identifikationsnummer
- [x] `ch.vat` — MWST/TVA/IVA — Mehrwertsteuernummer

**Sérvia (RS)**

- [x] `rs.pib` — PIB — Порески идентификациони број

**Tchéquia (CZ)**

- [x] `cz.bankaccount` — Cislo uctu — Ceske cislo bankovniho uctu
- [x] `cz.dic` — DIČ — Daňové identifikační číslo
- [x] `cz.ico` — ICO — Identifikacni cislo osoby
- [x] `cz.rc` — RČ — Rodné číslo

**Ucrânia (UA)**

- [x] `ua.edrpou` — ЄДРПОУ — Єдиний державний реєстр підприємств та організацій України
- [x] `ua.rntrc` — RNTRC — Reyestratsiynyi nomer oblikovoyi kartky platnyka podatkiv

**Áustria (AT)**

- [x] `at.businessid` — Firmenbuchnummer — Osterreichische Firmenbuchnummer
- [x] `at.postleitzahl` — Postleitzahl — Osterreichische Postleitzahl
- [x] `at.tin` — Abgabenkontonummer — Osterreichische Abgabenkontonummer
- [x] `at.uid` — UID — Umsatzsteuer-Identifikationsnummer
- [x] `at.vnr` — VNR — Osterreichische Sozialversicherungsnummer

### América Latina — `stdnum4j-latam` (23)

**Argentina (AR)**

- [x] `ar.cbu` — CBU — Clave Bancaria Uniforme
- [x] `ar.cuit` — CUIT — Código Único de Identificación Tributaria
- [x] `ar.dni` — DNI — Documento Nacional de Identidad

**Chile (CL)**

- [x] `cl.rut` — RUT — Rol Único Tributario

**Colômbia (CO)**

- [x] `co.nit` — NIT — Número De Identificación Tributaria

**Costa Rica (CR)**

- [x] `cr.cpf` — CPF — Cédula de Persona Física
- [x] `cr.cpj` — CPJ — Cédula de Persona Jurídica
- [x] `cr.cr` — CR — Cedula de Residencia

**Cuba (CU)**

- [x] `cu.ni` — NI — Número de identidad

**El Salvador (SV)**

- [x] `sv.nit` — NIT — Número de Identificación Tributaria

**Equador (EC)**

- [x] `ec.ci` — CI — Cédula de identidad
- [x] `ec.ruc` — RUC — Registro Unico de Contribuyentes

**Guatemala (GT)**

- [x] `gt.nit` — NIT — Número de Identificación Tributaria

**México (MX)**

- [x] `mx.curp` — CURP — Clave Unica de Registro de Poblacion
- [x] `mx.rfc` — RFC — Registro Federal de Contribuyentes

**Paraguai (PY)**

- [x] `py.ruc` — RUC — Registro Único de Contribuyentes

**Peru (PE)**

- [x] `pe.cui` — CUI — Codigo Unico de Identificacion
- [x] `pe.ruc` — RUC — Registro Único de Contribuyentes

**República Dominicana (DO)**

- [x] `do.cedula` — Cédula — Cédula de identidad y electoral
- [x] `do.ncf` — NCF — Numero de Comprobante Fiscal
- [x] `do.rnc` — RNC — Registro Nacional del Contribuyente

**Uruguai (UY)**

- [x] `uy.rut` — RUT — Registro Único Tributario

**Venezuela (VE)**

- [x] `ve.rif` — RIF — Registro de Identificación Fiscal

### América do Norte — `stdnum4j-na` (10)

**Canadá (CA)**

- [x] `ca.bc_phn` — PHN — British Columbia Personal Health Number
- [x] `ca.bn` — BN — Business Number
- [x] `ca.sin` — SIN — Social Insurance Number

**Estados Unidos (US)**

- [x] `us.atin` — ATIN — Adoption Taxpayer Identification Number
- [x] `us.ein` — EIN — Employer Identification Number
- [x] `us.itin` — ITIN — Individual Taxpayer Identification Number
- [x] `us.ptin` — PTIN — Preparer Tax Identification Number
- [x] `us.rtn` — RTN — Routing Transit Number
- [x] `us.ssn` — SSN — Social Security Number
- [x] `us.tin` — TIN — Taxpayer Identification Number

### Ásia-Pacífico — `stdnum4j-apac` (33)

**Austrália (AU)**

- [x] `au.abn` — ABN — Australian Business Number
- [x] `au.acn` — ACN — Australian Company Number
- [x] `au.tfn` — TFN — Tax File Number

**China (CN)**

- [x] `cn.ric` — RIC — Chinese Resident Identity Card number
- [x] `cn.uscc` — USCC — Unified Social Credit Code

**Coreia do Sul (KR)**

- [x] `kr.brn` — BRN — Korean Business Registration Number
- [x] `kr.rrn` — RRN — Resident registration number (주민등록번호)

**Indonésia (ID)**

- [x] `id.nik` — NIK — Nomor Induk Kependudukan
- [x] `id.npwp` — NPWP — Nomor Pokok Wajib Pajak

**Israel (IL)**

- [x] `il.hp` — H.P. — Israeli company number
- [x] `il.idnr` — Mispar Zehut — Israeli identity number

**Japão (JP)**

- [x] `jp.cn` — 法人番号 — Corporate Number (hōjin bangō)
- [x] `jp.in` — My Number — Japanese Individual Number

**Malásia (MY)**

- [x] `my.nric` — NRIC No. — National Registration Identity Card Number

**Nova Zelândia (NZ)**

- [x] `nz.bankaccount` — Bank account number — New Zealand bank account number
- [x] `nz.ird` — IRD — Inland Revenue Department number

**Omã (OM)**

- [x] `om.vat` — VAT — Oman VAT identification number

**Paquistão (PK)**

- [x] `pk.cnic` — CNIC — Computerised National Identity Card number

**Rússia (RU)**

- [x] `ru.inn` — ИНН — Идентификационный номер налогоплательщика
- [x] `ru.ogrn` — OGRN — Osnovnoy gosudarstvennyy registratsionnyy nomer

**Singapura (SG)**

- [x] `sg.uen` — UEN — Unique Entity Number

**Tailândia (TH)**

- [x] `th.moa` — MOA — Memorandum of Association Number
- [x] `th.pin` — PIN — Thailand Personal Identification Number
- [x] `th.tin` — TIN — Thai Tax Identification Number

**Taiwan (TW)**

- [x] `tw.ubn` — UBN — Unified Business Number (統一編號)

**Turquia (TR)**

- [x] `tr.tckimlik` — T.C. Kimlik No. — Türkiye Cumhuriyeti Kimlik Numarası
- [x] `tr.vkn` — VKN — Vergi Kimlik Numarasi

**Vietnã (VN)**

- [x] `vn.mst` — MST — Mã số thuế

**Índia (IN)**

- [x] `in.aadhaar` — Aadhaar
- [x] `in.epic` — EPIC — Electoral Photo Identity Card number
- [x] `in.gstin` — GSTIN — Goods and Services Tax identification number
- [x] `in.pan` — PAN — Permanent Account Number
- [x] `in.vid` — VID — Indian Virtual ID

### África — `stdnum4j-africa` (12)

**Argélia (DZ)**

- [x] `dz.nif` — NIF — Numéro d'Identification Fiscale

**Egito (EG)**

- [x] `eg.tn` — TN — Egyptian Tax Registration Number

**Gana (GH)**

- [x] `gh.tin` — TIN — Ghana Taxpayer Identification Number

**Guiné (GN)**

- [x] `gn.nifp` — NIFp — Numero d'Identification Fiscale permanent

**Marrocos (MA)**

- [x] `ma.ice` — ICE — Identifiant Commun de l'Entreprise

**Maurício (MU)**

- [x] `mu.nid` — NID — Mauritian National Identity number

**Moçambique (MZ)**

- [x] `mz.nuit` — NUIT — Número Único de Identificação Tributária

**Quênia (KE)**

- [x] `ke.pin` — KRA PIN — Kenya Revenue Authority Personal Identification Number

**Senegal (SN)**

- [x] `sn.ninea` — NINEA — Numéro d'Identification National des Entreprises et Associations

**Tunísia (TN)**

- [x] `tn.mf` — Matricule fiscal — Matricule fiscal tunisien

**África do Sul (ZA)**

- [x] `za.idnr` — ID number — South African Identity Document number
- [x] `za.tin` — TIN — South African Tax Identification Number

### Códigos postais — `stdnum4j-postal` (178)

Um tipo por país ou território para o qual os metadados de endereço do Google
(os da libaddressinput, dados CC BY 4.0) documentam um padrão — todos gerados
de `postal-codes.dat`, nunca escritos à mão. AT, ES, NL e SE têm tipo próprio
no `stdnum4j-eu` e ficam de fora daqui.

- [x] `ac.postal_code` — Postal code — Ascension Island postal code
- [x] `ad.postal_code` — Postal code — Andorra postal code
- [x] `af.postal_code` — Postal code — Afghanistan postal code
- [x] `ai.postal_code` — Postal code — Anguilla postal code
- [x] `al.postal_code` — Postal code — Albania postal code
- [x] `am.postal_code` — Postal code — Armenia postal code
- [x] `ar.postal_code` — Postal code — Argentina postal code
- [x] `as.postal_code` — ZIP code — American Samoa ZIP code
- [x] `au.postal_code` — Postal code — Australia postal code
- [x] `ax.postal_code` — Postal code — Åland Islands postal code
- [x] `az.postal_code` — Postal code — Azerbaijan postal code
- [x] `ba.postal_code` — Postal code — Bosnia & Herzegovina postal code
- [x] `bb.postal_code` — Postal code — Barbados postal code
- [x] `bd.postal_code` — Postal code — Bangladesh postal code
- [x] `be.postal_code` — Postal code — Belgium postal code
- [x] `bg.postal_code` — Postal code — Bulgaria postal code
- [x] `bh.postal_code` — Postal code — Bahrain postal code
- [x] `bl.postal_code` — Postal code — St. Barthélemy postal code
- [x] `bm.postal_code` — Postal code — Bermuda postal code
- [x] `bn.postal_code` — Postal code — Brunei postal code
- [x] `br.postal_code` — Postal code — Brazil postal code
- [x] `bt.postal_code` — Postal code — Bhutan postal code
- [x] `by.postal_code` — Postal code — Belarus postal code
- [x] `ca.postal_code` — Postal code — Canada postal code
- [x] `cc.postal_code` — Postal code — Cocos (Keeling) Islands postal code
- [x] `ch.postal_code` — Postal code — Switzerland postal code
- [x] `cl.postal_code` — Postal code — Chile postal code
- [x] `cn.postal_code` — Postal code — China postal code
- [x] `co.postal_code` — Postal code — Colombia postal code
- [x] `cr.postal_code` — Postal code — Costa Rica postal code
- [x] `cu.postal_code` — Postal code — Cuba postal code
- [x] `cv.postal_code` — Postal code — Cape Verde postal code
- [x] `cx.postal_code` — Postal code — Christmas Island postal code
- [x] `cy.postal_code` — Postal code — Cyprus postal code
- [x] `cz.postal_code` — Postal code — Czechia postal code
- [x] `de.postal_code` — Postal code — Germany postal code
- [x] `dk.postal_code` — Postal code — Denmark postal code
- [x] `do.postal_code` — Postal code — Dominican Republic postal code
- [x] `dz.postal_code` — Postal code — Algeria postal code
- [x] `ec.postal_code` — Postal code — Ecuador postal code
- [x] `ee.postal_code` — Postal code — Estonia postal code
- [x] `eg.postal_code` — Postal code — Egypt postal code
- [x] `eh.postal_code` — Postal code — Western Sahara postal code
- [x] `et.postal_code` — Postal code — Ethiopia postal code
- [x] `fi.postal_code` — Postal code — Finland postal code
- [x] `fk.postal_code` — Postal code — Falkland Islands postal code
- [x] `fm.postal_code` — ZIP code — Micronesia ZIP code
- [x] `fo.postal_code` — Postal code — Faroe Islands postal code
- [x] `fr.postal_code` — Postal code — France postal code
- [x] `gb.postal_code` — Postal code — United Kingdom postal code
- [x] `ge.postal_code` — Postal code — Georgia postal code
- [x] `gf.postal_code` — Postal code — French Guiana postal code
- [x] `gg.postal_code` — Postal code — Guernsey postal code
- [x] `gi.postal_code` — Postal code — Gibraltar postal code
- [x] `gl.postal_code` — Postal code — Greenland postal code
- [x] `gn.postal_code` — Postal code — Guinea postal code
- [x] `gp.postal_code` — Postal code — Guadeloupe postal code
- [x] `gr.postal_code` — Postal code — Greece postal code
- [x] `gs.postal_code` — Postal code — South Georgia & South Sandwich Islands postal code
- [x] `gt.postal_code` — Postal code — Guatemala postal code
- [x] `gu.postal_code` — ZIP code — Guam ZIP code
- [x] `gw.postal_code` — Postal code — Guinea-Bissau postal code
- [x] `hm.postal_code` — Postal code — Heard & McDonald Islands postal code
- [x] `hn.postal_code` — Postal code — Honduras postal code
- [x] `hr.postal_code` — Postal code — Croatia postal code
- [x] `ht.postal_code` — Postal code — Haiti postal code
- [x] `hu.postal_code` — Postal code — Hungary postal code
- [x] `id.postal_code` — Postal code — Indonesia postal code
- [x] `ie.postal_code` — Eircode — Ireland Eircode
- [x] `il.postal_code` — Postal code — Israel postal code
- [x] `im.postal_code` — Postal code — Isle of Man postal code
- [x] `in.postal_code` — PIN code — India PIN code
- [x] `io.postal_code` — Postal code — British Indian Ocean Territory postal code
- [x] `iq.postal_code` — Postal code — Iraq postal code
- [x] `ir.postal_code` — Postal code — Iran postal code
- [x] `is.postal_code` — Postal code — Iceland postal code
- [x] `it.postal_code` — Postal code — Italy postal code
- [x] `je.postal_code` — Postal code — Jersey postal code
- [x] `jo.postal_code` — Postal code — Jordan postal code
- [x] `jp.postal_code` — Postal code — Japan postal code
- [x] `ke.postal_code` — Postal code — Kenya postal code
- [x] `kg.postal_code` — Postal code — Kyrgyzstan postal code
- [x] `kh.postal_code` — Postal code — Cambodia postal code
- [x] `kr.postal_code` — Postal code — South Korea postal code
- [x] `kw.postal_code` — Postal code — Kuwait postal code
- [x] `ky.postal_code` — Postal code — Cayman Islands postal code
- [x] `kz.postal_code` — Postal code — Kazakhstan postal code
- [x] `la.postal_code` — Postal code — Laos postal code
- [x] `lb.postal_code` — Postal code — Lebanon postal code
- [x] `li.postal_code` — Postal code — Liechtenstein postal code
- [x] `lk.postal_code` — Postal code — Sri Lanka postal code
- [x] `lr.postal_code` — Postal code — Liberia postal code
- [x] `ls.postal_code` — Postal code — Lesotho postal code
- [x] `lt.postal_code` — Postal code — Lithuania postal code
- [x] `lu.postal_code` — Postal code — Luxembourg postal code
- [x] `lv.postal_code` — Postal code — Latvia postal code
- [x] `ma.postal_code` — Postal code — Morocco postal code
- [x] `mc.postal_code` — Postal code — Monaco postal code
- [x] `md.postal_code` — Postal code — Moldova postal code
- [x] `me.postal_code` — Postal code — Montenegro postal code
- [x] `mf.postal_code` — Postal code — St. Martin postal code
- [x] `mg.postal_code` — Postal code — Madagascar postal code
- [x] `mh.postal_code` — ZIP code — Marshall Islands ZIP code
- [x] `mk.postal_code` — Postal code — North Macedonia postal code
- [x] `mm.postal_code` — Postal code — Myanmar (Burma) postal code
- [x] `mn.postal_code` — Postal code — Mongolia postal code
- [x] `mp.postal_code` — ZIP code — Northern Mariana Islands ZIP code
- [x] `mq.postal_code` — Postal code — Martinique postal code
- [x] `mt.postal_code` — Postal code — Malta postal code
- [x] `mu.postal_code` — Postal code — Mauritius postal code
- [x] `mv.postal_code` — Postal code — Maldives postal code
- [x] `mx.postal_code` — Postal code — Mexico postal code
- [x] `my.postal_code` — Postal code — Malaysia postal code
- [x] `mz.postal_code` — Postal code — Mozambique postal code
- [x] `na.postal_code` — Postal code — Namibia postal code
- [x] `nc.postal_code` — Postal code — New Caledonia postal code
- [x] `ne.postal_code` — Postal code — Niger postal code
- [x] `nf.postal_code` — Postal code — Norfolk Island postal code
- [x] `ng.postal_code` — Postal code — Nigeria postal code
- [x] `ni.postal_code` — Postal code — Nicaragua postal code
- [x] `no.postal_code` — Postal code — Norway postal code
- [x] `np.postal_code` — Postal code — Nepal postal code
- [x] `nz.postal_code` — Postal code — New Zealand postal code
- [x] `om.postal_code` — Postal code — Oman postal code
- [x] `pe.postal_code` — Postal code — Peru postal code
- [x] `pf.postal_code` — Postal code — French Polynesia postal code
- [x] `pg.postal_code` — Postal code — Papua New Guinea postal code
- [x] `ph.postal_code` — Postal code — Philippines postal code
- [x] `pk.postal_code` — Postal code — Pakistan postal code
- [x] `pl.postal_code` — Postal code — Poland postal code
- [x] `pm.postal_code` — Postal code — St. Pierre & Miquelon postal code
- [x] `pn.postal_code` — Postal code — Pitcairn Islands postal code
- [x] `pr.postal_code` — ZIP code — Puerto Rico ZIP code
- [x] `pt.postal_code` — Postal code — Portugal postal code
- [x] `pw.postal_code` — ZIP code — Palau ZIP code
- [x] `py.postal_code` — Postal code — Paraguay postal code
- [x] `re.postal_code` — Postal code — Réunion postal code
- [x] `ro.postal_code` — Postal code — Romania postal code
- [x] `rs.postal_code` — Postal code — Serbia postal code
- [x] `ru.postal_code` — Postal code — Russia postal code
- [x] `sa.postal_code` — Postal code — Saudi Arabia postal code
- [x] `sd.postal_code` — Postal code — Sudan postal code
- [x] `sg.postal_code` — Postal code — Singapore postal code
- [x] `sh.postal_code` — Postal code — St. Helena postal code
- [x] `si.postal_code` — Postal code — Slovenia postal code
- [x] `sj.postal_code` — Postal code — Svalbard & Jan Mayen postal code
- [x] `sk.postal_code` — Postal code — Slovakia postal code
- [x] `sm.postal_code` — Postal code — San Marino postal code
- [x] `sn.postal_code` — Postal code — Senegal postal code
- [x] `so.postal_code` — Postal code — Somalia postal code
- [x] `sv.postal_code` — Postal code — El Salvador postal code
- [x] `sz.postal_code` — Postal code — Eswatini postal code
- [x] `ta.postal_code` — Postal code — Tristan da Cunha postal code
- [x] `tc.postal_code` — Postal code — Turks & Caicos Islands postal code
- [x] `th.postal_code` — Postal code — Thailand postal code
- [x] `tj.postal_code` — Postal code — Tajikistan postal code
- [x] `tm.postal_code` — Postal code — Turkmenistan postal code
- [x] `tn.postal_code` — Postal code — Tunisia postal code
- [x] `tr.postal_code` — Postal code — Türkiye postal code
- [x] `tt.postal_code` — Postal code — Trinidad & Tobago postal code
- [x] `tw.postal_code` — Postal code — Taiwan postal code
- [x] `tz.postal_code` — Postal code — Tanzania postal code
- [x] `ua.postal_code` — Postal code — Ukraine postal code
- [x] `um.postal_code` — ZIP code — U.S. Outlying Islands ZIP code
- [x] `us.postal_code` — ZIP code — United States ZIP code
- [x] `uy.postal_code` — Postal code — Uruguay postal code
- [x] `uz.postal_code` — Postal code — Uzbekistan postal code
- [x] `va.postal_code` — Postal code — Vatican City postal code
- [x] `vc.postal_code` — Postal code — St. Vincent & Grenadines postal code
- [x] `ve.postal_code` — Postal code — Venezuela postal code
- [x] `vg.postal_code` — Postal code — British Virgin Islands postal code
- [x] `vi.postal_code` — ZIP code — U.S. Virgin Islands ZIP code
- [x] `vn.postal_code` — Postal code — Vietnam postal code
- [x] `wf.postal_code` — Postal code — Wallis & Futuna postal code
- [x] `xk.postal_code` — Postal code — Kosovo postal code
- [x] `yt.postal_code` — Postal code — Mayotte postal code
- [x] `za.postal_code` — Postal code — South Africa postal code
- [x] `zm.postal_code` — Postal code — Zambia postal code
