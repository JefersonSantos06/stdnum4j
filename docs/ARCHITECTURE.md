# Arquitetura

A biblioteca responde a uma pergunta — *esta string é um número tal-e-tal
válido, e como ele fica escrito direito* — para 451 tipos de número. Tudo aqui
existe para manter essa única pergunta respondida do mesmo jeito 451 vezes.

## Uma interface, e nada mais para aprender

Um tipo de número é uma classe que implementa `StdNum`:

```java
public interface StdNum {
    Descriptor descriptor();
    String compact(String number);         // representação mínima
    String validate(String number);        // valida E devolve a forma compacta
    default boolean isValid(String number);
    default Check check(String number);    // resultado sem exceção: Valid | Invalid
    default String format(String number);  // a apresentação que as pessoas esperam
}
```

Dois métodos são abstratos; quatro têm implementação padrão escrita uma vez na
interface. `isValid` e `check` são o `validate` com a falha capturada, e
`format` cai no `validate` — um tipo cuja apresentação canônica é a própria
forma compacta ganha um `format` correto sem escrever nada.

A instância é um singleton sem estado, por convenção `Xyz.INSTANCE` com
construtor privado. Não há fábrica, objeto de configuração nem builder para
aprender: `Cpf.INSTANCE.validate(entrada)` é a API inteira.

`validate` devolver a forma compacta em vez de `void` ou `boolean` é a decisão
da qual o resto decorre. É o que faz o caso comum — *confira isto, depois
guarde* — caber em uma chamada só, e é o que permite que `isValid`, `check` e
`format` sejam defaults em vez de 451 cópias escritas à mão.

## Três pilares

Tudo que quem chama enxerga é uma destas três coisas.

**`Descriptor`** diz o que o número *é*. Um record com id, país, nome curto,
título, descrição, tags e URLs de referência, construído por um builder fluente
pequeno e validado no construtor compacto: o id tem que casar com
`[a-z0-9]+([._-][a-z0-9]+)*`, o país tem que ter duas letras, o conjunto de
tags é copiado para um `EnumSet` imutável. É a chave do registry, a
documentação e os metadados no mesmo objeto, e é construído uma vez por tipo
como campo `static final`.

**`Tag`** diz para que o número *serve*: `TAX`, `VAT`, `EXCISE`, `PERSON`,
`COMPANY`, `BANK`, `PAYMENT`, `FINANCIAL`, `HEALTH`, `VEHICLE`, `POSTAL`,
`TELECOM`, `MEDIA`, `PRODUCT`, `EDUCATION`, `LOCATION`, `OTHER`. Um número
carrega várias — um número fiscal de pessoa física é `TAX` e `PERSON` — e o
registry as indexa, então "todo número de VAT que esta biblioteca conhece" é
uma chamada.

**`Check`** diz o que aconteceu. Uma interface selada com dois records:

```java
public sealed interface Check {
    record Valid(String compact) implements Check {}
    record Invalid(ValidationError error, Message message) implements Check {}
}
```

Selada, e seus dois casos são records aninhados dentro dela, então um `switch`
sobre um `Check` é exaustivo e o compilador garante isso. É o ponto de entrada
para código que trata número inválido como resultado esperado, não como
exceção — um formulário, uma importação de planilha, um job em lote.

## Falhar

`validate` lança quando o número está errado. A hierarquia tem quatro classes e
para por aí:

```
ValidationException          (unchecked)
├── InvalidFormatException   os caracteres estão errados
│   └── InvalidLengthException   os caracteres estão certos, a quantidade não
├── InvalidChecksumException o dígito verificador não fecha
└── InvalidComponentException uma parte do número nomeia algo que não existe
                             (uma província, um banco, uma data)
```

`InvalidLengthException` estende `InvalidFormatException` porque quem quer
capturar "malformado" não deveria precisar nomear as duas. Cada uma mapeia para
uma das quatro constantes do enum `ValidationError`, que é o que o
`Check.Invalid` carrega.

As exceções são construídas com `super(message, null, false, false)`: sem
supressão, **sem stack trace**. Preencher um stack trace custa mais do que
todas as validações desta biblioteca somadas, e a pilha de uma falha de
validação não conta nada a ninguém — a informação útil é o número e o motivo,
e os dois já estão ali. Elas são sinalização, não relato de erro. É por isso
que `isValid` pode ser implementado como uma exceção capturada sem pedir
desculpa.

## Dizer por quê, num idioma

`getMessage()` e `Check.Invalid.reason()` são em inglês, que é o que pertence a
um log. Para mostrar a falha a uma pessoa, entregue-a a um `Messages` do idioma
dela. A biblioteca nunca escolhe um, não guarda estado global e nunca lê
`Locale.getDefault()`:

```java
Messages pt = Messages.forLocale(Locale.forLanguageTag("pt-BR"));

switch (cpf.check(entrada)) {
    case Check.Valid v   -> guardar(v.compact());
    case Check.Invalid i -> recusar(pt.render(i));
}
```

Uma `Message` é um record com *classe âncora*, *código*, *texto padrão em
inglês* e *argumentos*. A resolução tem três níveis, tentados nesta ordem:

1. o código da própria mensagem, no arquivo de tradução ao lado da classe
   âncora — assim um módulo leva suas traduções dentro do próprio jar;
2. `error.<NOME>` do `ValidationError` — quatro frases que toda tradução
   carrega;
3. o inglês com que o validador foi escrito.

Uma tradução, portanto, nunca é tudo-ou-nada: um código sem tradução custa uma
frase menos específica, não uma frase em inglês. A interpolação é no estilo
`{0}` e escrita à mão, não `MessageFormat`, porque o `MessageFormat` come
apóstrofos — e apóstrofo é inevitável nos idiomas para os quais isto vai ser
traduzido.

Os arquivos de tradução são `messages_<idioma>[_<PAÍS>].properties`, lidos como
UTF-8 via `getResourceAsStream` a partir do pacote da âncora. Não existe
`messages.properties` de propósito: o inglês mora no código Java, uma vez, ao
lado do `throw`. O arquivo do idioma é lido primeiro e o do país sobreposto a
ele, então `pt` atende pt-BR, pt-PT e pt-419, e um arquivo de país só carrega o
que realmente difere.

Motivos que mais de um número dá — uma data de nascimento que não é data, um
serial só de zeros, um código de província que não nomeia província nenhuma —
moram uma vez em `Reasons`, como constantes imutáveis, então são traduzidos uma
vez e leem igual em todo lugar.

## O registry

As implementações se registram por `StdNumProvider`, um serviço de
`ServiceLoader`. Cada módulo tem exatamente uma classe de provider listando
seus tipos, declarada em
`META-INF/services/io.github.jefersonsantos06.stdnum.spi.StdNumProvider`. O
`StdNums` carrega todas uma vez e indexa por id, país e tag:

```java
StdNums.all();                        // todo tipo no classpath
StdNums.byId("br.cpf");               // Optional<StdNum>
StdNums.byCountry("BR");              // List<StdNum>
StdNums.byCountry(locale);            // o país de um Locale que você já tem
StdNums.byCountry("ES", "nif");       // um tipo de um país, pelo nome curto
StdNums.byTag(Tag.VAT);
```

O registry é o que torna possíveis os tipos despachantes sem dependência
cíclica: o `iban` procura a regra nacional de IBAN de um país por
`StdNums.byCountry(cc, "iban")`, e o `vatin` acha o tipo nacional de VAT do
mesmo jeito. Nenhum dos dois importa uma classe de país. Ponha outro conjunto
de módulos no classpath e o mesmo despachante cobre outro conjunto de países.

## Módulos

| Módulo | Depende de | Contém |
|---|---|---|
| `stdnum-core` | *(nada)* | A SPI, os algoritmos de dígito verificador, o banco de prefixos `NumDb`, os utilitários de texto, o registry. |
| `stdnum-tck` | core | O teste de contrato reutilizável. Consumido em escopo `test`. |
| `stdnum-international` | core | Números independentes de país, e os despachantes `iban`/`vatin`/`eu.vat`/`eu.excise`. |
| `stdnum-br` | core | Brasil. |
| `stdnum-eu` | core, **international** | Europa. |
| `stdnum-latam` | core | América Latina. |
| `stdnum-na` | core | América do Norte. |
| `stdnum-apac` | core | Ásia-Pacífico. |
| `stdnum-africa` | core | África. |
| `stdnum-postal` | core | Os códigos postais de 178 países e territórios: uma classe, um arquivo de dados, um tipo por país. |
| `stdnum-all` | todos acima | Sem código. Um agregador, e a casa dos testes que precisam de todos os módulos ao mesmo tempo. |

Todo módulo regional depende só do `stdnum-core`. A única exceção é o
`stdnum-eu`, que também depende do `stdnum-international`, porque um IBAN
espanhol, montenegrino ou norueguês *é* um IBAN com uma regra nacional por
cima, e herda a estrutura dele.

**A dependência corre num sentido só.** O módulo internacional nunca importa
uma classe de país; ele alcança tipos nacionais pelo registry. É essa regra que
mantém o grafo acíclico e ao mesmo tempo deixa o `iban` se comportar como se
soubesse da Espanha.

Quem consome leva só as regiões de que precisa. Alguém validando documentos
brasileiros puxa `stdnum-br` e ganha `stdnum-core`, não 128 tipos europeus.

Todo jar declara um `Automatic-Module-Name`
(`io.github.jefersonsantos06.stdnum`, `.br`, `.eu`, …), para que quem estiver
no module path ganhe módulos nomeados em vez de nomes derivados do arquivo.
Nada aqui declara `module-info`.

## Dentro do `stdnum-core`

```
io.github.jefersonsantos06.stdnum          StdNums — o registry
                                    .spi   StdNum, Descriptor, Tag, Check,
                                           as exceções, Message, Messages,
                                           Reasons, Dates, StdNumProvider
                                    .algo  Luhn, Damm, Verhoeff, Iso7064,
                                           Mod97, Weighted, Crc16
                                    .numdb NumDb — o banco de prefixos
                                    .text  Strings, Mask, Resources
```

O que se repetiria em cada tipo mora aqui. `Mask` escreve o número do jeito
que ele se escreve, e é o `format` de 57 tipos; `Strings.compact` de três
argumentos tira o prefixo que 34 números carregam mas não guardam;
`Strings.requireDigits` é a guarda com que todo acessor abre; `Dates.birthDate`
é o que duas dúzias de identificadores pessoais fazem com os seis dígitos da
data. A regra de qual século dois dígitos significam continua em cada país,
porque cada país tem a sua.

`algo` é a aritmética, e só a aritmética: `Luhn.calcCheckDigit`,
`Weighted.mod11CheckDigit`, `Iso7064.MOD_11_2.validate`. Nenhuma classe de
`algo` sabe o que é um CPF, e um validador costuma ser três linhas de limpeza
com `Strings` mais uma linha de `algo`.

`Strings.compact(number, " -./")` tira os separadores com que o número é
escrito e é como o `compact` é implementado quase sempre. Antes de tirá-los,
ele dobra os sósias Unicode que aparecem quando um número é colado de um
documento formatado: as vinte e três variantes de traço viram `-`, dezesseis
variantes de espaço viram `' '`, e dígitos de largura completa, arábico-índicos
e matemáticos viram dígitos ASCII. Um travessão onde deveria haver um hífen é o
jeito mais comum de uma entrada real falhar sem motivo real.

## Arquivos de dados

Quinze classes precisam de um banco de prefixos: um IBAN precisa da estrutura
BBAN do país dele, um ISBN precisa das faixas de grupo de registro, um endereço
MAC precisa do registro do IEEE para nomear o fabricante, e os 178 códigos
postais leem o padrão de cada país de um arquivo só, gerado dos metadados de
endereço do Google que a libaddressinput usa (dados CC BY 4.0). Eles são distribuídos
como arquivos `.dat` ao lado da classe que os lê, no formato que o `NumDb`
interpreta: um prefixo, depois propriedades `chave="valor"`, uma entrada por
linha, linhas indentadas aninhando sob o pai.

Um comando regera os dezesseis: `java -cp tools/classes Regenerate`. Cada
arquivo é uma classe que implementa `Source` e declara de onde vem e como se
produz; buscar, escolher o conjunto de caracteres e comparar são do driver,
uma vez, para todas — não há um segundo jeito de gerar um `.dat`, e por isso
gerar um e gerar todos não podem divergir. Ele roda semanalmente na CI e abre um PR por arquivo que
mudou; uma fonte fora do ar vira aviso, não interrupção; e o carimbo de coleta
no cabeçalho só anda quando o corpo anda, senão haveria um PR por semana sem
nenhuma mudança de dado. O `postal-codes.dat` exige o JDK 25, porque os nomes
de país saem do CLDR do próprio JDK.

A regra é absoluta: **um arquivo `.dat` é gerado a partir do registro de origem
e nunca editado à mão.** Cada um tem uma fonte em [`tools/`](../tools/README.md),
sem dependências, e um cabeçalho nomeando a origem e a versão de onde veio.
Quando um registro publica dados novos, você roda o `Regenerate`; você não
remenda o arquivo. Veja
[CONTRIBUTING.md](CONTRIBUTING.md#regerar-um-arquivo-de-dados).

## O que falta de propósito

- **Nenhuma dependência.** O `stdnum-core` não tem nenhuma, e nenhum módulo tem
  além do core. Nada é puxado para JSON, XML, HTTP ou log.
- **Nenhum estado global e nenhum locale ambiente.** Nada lê
  `Locale.getDefault()`, nada guarda um "atual" de coisa alguma. Um `Messages`
  é um valor que você segura.
- **Nenhuma reflexão no caminho de validação.** O registry usa `ServiceLoader`
  uma vez, no primeiro uso; depois disso tudo é chamada virtual comum.
- **Nenhum `module-info`.** Nomes automáticos de módulo bastam hoje, e um
  descritor de módulo de verdade precisaria de `opens` em todo pacote que leva
  arquivo de tradução.
- **Nenhuma anotação, nenhum processador, nenhuma configuração em tempo de
  execução.** Um tipo de número é uma classe com dois métodos.
