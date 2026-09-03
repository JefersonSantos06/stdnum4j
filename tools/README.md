# Geradores de arquivo de dados

A biblioteca distribui os bancos de prefixo de que precisa **gerados, nunca
editados à mão**. Cada um vem de uma classe aqui, de modo que os dados
sempre possam ser rastreados até a fonte e reconstruídos a partir dela.

Há um jeito de fazer isso, e um só: o `Regenerate`. As classes não são
programas — não têm `main`, não leem argumento e não escolhem onde escrever.
É o que garante que regerar um arquivo e regerar os dezesseis deem o mesmo
resultado, porque são literalmente a mesma execução.

## Regerar tudo de uma vez

```bash
javac -d tools/classes tools/*.java
java -cp tools/classes Regenerate            # os dezesseis
java -cp tools/classes Regenerate isbn oui   # só esses
java -cp tools/classes Regenerate --check    # regera e compara, não escreve
```

O `Regenerate.java` busca cada fonte, roda o gerador e escreve o `.dat` só se
ele mudou. **A tabela `TARGETS` é o único lugar onde um arquivo de dados é
configurado** — sua fonte, seu gerador e seu destino. Acrescentar um `.dat` é
acrescentar uma linha ali.

Cada alvo é independente: uma fonte que não responde, ou um gerador que recusa
porque a fonte mudou de forma, é reportado e os outros seguem. O código de
saída continua 0 — use `--strict` para que uma falha o mude. Toda busca é
limitada duas vezes, pelo timeout da requisição e por um prazo externo, então
nada prende a execução.

| flag | efeito |
|---|---|
| `--list` | os alvos, seus geradores e seus destinos |
| `--check` | regera e compara sem escrever; sai 1 se algo difere |
| `--strict` | sai 1 se algum alvo falhou |
| `--out-dir <dir>` | escreve `<id>.dat` ali em vez de no repositório |
| `--report <dir>` | `summary.tsv`, o stderr de cada alvo e os `.dat` que mudaram |
| `--keep` / `--offline <dir>` | guarda o diretório de trabalho / reusa um guardado, sem rede |
| `--jobs`, `--deadline`, `--date` | buscas simultâneas, prazo global, data de coleta |

### Duas coisas que não são detalhe

**O JDK importa.** O `GeneratePostalCodesDat` tira os nomes de país da cópia de
CLDR do próprio JDK, e ela muda entre versões: no 17 é `Turkey`, no 25 é
`Türkiye`. O `postal-codes.dat` foi gerado no **JDK 25**; regerar num JDK mais
velho regride cento e oitenta nomes parecendo atualização de dados. O `--check`
acusa: se o `postal-codes` vier `changed` só com nome de país, o JDK está
errado.

**O User-Agent importa.** A Wikipédia responde 403 ao agente padrão do
`HttpClient`, o que derrubaria quatro alvos (`iban`, `imsi`, `cn-loc`,
`at-fa`). O driver se identifica; se você buscar à mão, use `-A`.

As receitas por gerador abaixo continuam valendo como caminho manual, para
depurar um alvo sozinho.

## As dezesseis fontes

Cada arquivo de dados é uma classe que implementa `Source`: ela declara de
onde vem, para onde vai, e como uma coisa vira a outra. Não há segundo
caminho — buscar, escolher o conjunto de caracteres, normalizar o fim de
linha e comparar são do driver, uma vez, para todas. Por isso gerar um
arquivo à mão e gerar os dezesseis não podem divergir: é o mesmo código.

E por isso nenhuma fonte recebe parâmetro. A data que um cabeçalho registra
vem do `run.retrievedOn()`; a revisão que era argumento do NACE virou o que
aquela fonte *é* — `GenerateEuNace20Dat` e `GenerateEuNace21Dat`, uma cada.

Para ver o que existe e de onde vem:

```bash
java -cp tools/classes Regenerate --list
```

Acrescentar um arquivo de dados é escrever uma classe. O driver acha sozinho
toda classe do classpath que implementa `Source` — não há lista para manter,
nem no driver, nem aqui, nem no workflow.

## Depurar uma fonte

O `--keep` guarda o que foi baixado e diz onde; o `--offline` reaproveita
esse diretório sem tocar na rede. Depurar um parser deixa de custar 250
requisições:

```bash
java -cp tools/classes Regenerate --keep postal-codes
java -cp tools/classes Regenerate --offline /tmp/stdnum-regen-123 postal-codes
```

Uma fonte que recusa lança com uma frase dizendo o que mudou lá em cima; o
driver reporta aquele alvo como falho, não escreve nada e segue com os
outros.

## Por que estes não são módulos Maven

Eles rodam quando um registro publica dados novos, não a cada build, e não
têm dependências. Mantê-los fora do reator evita um módulo que não produz
artefato. A CI ainda os compila a cada push, então uma refatoração não pode
quebrá-los sem ninguém notar.
