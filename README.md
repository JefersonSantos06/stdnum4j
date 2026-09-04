# stdnum4j

Uma biblioteca Java para interpretar, validar e reformatar números e códigos
padronizados — números fiscais, identificadores pessoais, números bancários e
de pagamento, códigos de produto. **451 tipos de número em 183 países**, sem
nenhuma dependência.

Inspirada no [python-stdnum](https://github.com/arthurdejong/python-stdnum);
implementada do zero a partir das especificações públicas.

> **Estado: desenvolvimento inicial.** A API pública ainda pode mudar, e nada
> foi publicado no Maven Central. Construa localmente com `mvn install`.

## Usando

```java
Cpf.INSTANCE.isValid("390.533.447-05");   // true
Cpf.INSTANCE.validate("390.533.447-05");  // "39053344705"  — a forma compacta
Cpf.INSTANCE.format("39053344705");       // "390.533.447-05"
```

`validate` devolve a forma compacta e lança uma subclasse de
`ValidationException` quando o número está errado. Quando um número inválido é
resultado esperado, e não erro — um formulário, uma importação, um job em lote
—, use `check`, que devolve um resultado selado sobre o qual dá para fazer um
`switch` exaustivo:

```java
switch (Cnpj.INSTANCE.check(entrada)) {
    case Check.Valid v   -> guardar(v.compact());
    case Check.Invalid i -> recusar(i.reason());
}
```

Os tipos também são descobríveis em tempo de execução, sem importá-los:

```java
StdNums.byId("br.cpf");        // Optional<StdNum>
StdNums.byCountry("ES");       // todo tipo espanhol no classpath
StdNums.byTag(Tag.VAT);        // todo número de VAT
```

### Recusas no idioma de quem usa

`reason()` e `getMessage()` são em inglês, que é o que pertence a um log. Para
mostrar a falha a uma pessoa, entregue-a a um `Messages` do idioma dela — a
biblioteca nunca escolhe um, não guarda estado global e nunca lê
`Locale.getDefault()`:

```java
Messages pt = Messages.forLocale(Locale.forLanguageTag("pt-BR"));

switch (Cpf.INSTANCE.check(entrada)) {
    case Check.Valid v   -> guardar(v.compact());
    case Check.Invalid i -> recusar(pt.render(i));
    // "Um CPF formado por um único dígito repetido não é válido."
}
```

Todo motivo que a biblioteca dá está traduzido para o português, e o teste de
contrato prova isso para cada tipo. Uma tradução nunca é tudo-ou-nada: um
motivo sem tradução cai numa frase menos específica no mesmo idioma, não numa
frase em inglês. [Como funciona →](docs/ARCHITECTURE.md#dizer-por-quê-num-idioma)

## Módulos

Leve só as regiões de que precisa. Todo módulo depende apenas do `stdnum4j-core`,
exceto o `stdnum4j-eu`, que também depende do `stdnum4j-international` porque um
IBAN nacional é um IBAN com uma regra nacional por cima.

| Módulo | Tipos | Conteúdo |
|---|---:|---|
| `stdnum4j-core` | — | SPI, exceções, algoritmos de dígito verificador (Luhn, Damm, Verhoeff, ISO 7064, Mod 97-10, mod 11 ponderado, CRC-16), o banco de prefixos `NumDb` e o registry. Zero dependências. |
| `stdnum4j-tck` | — | Testes de contrato reutilizáveis, em JUnit 5, para implementações de `StdNum`. |
| `stdnum4j-international` | 30 | Formatos independentes de país: IBAN, ISBN, ISSN, ISMN, ISNI, EAN/GTIN, ISIN, CUSIP, SEDOL, FIGI, BIC, IMEI, MAC, LEI, IMO, CAS RN, ISO 6346, ISO 11649, GRid, ISAN, ISRC, MEID, UPI, CFI, IMSI, a element string GS1-128, o endereço Bitcoin e os despachantes VATIN e EU VAT. |
| `stdnum4j-br` | 37 | Brasil: CPF, CNPJ (inclusive o formato alfanumérico de 2026), PIS/PASEP, CNS, título de eleitor, RENAVAM, chave de acesso da NF-e, boleto FEBRABAN, Pix BR Code e as inscrições estaduais das 27 unidades federativas. |
| `stdnum4j-eu` | 128 | Europa: 44 países, mais o identificador SEPA de credor, o One Stop Shop, o EIC, a NACE, o número CE e o serial de cédula de euro, todos válidos em toda a União. |
| `stdnum4j-latam` | 23 | América Latina: AR, CL, CO, CR, CU, DO, EC, GT, MX, PE, PY, SV, UY, VE. |
| `stdnum4j-na` | 10 | América do Norte: os SSN, ITIN, EIN, PTIN e ATIN dos EUA, o TIN que é qualquer um deles que o contribuinte tenha, e o routing number; o SIN canadense, o business number e o número de saúde da Colúmbia Britânica. |
| `stdnum4j-apac` | 33 | Ásia-Pacífico e adjacências: AU, CN, ID, IL, IN, JP, KR, MY, NZ, OM, PK, RU, SG, TH, TR, TW, VN. |
| `stdnum4j-africa` | 12 | África: DZ, EG, GH, GN, KE, MA, MU, MZ, SN, TN, ZA. |
| `stdnum4j-postal` | 178 | Códigos postais: um tipo por país ou território com padrão nos metadados de endereço do Google (os da libaddressinput, dados CC BY 4.0) — CEP, ZIP, Eircode, PIN e os demais, gerados de um arquivo só. |
| `stdnum4j-all` | — | Agregador que depende de todos os módulos, e casa dos testes entre módulos. |

O inventário completo, tipo a tipo, está em
**[docs/NUMBERS.md](docs/NUMBERS.md)**. Ele cobre 234 dos 235 tipos de número
que o python-stdnum traz, e acrescenta 217 que ele não tem.

## Arquivos de dados

Quinze classes leem um banco de prefixos — os registros de IBAN e ISBN,
os CEPs austríacos, os cadastros bancários tcheco, belga e neozelandês, a
classificação CFI da ISO 10962, os códigos de país e de rede móvel, o registro
MAC do IEEE, as divisões administrativas chinesas, os identificadores de
aplicação GS1, a classificação NACE, as repartições fiscais austríacas, as
regiões indonésias e, num arquivo só, o formato do código postal de 178 países
e territórios, tirado dos metadados de endereço do Google (dados CC BY 4.0).

Eles são **gerados a partir dos registros de origem e nunca editados à mão**.
Os geradores ficam em [`tools/`](tools/README.md), que documenta como
reconstruir cada arquivo; eles não têm dependências, e as duas fontes
publicadas como planilha são lidas só com o JDK, já que um xlsx é um zip de
XML.

## Build

```bash
mvn verify
```

Requer JDK 17+. Roda 21.340 testes.

## Documentação

| | |
|---|---|
| **[Arquitetura](docs/ARCHITECTURE.md)** | A SPI, os três pilares, como as falhas são relatadas e traduzidas, o grafo de módulos, o que falta de propósito. |
| **[Testes](docs/TESTING.md)** | Como 21.340 testes saem de um contrato e 15.960 linhas de fixture, e o que o contrato garante. |
| **[Contribuindo](docs/CONTRIBUTING.md)** | Adicionar um tipo de número, passo a passo — e como se mantêm os arquivos de dados, os links de referência e o inventário. |
| **[Números](docs/NUMBERS.md)** | Todos os tipos, por módulo e país. |
| **[Geradores de dados](tools/README.md)** | Como reconstruir cada arquivo `.dat` a partir do registro dele. |

A documentação é em português. O código, o Javadoc, o texto padrão das
mensagens de erro e as mensagens de commit são em inglês.
