# Testes

`mvn verify` roda **21.340 testes**. Quase nenhum deles foi escrito um a um.
Esta página explica como se chega a esse número, para que adicionar um tipo de
número signifique adicionar dados, e não adicionar código de teste.

## Como rodar

```bash
mvn verify                            # tudo
mvn -pl stdnum4j-br test                # um módulo
mvn -pl stdnum4j-eu test -Dtest=EsNifTest
mvn -pl stdnum4j-all test               # só os testes entre módulos
```

A CI roda `mvn -B -ntp verify` no JDK 17 e no 21, e depois compila
`tools/*.java` à parte — os geradores de arquivo de dados não são módulos
Maven, e é isso que impede que uma refatoração os quebre sem ninguém notar.

## A ideia: um contrato, muitos tipos

Todo tipo de número responde às mesmas perguntas, então as asserções são
escritas uma vez, no `stdnum4j-tck`, e os tipos fornecem os dados. Uma classe de
teste costuma ser isto e nada mais:

```java
class EsNifTest extends StdNumContractTest {
    @Override
    protected StdNum subject() {
        return EsNif.INSTANCE;
    }
}
```

Esse arquivo tem 11 linhas e roda **22 testes**: as 5 amostras válidas e as 3
inválidas dos arquivos de fixture, as 9 entradas-lixo que todo tipo recebe, a
verificação de tradução, as duas verificações fixas, e dois pulos pelos
arquivos de formato e de acessor que ele não tem. O `StdNumContractTest` é
quase todo feito de métodos `@TestFactory` que transformam cada linha de
amostra num `DynamicTest` próprio, nomeado com a amostra, de modo que uma falha
se lê assim:

```
valid: 39053344705
invalid: 111.111.111-11
garbage: abc😀def
```

O `stdnum4j-tck` é um artefato publicado, não um test-jar, então quem escrever um
tipo de número fora deste repositório ganha o mesmo contrato apenas dependendo
dele.

## O que o contrato verifica

**Para toda amostra válida**

- `validate(x)` devolve uma string não vazia e igual a `compact(x)`;
- `compact` e `validate` são idempotentes — devolver a forma compacta para
  dentro não muda nada;
- `validate(format(x))` volta à mesma forma compacta;
- espaço em branco em volta não faz diferença (` \t<número>\n ` valida), do
  jeito que um número colado de um formulário ou de uma planilha chega;
- `isValid` devolve verdadeiro e `check` devolve um `Check.Valid` carregando a
  mesma forma compacta.

**Para toda amostra inválida e toda entrada-lixo**

- `validate` lança uma subclasse de `ValidationException` — nunca
  `NullPointerException`, `IndexOutOfBoundsException`,
  `StringIndexOutOfBoundsException`, `NumberFormatException`,
  `DateTimeParseException` nem qualquer outra exceção não verificada;
- `format` recusa o que o `validate` recusa, de modo que uma apresentação é
  sempre a apresentação de um número válido;
- `isValid` é falso e `check` devolve um `Check.Invalid` com erro não nulo.

**Sempre**

- `null` é recusado por `validate` e por `compact` com uma
  `ValidationException`, não com um NPE;
- o `Descriptor` tem id, nome curto e título não vazios;
- todo motivo que o tipo dá está traduzido para todos os idiomas que a
  biblioteca distribui (veja abaixo).

A lista de lixo é fixa e aplicada a todo tipo sem ninguém escrevê-la: a string
vazia, três formas de espaço em branco, `!!!`, `%%%`, `----`, `abc😀def` (um par
substituto, que é onde laços ingênuos de `charAt` quebram) e 1.024 noves (que é
onde um acumulador `int` estoura). Um tipo que legitimamente aceite uma dessas
sobrescreve `garbageSamples()` e diz por quê na sobrescrita.

## Fixtures

As amostras ficam em `src/test/resources/fixtures/` do módulo dono do tipo,
nomeadas conforme o id do `Descriptor`. Um número por linha, **exatamente como
aparece no mundo real** — máscaras, separadores e tudo. Linhas em branco e
linhas começando com `#` são ignoradas, e as linhas de `#` são onde se registra
a procedência da amostra.

| Arquivo | Contém | Arquivos | Linhas |
|---|---|---|---|
| `<id>.txt` | números válidos | 265 | 13.452 |
| `<id>-invalid.txt` | números que precisam ser recusados | 265 | 2.043 |
| `<id>-format.txt` | `entrada<TAB>apresentação esperada` | 132 | 231 |
| `<id>-accessor.txt` | `método<TAB>entrada<TAB>esperado` | 52 | 234 |
| | | **714** | **15.960** |

Os dois últimos existem porque a verificação de ida e volta não prende tudo.
`validate(format(x))` prova que o `format` produz *alguma coisa* válida; não
prova que produz `16.727.230/0001-97`. E um acessor — `BeBis.getBirthDate`,
`AtUid.calcCheckDigit`, `Mac.manufacturer`, `FrSiret.toSiren` — nem está na
interface `StdNum`, então o contrato não alcança. Uma amostra de acessor nomeia
um método público estático da classe de implementação que recebe uma string, e
compara `String.valueOf(resultado)` com o texto esperado:

```
getBirthDate	75.46.08-980.95	1975-06-08
getGender	85473500193	M
```

Um arquivo de fixture faltando não é falha — ele aborta como uma premissa
pulada, nomeando o arquivo que queria. É daí que vêm os 346 pulos de um build
verde, exatamente: das 265 classes de teste de contrato, 133 não têm arquivo de
formato (a apresentação delas *é* a forma compacta) e 213 não têm arquivo de
acessor (não expõem acessores). Todas as 265 têm arquivo válido e inválido, e é
por isso que nenhuma dessas duas fábricas jamais aborta. Um arquivo **válido**
ou **inválido** faltando merece atenção; um de formato ou de acessor faltando
costuma significar que não há nada a dizer.

## De onde vieram as amostras

O corpus não é inventado. Ele é, em ordem de preferência:

1. os exemplos resolvidos que o órgão emissor publica (as páginas do *Roteiro
   de Crítica* do SINTEGRA, a documentação de cédula da Hacienda, os exemplos
   do próprio registro IBAN da SWIFT);
2. os vetores contra os quais o python-stdnum testa, importados mecanicamente e
   depois reconferidos aqui;
3. números derivados à mão de uma rotina de dígito verificador publicada,
   marcados como tal num comentário `#`.

O comportamento de validação foi comparado com o do python-stdnum número a
número, ao longo de 13.255 vetores, e concorda em todos eles. O `format`
concorda em 152 de 169 casos comparáveis; as divergências são deliberadas e
cada uma está documentada no Javadoc do tipo que diverge — principalmente a de
que aqui o `format` recusa um número inválido, enquanto a referência o
reagrupa e devolve uma string bem-vestida.

Essa comparação é um oráculo de uma vez só, não parte do build: o python-stdnum
é referência, e nada neste repositório depende dele ou copia dele.

## A verificação de tradução

`everyReasonIsTranslated` percorre as amostras inválidas e de lixo, recolhe a
`Message` que cada recusa carrega, e afirma que uma mensagem com código tem
chave no arquivo de tradução de todo idioma que a biblioteca distribui (hoje,
`pt`).

Ela lê o arquivo `.properties` diretamente em vez de perguntar ao `Messages`
como a frase saiu — e essa distinção é o sentido inteiro do teste. Um código
sem tradução cai na frase do `ValidationError` dele, que *está* traduzida, de
modo que renderizar nunca revela uma chave faltando. Perguntar ao arquivo,
sim.

Um tipo que distribua traduções em outro idioma sobrescreve `translations()` e
a mesma verificação passa a cobri-lo.

## Além do contrato

O contrato é o piso. Um tipo com qualquer coisa interessante também ganha
testes escritos à mão, na mesma classe, para o que um contrato genérico não tem
como saber:

```java
@Test
void unicodeSeparatorsAreCleaned() {
    // travessão no lugar do hífen, como colado de documento formatado
    assertEquals("39053344705", Cpf.INSTANCE.validate("390.533.447–05"));
}
```

Dois padrões se repetem:

- **Um teste de tabela para uma família.** O `SintegraExamplesTest` guarda
  exemplos resolvidos de cada unidade federativa, afirma que as 27 estão
  presentes, e altera o último dígito verificador de cada exemplo válido para
  provar que ele passa a ser recusado. É por isso que as 24 inscrições
  estaduais que não têm arquivo de fixture próprio continuam cobertas. As
  amostras escritas com separadores fazem dobro de serviço: para elas, o teste
  também exige que `format` devolva exatamente a amostra, o que faz da tabela a
  especificação das máscaras de todos os 27 estados.
- **Um teste entre módulos.** Tudo que precisa de mais de um módulo mora no
  `stdnum4j-all`: os despachantes de IBAN, VATIN, EU VAT e excise só alcançam as
  regras nacionais quando todo jar regional está no classpath.

## A varredura do registry

O `AllRegisteredContractTest`, no `stdnum4j-all`, percorre o registry sem saber o
que há nele, e acerta todo tipo descoberto com `null` e com a lista de lixo. Um
módulo que registre um validador frágil falha aqui **mesmo que não traga teste
nenhum**.

Ele também afirma contagens fixas — 451 tipos registrados, 38 do Brasil, 10 da
Espanha, 8 da França — e que os tipos internacionais não carregam país. Esses
números são um arame de tropeço: adicionar um tipo sem registrá-lo, ou
registrá-lo duas vezes, falha aqui. Adicionar um tipo, portanto, significa
atualizar este teste de propósito. Veja
[CONTRIBUTING.md](CONTRIBUTING.md#adicionar-um-tipo-de-número).

## Onde estão os testes

| Módulo | Testes | Pulados |
|---|---:|---:|
| `stdnum4j-core` | 92 | 0 |
| `stdnum4j-tck` | 28 | 2 |
| `stdnum4j-br` | 794 | 22 |
| `stdnum4j-international` | 2.715 | 31 |
| `stdnum4j-eu` | 6.702 | 180 |
| `stdnum4j-latam` | 3.679 | 27 |
| `stdnum4j-na` | 353 | 13 |
| `stdnum4j-apac` | 3.069 | 35 |
| `stdnum4j-africa` | 1.269 | 13 |
| `stdnum4j-postal` | 820 | 16 |
| `stdnum4j-all` | 1.819 | 7 |
| **Total** | **21.340** | **346** |

O `stdnum4j-tck` testa a si mesmo contra um `DummyNumber` que existe só para
provar que o contrato pega o que diz pegar, e o `RegistryIntegrationTest` prova
que um tipo registrado por `ServiceLoader` é achado pelo `StdNums`.
