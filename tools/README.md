# Geradores de arquivo de dados

A biblioteca distribui os bancos de prefixo de que precisa **gerados, nunca
editados à mão**. Cada um é produzido por um programa Java de arquivo único
aqui, de modo que os dados sempre possam ser rastreados até a fonte e
reconstruídos a partir dela.

Rode-os com `java <arquivo>` — o Java 11 em diante compila um arquivo-fonte
único na hora, então não há nada a construir antes. Mas na prática você não
roda um de cada vez: o `Regenerate` roda os dezesseis.

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

## isbn.dat — faixas de grupo de registro e de editora

Fonte: a range message oficial publicada pela ISBN International.

```bash
curl -L -o RangeMessage.xml https://www.isbn-international.org/export_rangemessage.xml
java tools/GenerateIsbnDat.java RangeMessage.xml \
  > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/isbn.dat
```

O serial e a data da mensagem são copiados para o cabeçalho do arquivo, então
a versão dos dados no repositório é sempre identificável.

## iban.dat — registro de países e estruturas BBAN

A fonte autoritativa é o SWIFT IBAN Registry, cujo download está atrás de um
muro anti-robô. O gerador, por isso, lê o artigo da Wikipédia que o espelha, e
confere cada entrada: um registro cuja estrutura interpretada não soma o
comprimento que a tabela declara é reportado e descartado, em vez de
emitido.

```bash
curl -L -A "Mozilla/5.0" -o iban.html \
  https://en.wikipedia.org/wiki/International_Bank_Account_Number
java tools/GenerateIbanDat.java iban.html \
  > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/iban.dat
```

Se um dia o registro da SWIFT ficar acessível, substitua este gerador por um
que o leia direto — o que importa é o formato de saída, não a fonte.

## cz-banks.dat — códigos do sistema de pagamentos tcheco

```bash
curl -L -o kody_bank_CR.csv   https://www.cnb.cz/cs/platebni-styk/.galleries/ucty_kody_bank/download/kody_bank_CR.csv
java tools/GenerateCzBanksDat.java kody_bank_CR.csv   > stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/cz-banks.dat
```

## at-postleitzahl.dat — CEPs austríacos

Fonte: a API de dados abertos do regulador. Só os códigos marcados como
endereçáveis são emitidos; o resto é caixa postal e código interno. O carimbo
de versão que a API traz é copiado para o cabeçalho do arquivo.

```bash
curl -L -o plz.json https://data.rtr.at/api/v1/tables/plz.json
java tools/GenerateAtPostleitzahlDat.java plz.json   > stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/at-postleitzahl.dat
```

## be-banks.dat e nz-banks.dat — a partir de planilhas

Os dois cadastros são publicados em xlsx. O `Xlsx.java` lê um deles só com o
JDK — um xlsx é um zip de XML —, então estes geradores continuam sem
dependências como o resto. Compile-o junto com o gerador:

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

## oui.dat — o registro IEEE de blocos de endereço MAC

Três registros, um por tamanho de bloco: o MA-L atribui os primeiros 24 bits de
um endereço, o MA-M os primeiros 28 e o MA-S os primeiros 36. Um bloco médio ou
pequeno é sempre subdivisão de um grande, e é escrito aninhado sob ele. Blocos
mantidos pela própria Registration Authority, ou registrados de forma privada,
não nomeiam fabricante e ficam de fora — e são exatamente os pais dos blocos
subdivididos.

```bash
curl -L -o oui.csv   https://standards-oui.ieee.org/oui/oui.csv
curl -L -o mam.csv   https://standards-oui.ieee.org/oui28/mam.csv
curl -L -o oui36.csv https://standards-oui.ieee.org/oui36/oui36.csv
java tools/GenerateOuiDat.java oui.csv mam.csv oui36.csv   > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/oui.dat
```

Blocos consecutivos de uma mesma organização são unidos numa faixa, o que
transforma a sequência de centenas de blocos de uma empresa numa entrada só.

## cfi.dat — a classificação ISO 10962

O link de download está na página de data standards do grupo SIX e casa com
`.*/cfi/.*xlsx`; ache-o lá, em vez de fixar um nome de arquivo datado.

```bash
javac -d tools/classes tools/Xlsx.java tools/GenerateCfiDat.java
java -cp tools/classes GenerateCfiDat cfi.xlsx   > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/cfi.dat
```

## imsi.dat — códigos de país e de rede móvel

Sete páginas da Wikipédia, que espelham a lista da UIT. Busque o wikitexto cru
de cada uma e passe todas:

```bash
for p in "Mobile_country_code"          "Mobile_network_codes_in_ITU_region_2xx_(Europe)"          "Mobile_network_codes_in_ITU_region_3xx_(North_America)"          "Mobile_network_codes_in_ITU_region_4xx_(Asia)"          "Mobile_network_codes_in_ITU_region_5xx_(Oceania)"          "Mobile_network_codes_in_ITU_region_6xx_(Africa)"          "Mobile_network_codes_in_ITU_region_7xx_(South_America)"; do
  curl -L -o "$p.wiki" "https://en.wikipedia.org/w/index.php?title=$p&action=raw"
done
java tools/GenerateImsiDat.java *.wiki   > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/imsi.dat
```

## cn-loc.dat — códigos de divisão administrativa chinesa

Oito páginas da Wikipédia em chinês, uma por região de numeração. Um condado
que existiu só durante certo intervalo de anos é escrito com esse intervalo à
frente, para que um número possa ser lido contra o ano de nascimento de quem o
carrega.

```bash
for i in 1 2 3 4 5 6 7 8; do
  curl -L -o "region$i.wiki"     "https://zh.wikipedia.org/w/index.php?title=$(python -c "import urllib.parse,sys;print(urllib.parse.quote('中华人民共和国行政区划代码_(%s区)' % sys.argv[1]))" $i)&action=raw"
done
java tools/GenerateCnLocDat.java region*.wiki   > stdnum-apac/src/main/resources/io/github/jefersonsantos06/stdnum/apac/cn-loc.dat
```

## gs1-ai.dat — identificadores de aplicação GS1

Os identificadores são publicados como um bloco JSON-LD dentro da página de
referência. Consecutivos que concordam em tudo menos no próprio número são
escritos como uma faixa só, que é como se guardam os identificadores de casa
decimal, do 3100 ao 3105.

```bash
curl -L -o ai.html https://ref.gs1.org/ai/
java tools/GenerateGs1AiDat.java ai.html   > stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/gs1-ai.dat
```

## eu-nace20.dat e eu-nace21.dat — a classificação NACE

O Eurostat publica a classificação em si pela API SDMX dele, que é fonte melhor
do que uma renderização dela. A Rev. 2 é `NACE_R2` e a Rev. 2.1 é
`NACE_R2_1`.

```bash
base=https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1/codelist/ESTAT
curl -L -o nace21.xml "$base/NACE_R2_1"
java tools/GenerateEuNaceDat.java nace21.xml "Rev. 2.1"   > stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/eu-nace21.dat
curl -L -o nace20.xml "$base/NACE_R2"
java tools/GenerateEuNaceDat.java nace20.xml "Rev. 2"   > stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/eu-nace20.dat
```

## at-fa.dat — números de repartição fiscal austríaca

```bash
curl -L -o abgabenkontonummer.wiki   "https://de.wikipedia.org/w/index.php?title=Abgabenkontonummer&action=raw"
java tools/GenerateAtFaDat.java abgabenkontonummer.wiki   > stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/at-fa.dat
```

## id-loc.dat — regiões administrativas indonésias

O serviço de bridging do Badan Pusat Statistik publica tanto os códigos dele
quanto os do Kemendagri. Um NIK carrega o código do Kemendagri, que é o
adotado — e os dois discordam sobre Papua, então a distinção importa.

```bash
base=https://sig.bps.go.id/rest-bridging/getwilayah
curl -L -o provinsi.json "$base?level=provinsi"
for p in $(grep -o '"kode_bps":"[0-9]*"' provinsi.json | cut -d'"' -f4); do
  curl -L -o "kab-$p.json" "$base?level=kabupaten&parent=$p"
done
java tools/GenerateIdLocDat.java provinsi.json kab-*.json   > stdnum-apac/src/main/resources/io/github/jefersonsantos06/stdnum/apac/id-loc.dat
```

## postal-codes.dat — o código postal de todos os países

Fonte: os metadados de endereço do Google, os mesmos que a libaddressinput
usa, que documentam para cada país e território o regex do código postal, os
exemplos e o prefixo de exibição. Os dados são CC BY 4.0. O serviço não
publica versão, então a data da coleta vai como argumento e para o cabeçalho.
A raiz lista as regiões; busque uma a uma (a URL redireciona para o gstatic,
daí o `-L`):

```bash
base=https://chromium-i18n.appspot.com/ssl-address/data
curl -L -o data.json "$base"
for c in $(grep -o '"countries":"[^"]*"' data.json | cut -d'"' -f4 | tr '~' ' '); do
  curl -L -o "$c.json" "$base/$c"
done
java tools/GeneratePostalCodesDat.java 2026-09-03 data.json ??.json \
  > stdnum-postal/src/main/resources/io/github/jefersonsantos06/stdnum/postal/postal-codes.dat
```

O regex da fonte descreve o código como se escreve, com separadores; a
biblioteca casa a forma compacta. O gerador reescreve o regex para descrever
essa forma — espaço e hífen literais somem junto com o `?` que os tornava
opcionais, uma classe só de separadores some do mesmo jeito, e um prefixo
literal opcional como `(?:PC )?` é tirado de dentro e vira um prefixo que o
`compact()` sabe remover. A apresentação é recuperada dos exemplos, cada um
virando uma máscara em que dígito é `9` e letra é `A`. Antes de escrever, todo
exemplo passa pelo resultado: tem de casar, e tem de voltar a si mesmo quando
formatado. Qualquer falha encerra o gerador com 1 e nada é escrito.

Todo `\` do regex sai dobrado no arquivo, porque o leitor transforma `\x` em
`x`; um diff de regeração precisa ser lido com isso em mente.

## Por que estes não são módulos Maven

Eles — o `Regenerate` incluído — rodam quando um registro publica dados novos,
não a cada build, e
não têm dependências. Mantê-los fora do reator evita um módulo que não produz
artefato. A CI ainda os compila a cada push, então uma refatoração não pode
quebrá-los sem ninguém notar.
