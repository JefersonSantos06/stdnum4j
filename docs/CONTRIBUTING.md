# Adicionar e manter tipos de número

Duas metades: [adicionar um tipo de número](#adicionar-um-tipo-de-número) e
[manter o que já existe](#manutenção). Leia
[ARCHITECTURE.md](ARCHITECTURE.md) antes, se ainda não leu — esta página
pressupõe a SPI.

---

# Adicionar um tipo de número

## Como é um tipo pronto

O CPF é a forma inteira em cinquenta linhas. Nada abaixo é cerimônia que você
poderia ter pulado.

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
        if (Strings.allSame(n)) {
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

O código e o Javadoc da biblioteca são escritos em inglês, inclusive o texto
padrão das mensagens de erro. A tradução para português vive nos arquivos
`.properties` (veja o [passo 6](#passo-6--traduções)), e a documentação, aqui
em `docs/`, é em português.

## Passo 1 — onde ele vai e como se chama

Escolha o módulo pela região: `stdnum-br`, `stdnum-eu`, `stdnum-latam`,
`stdnum-na`, `stdnum-apac`, `stdnum-africa`, ou `stdnum-international` para um
número sem país.

O **id** é a chave do registry e o nome de todo arquivo de fixture. É o código
ISO 3166-1 alfa-2 do país em minúsculas, um ponto, e o nome curto de todo dia
do número: `br.cpf`, `es.nif`, `de.vat`. Um número internacional é só o nome
curto: `iban`, `isbn`, `lei`. Ele precisa casar com
`[a-z0-9]+([._-][a-z0-9]+)*`, o que o construtor do `Descriptor` exige.

O **nome da classe** é o id em PascalCase sem os pontos — `es.nif` → `EsNif`,
`at.uid` → `AtUid`. O módulo brasileiro usa os nomes locais (`Cpf`, `Renavam`,
`TituloEleitor`), porque só há um país nele e o prefixo não diria nada.

### Se o tipo é alvo de um despachante

`iban`, `vatin` e `eu.excise` moram no `stdnum-international` e alcançam as
regras nacionais pelo registry, nunca importando-as. Ponha a regra nacional no
módulo regional dela e a torne localizável — **não mexa no despachante**:

| Despachante | Acha a regra nacional por |
|---|---|
| `iban` | id `<cc>.iban`, e nada mais |
| `vatin`, `eu.vat` | id `<cc>.vat`; na falta dele, o **único** tipo do país com a tag `Tag.VAT` |
| `eu.excise` | id `<cc>.excise`; na falta dele, o **único** tipo do país com a tag `Tag.EXCISE` |

O plano B é pela tag *única*. Adicionar um segundo tipo com `Tag.VAT` a um país
que não tenha id `<cc>.vat` torna a busca ambígua, e o despachante passa a
recusar todo número de VAT daquele país. Ou batize um deles de `<cc>.vat`, ou
não marque o segundo com `VAT`.

### Se é um código postal

Os códigos postais genéricos moram no `stdnum-postal`, um tipo por país,
gerados de `postal-codes.dat` — lá não se escreve um à mão. Um código postal
que mereça regra própria (uma lista de códigos em uso, como o austríaco; um
dígito verificador) vai para o módulo regional, com o id de sempre, e o país
entra em `PostalCode.HAND_WRITTEN` para o genérico sair do caminho. O
`PostalTypesTest` do `stdnum-all` quebra nomeando o país se você esquecer.

## Passo 2 — escreva a classe

- `public final class`, construtor `private`, e
  `public static final X INSTANCE`. Não há outro jeito de obter uma.
- O `Descriptor` é um campo `static final`, construído uma vez na inicialização
  da classe, nunca a cada chamada.
- `compact` é quase sempre `Strings.compact(number, "<separadores>")`, listando
  todo separador com que o número é escrito. O `Strings` dobra antes os sósias
  Unicode — traços, espaços, dígitos de largura completa e arábico-índicos — e
  é isso que faz funcionar um número colado de um PDF.
- `validate` **precisa** devolver a forma compacta. Verifique na ordem do mais
  básico: conjunto de caracteres, depois comprimento, depois componentes,
  depois o dígito verificador. Quem vê "checksum inválido" deve poder confiar
  que tudo antes do checksum estava certo.
- Use `algo` para a aritmética — `Luhn`, `Damm`, `Verhoeff`, `Iso7064`,
  `Mod97`, `Weighted`, `Crc16`. Se a rotina de que você precisa for realmente
  nova, acrescente-a a `algo` com testes próprios; se for caso único, mantenha
  privada na classe. Não cole um laço de Luhn.
- Sobrescreva `format` **só** se a apresentação diferir da forma compacta. O
  padrão já valida e devolve a forma compacta, que é a apresentação correta de
  um número escrito sem separadores. Quando diferir, ela é uma `Mask` num campo
  `static final`, não aritmética de `substring`:
  `private static final Mask MASK = Mask.of("###.###.###-##");` e
  `return MASK.fill(validate(number));`.
- Acessores (`getBirthDate`, `toSiren`, `manufacturer`) são `public static`,
  recebem uma única `String` e **validam primeiro**. Um método público que
  lança `NumberFormatException` num número dos nossos próprios fixtures é um
  defeito; isso já aconteceu, e os fixtures existem para pegar. Abra com
  `Strings.requireDigits(INSTANCE.compact(number), 11)` e termine uma data com
  `Dates.birthDate(ano, mês, dia)`, que já recusa o dia que não existe com a
  frase que todos os outros usam.
- Nunca deixe escapar de `validate`, `compact` ou de um acessor nada além de
  uma subclasse de `ValidationException`. Nem em `null`, nem em string vazia,
  nem em 1.024 noves, nem num emoji.

## Passo 3 — registre-o

Acrescente a instância ao provider do módulo — `BrProvider`, `EuProvider` e
assim por diante —, que é a única classe listada em
`META-INF/services/io.github.jefersonsantos06.stdnum.spi.StdNumProvider`. Um
tipo não registrado ainda valida; ele apenas fica invisível para o `StdNums`,
para os despachantes e para a varredura do registry.

Depois **atualize as contagens no `AllRegisteredContractTest`**:

```java
assertEquals(451, StdNums.all().size());
assertEquals(38, StdNums.byCountry("BR").size());
```

Essas asserções são arames de tropeço de propósito. Um tipo escrito e não
registrado, ou registrado duas vezes, falha aqui. Mudar o número é uma edição
de uma linha, feita de propósito.

## Passo 4 — fixtures

Em `src/test/resources/fixtures/` do mesmo módulo:

| Arquivo | Obrigatório | Conteúdo |
|---|---|---|
| `<id>.txt` | sim | números válidos, um por linha, como se escreve no mundo real |
| `<id>-invalid.txt` | sim | números que precisam ser recusados |
| `<id>-format.txt` | só se o `format` for sobrescrito | `entrada<TAB>esperado` |
| `<id>-accessor.txt` | só se houver acessores | `método<TAB>entrada<TAB>esperado` |

Preserve as máscaras e os separadores — `390.533.447-05` e `39053344705` são
dois testes diferentes. Registre num comentário `#` de onde veio cada amostra:

```
# valid CPFs (well-known public test vectors)
390.533.447-05
```

O arquivo de inválidos deve ter um número para cada modo de falha que o
validador tem: curto demais, caracteres errados, dígito verificador quebrado,
componente desconhecido. Se você escreveu um `if` que lança, deve haver uma
linha que chega nele.

## Passo 5 — a classe de teste

```java
class EsNifTest extends StdNumContractTest {
    @Override
    protected StdNum subject() {
        return EsNif.INSTANCE;
    }
}
```

É o arquivo inteiro, para a maioria dos tipos. Acrescente métodos `@Test` só
para o que um contrato genérico não tem como saber — uma apresentação
documentada, o caso-limite de um acessor, uma regra que a especificação faz
questão de citar. Veja [TESTING.md](TESTING.md) para o que o contrato já cobre,
e assim não escrever duas vezes.

## Passo 6 — traduções

Se você lançou um `Message.of(...)` com código, acrescente essa chave ao
`messages_pt.properties` **no mesmo pacote da classe âncora**:

```properties
cpf.repeated = Um CPF formado por um único dígito repetido não é válido.
```

O inglês fica no código Java e nunca se repete num arquivo de propriedades. Se
o motivo for daqueles que outros números também dão — uma data de nascimento
que não é data, um serial só de zeros, uma província desconhecida — use a
constante que já existe em `Reasons` em vez de inventar um código, e não há
nada a traduzir.

O `everyReasonIsTranslated` vai falhar nomeando qualquer código que você
esquecer, então este passo é cobrado, não lembrado.

## Passo 7 — referências

`.references(...)` diz *onde o número está documentado*. A revisão de links de
setembro de 2026 trocou 60 de 128 links; estas são as regras que saíram dela.

**O que vale, em ordem de preferência:**

1. a página do órgão emissor **sobre o número** — não a home dele, não o portal;
2. uma especificação oficial, ou a ficha TIN da OCDE para um número fiscal,
   quando o órgão não publica nada melhor;
3. a Wikipédia, quando o órgão só publica um login, um formulário de busca ou
   um comunicado. Isso não é recuo: para muitos números, é o único lugar onde a
   rotina do dígito verificador está escrita.

**O que não vale:**

- a **home** de um ministério ou de um registro — nomeia uma instituição, não
  um número;
- um **formulário de consulta** — "digite um número e dizemos se ele existe"
  confere um número e documenta nenhum;
- uma página sobre um **número diferente** do mesmo país. O `sk.ico` citava o
  artigo inglês de VAT, cuja linha eslovaca é IČ DPH, um número inteiramente
  outro.

**Leia a página. Não apenas busque.** A falha mais comum não é um 404; é uma
URL que responde `200` e não documenta mais nada, porque agora redireciona para
uma home. Por outro lado, um `403` ou uma conexão que não completa é quase
sempre um WAF recusando busca automatizada, não um link morto — abra no
navegador antes de trocar.

## Passo 8 — o inventário

Acrescente o tipo ao [NUMBERS.md](NUMBERS.md), na seção do módulo dele, sob o
país dele, em ordem de id. Aquele arquivo é mantido à mão e nada o cobra.

## Checklist

- [ ] classe no módulo certo, `final`, construtor privado, `INSTANCE`
- [ ] `Descriptor` com id, país, nome curto, título, descrição, tags e
      referências
- [ ] `compact` lista todo separador
- [ ] `validate` devolve a forma compacta e verifica o básico primeiro
- [ ] `format` sobrescrito só se a apresentação diferir
- [ ] registrado no provider do módulo
- [ ] contagens atualizadas no `AllRegisteredContractTest`
- [ ] `<id>.txt` e `<id>-invalid.txt`, com comentários de procedência
- [ ] `<id>-format.txt` / `<id>-accessor.txt`, se for o caso
- [ ] classe de teste estendendo `StdNumContractTest`
- [ ] todo código de mensagem novo traduzido em `messages_pt.properties`
- [ ] referências lidas, não apenas buscadas
- [ ] listado em `docs/NUMBERS.md`
- [ ] `mvn verify` verde

---

# Manutenção

## Regerar um arquivo de dados

Quinze classes leem um de dezesseis bancos de prefixo `.dat` — uma delas, o
`PostalCode`, em nome de 178 tipos. **Esses arquivos são
gerados e nunca editados à mão** — nem para corrigir uma linha errada, nem para
acrescentar um banco que falta. Cada um tem um gerador Java de arquivo único em
[`tools/`](../tools/README.md) com o `curl` e o `java` exatos que o produzem, e
cada arquivo carrega um cabeçalho nomeando a fonte e o carimbo de versão dela.

Quando um registro publica dados novos:

```bash
curl -L -o RangeMessage.xml https://www.isbn-international.org/export_rangemessage.xml
java tools/GenerateIsbnDat.java RangeMessage.xml \
  > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/isbn.dat
```

depois `mvn verify` e leia o diff. Uma regeração que muda milhares de linhas
quando o registro anunciou uma mudança pequena significa que o formato da fonte
mudou, não que os dados mudaram.

Se uma linha estiver mesmo errada, conserte o **gerador** — o parser, o filtro,
a conferência cruzada — para que a próxima regeração preserve a correção. O
`GenerateIbanDat` descarta qualquer entrada cuja estrutura BBAN interpretada
não bata com o comprimento declarado, em vez de emiti-la; o
`GenerateBeBanksDat` descarta as linhas que o registro marca como não
atribuídas. É ali que as correções pertencem.

Os geradores não são módulos Maven — eles rodam quando um registro publica, não
a cada build, e não têm dependências. A CI os compila a cada push para que uma
refatoração não os quebre em silêncio.

## Revisar links de referência

Links apodrecem, e apodrecem calados. As regras do que vale como referência
estão no [passo 7](#passo-7--referências) acima; o método de uma varredura é:

1. buscar todos e anotar o status — isso acha os 404 declarados, que são a
   minoria;
2. **ler toda página que respondeu**, conferindo se ela ainda documenta o
   número que o tipo valida. A falha que uma checagem de status não vê é a URL
   que responde 200 e cai numa home;
3. abrir à mão qualquer coisa que voltou 403 ou não voltou nada. Isso costuma
   ser um WAF, e o link está bom.

Na última varredura, **115 dos 451 tipos não citam referência nenhuma**. Isso é
lacuna, não link quebrado — a hora natural de fechar um pedaço dela é quando
você mexer num desses tipos por outro motivo.

## Manter o inventário honesto

Dois lugares saem do lugar quando um tipo é adicionado:

- as contagens fixas do `AllRegisteredContractTest` — cobradas, o build quebra;
- o [NUMBERS.md](NUMBERS.md) — não cobrado, mantido à mão.

A contagem no cabeçalho do NUMBERS.md e a do `AllRegisteredContractTest` devem
ser sempre o mesmo número. Se em algum momento houver dúvida sobre o que está
registrado, quem manda é o registry:

```java
StdNums.all().forEach(n -> System.out.println(n.descriptor().id()));
```

## Acrescentar um idioma

Copie `messages_pt.properties` para `messages_<idioma>.properties` no mesmo
pacote, em **todo** módulo que tenha um — `spi`, `br`, `br/ie`,
`international`, `eu`, `latam`, `na`, `apac`, `africa` — e traduza. Depois
sobrescreva `translations()` no teste de contrato para incluir o novo locale, e
a mesma verificação de cobertura que guarda o português passa a guardá-lo.

Duas coisas a saber:

- As quatro chaves `error.*` no pacote `spi` do `stdnum-core` são a rede de
  segurança. Qualquer número inválido, de qualquer tipo, cai numa delas, então
  elas precisam ser verdadeiras tanto para uma "Chave NF-e" quanto para um
  "Código de barras". Nunca nomeie um número específico nelas.
- Um arquivo de país (`messages_pt_BR.properties`) é **sobreposto** ao arquivo
  do idioma, então deve conter só o que realmente difere. Distribua o arquivo
  do idioma; acrescente um de país só quando um país de fato precisar de outra
  redação.

## Versões e dependências

`maven.compiler.release` é 17 e a CI builda no 17 e no 21. A única dependência
é o JUnit, declarada uma vez no `dependencyManagement` do pai, pelo BOM do
JUnit. Acrescentar uma dependência de runtime a qualquer módulo é uma decisão a
se tomar de propósito, não uma conveniência — o `stdnum-core` não ter nenhuma é
uma característica da biblioteca.

O jar de todo módulo declara um `Automatic-Module-Name` via
`${auto.module.name}`. Um módulo novo precisa definir essa propriedade, senão
quem estiver no module path ganha um nome derivado do arquivo.

## Regras que não dobram

- **O python-stdnum é referência, nunca fonte.** O comportamento dele foi
  comparado, e os vetores de teste dele foram importados e reconferidos. Nenhum
  código, nenhum arquivo de dados e nenhuma redação veio de lá, e nenhum pode
  vir.
- **Um arquivo `.dat` é gerado.** Se você se pegar abrindo um num editor, a
  resposta está no gerador.
- **Um validador lança só subclasses de `ValidationException`.** Qualquer outra
  exceção escapando de `validate`, `compact` ou de um acessor é defeito, por
  mais estranha que tenha sido a entrada.
- **`format` recusa o que `validate` recusa.** Um número é formatado no momento
  em que vai para uma nota ou para uma tela, que é o pior lugar possível para
  lavar um inválido.

## Commits

As mensagens de commit aqui são prosa, não prefixos de conventional commit. O
assunto diz o que mudou em palavras comuns — *"Sixty references that had
stopped pointing at anything"*, *"Eleven types that need a prefix database,
each with its own generator"* — e o corpo explica por quê, incluindo o que se
descobriu errado e o que se deixou de fazer de propósito. Um commit que
conserta defeitos os nomeia. As mensagens são escritas em inglês, como o resto
do histórico.
