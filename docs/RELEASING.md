# Publicando no Maven Central

O `stdnum4j` é publicado no [Central Publisher Portal](https://central.sonatype.com)
sob o namespace `io.github.jefersonsantos06`. **Uma release é um botão.** Em
**Actions → release → Run workflow** você escolhe qual parte da versão sobe, e
o [workflow](../.github/workflows/release.yml) faz o resto: fixa a versão, roda
os testes, assina, envia ao portal, tagueia o commit e devolve o `master`
a um snapshot.

## Preparo, uma vez

### 1. A conta e o namespace

1. Entre em [central.sonatype.com](https://central.sonatype.com) com a conta do
   GitHub.
2. Em **Namespaces**, adicione `io.github.jefersonsantos06`. Como ele deriva de
   um usuário do GitHub, a verificação é automática assim que o portal confirma
   que a conta é sua.
3. Em **Account → Generate User Token**, gere um token. O portal mostra um par
   `username`/`password` **uma vez só** — é o que vai nos secrets adiante, não a
   senha da conta.

### 2. A chave de assinatura

Todo arquivo publicado no Central vai acompanhado de uma assinatura destacada
— um `.asc` por artefato — e o portal recusa o envio inteiro se não conseguir
conferi-la. São duas metades da mesma chave, em lugares diferentes: a pública
num keyserver, para o Central conferir; a privada num secret, para a CI assinar.

A chave se cria na sua máquina, e é a única coisa aqui que se faz nela. Requer
o `gpg` instalado (`brew install gnupg` no macOS):

```bash
gpg --full-generate-key
```

Responda `1` (RSA and RSA), `4096`, e uma validade — `0` nunca expira, `2y` é
mais prudente, ao custo de renovar. **Não escolha ECC nem Ed25519:** o Central
tem histórico de recusar assinaturas dessas curvas. Guarde a passphrase num
gerenciador de senhas antes de seguir; ela vira um secret no passo 3.

Descubra o identificador da chave — é o que vem depois da barra na linha `sec`:

```bash
gpg --list-secret-keys --keyid-format=long
```

Publique a metade pública. Dois keyservers, porque eles sincronizam mal entre si
e o Central não consulta sempre o mesmo:

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
```

```bash
gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>
```

**Confirme que propagou antes de tentar publicar.** Uma chave que ainda não
chegou ao keyserver é a causa mais comum de um envio recusado, e a mensagem que
o portal devolve não diz isso com todas as letras:

```bash
gpg --keyserver keyserver.ubuntu.com --recv-keys <KEY_ID>
```

Enquanto responder `key not found on keyserver`, espere e repita. Por fim,
exporte a metade privada, que é o que a CI usa:

```bash
gpg --armor --export-secret-keys <KEY_ID>
```

A saída é um bloco que começa em `-----BEGIN PGP PRIVATE KEY BLOCK-----` e
termina em `-----END-----`. As duas linhas fazem parte do conteúdo. Guarde uma
cópia junto da passphrase: perder a chave não impede uma release futura — gera-se
outra —, mas obriga a refazer este passo inteiro.

### 3. O environment e os secrets

O job declara um environment chamado `maven-central`. Não é obrigatório criá-lo:
um environment que o workflow menciona e não existe é criado pelo GitHub na
primeira execução, sem regra nenhuma. Vale criá-lo à mão pelo que se pendura
nele.

Em **Settings → Environments → New environment**, com esse nome, e então:

- **Required reviewers**, você mesmo. O job passa a parar e esperar aprovação
  antes de rodar — e como ele para *antes* do primeiro passo, aprovar é o
  momento em que você confirma a release. Uma versão publicada no Central é
  imutável; esta é a trava que separa um clique de um número de versão queimado.
- **Deployment branches and tags**, restrito a `master`. Sem isso, qualquer
  branch que rode o workflow alcança os secrets de publicação.

Os quatro secrets vão em **Environment secrets**, e não no nível do repositório:
ali eles existem só para o job que declara este environment, e os outros dois
workflows — o `build` e o `regenerate data` — não os enxergam.

| Secret | Conteúdo |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | o `username` do token do portal |
| `MAVEN_CENTRAL_PASSWORD` | o `password` do token do portal |
| `GPG_PRIVATE_KEY` | a saída inteira do `--export-secret-keys`, com as linhas `-----BEGIN/END-----` |
| `MAVEN_GPG_PASSPHRASE` | a passphrase da chave |

No GitHub Free, regras de proteção de environment só valem em repositório
público. Se este um dia fechar sem plano pago, elas passam a ser ignoradas em
silêncio — os secrets continuam funcionando, a trava não.

O workflow declara `permissions: contents: write`, porque ele empurra dois
commits e uma tag. O padrão do repositório é somente leitura, e essa declaração
é o que basta: não é preciso mexer em **Settings → Actions → Workflow
permissions**.

## Cada release

O `master` vive num snapshot: `X.Y.Z-SNAPSHOT` é a versão *sendo preparada*.
Publicar é decidir qual número ela recebe.

1. **Abra o workflow.** Actions → **release** → **Run workflow**, e escolha:

   | Campo | O que faz |
   |---|---|
   | `bump` | `patch` publica o snapshot como está — `1.0.1-SNAPSHOT` vira `1.0.1`. `minor` sobe para `1.1.0`, `major` para `2.0.0` |
   | `version` | uma versão exata, se você quiser ignorar o `bump`. Vazio na maioria das vezes |
   | `dry_run` | faz tudo menos publicar e empurrar. Veja adiante |

2. **Aprove o job**, se você configurou o revisor obrigatório. Ele fica em
   `Waiting` até isso.

3. **Espere.** Cerca de dois minutos, a maior parte nos testes.

4. **Solte a release no portal.** O workflow envia o deployment e para em
   `VALIDATED` — de propósito, para que ninguém publique sem olhar. Abra
   [Deployments](https://central.sonatype.com/publishing/deployments), confira
   os artefatos e clique em **Publish**. Daí até o `search.maven.org` mostrar a
   versão levam alguns minutos; até o Central sincronizar por completo, algumas
   horas.

   Depois desse botão não há desfazer: uma versão publicada no Central não pode
   ser alterada nem removida. Um engano se conserta publicando `X.Y.Z+1`.

5. **Publique as notas.** O workflow cria a release do GitHub como **rascunho**,
   com as notas geradas dos commits desde a tag anterior. Revise e publique em
   [Releases](https://github.com/JefersonSantos06/stdnum4j/releases).

O sumário do run diz, no fim, o que sobrou para você e em que versão o `master`
ficou.

### A ordem, que é o que importa quando algo falha

O workflow foi montado para que uma falha não deixe meio caminho andado:

1. lê a versão do `master` e recusa se ela não for um snapshot, ou se a tag
   já existir
2. `versions:set` para a versão de release — nos doze poms de uma vez, e isso
   também reescreve o `project.build.outputTimestamp`
3. `mvn verify` — os 21.605 testes, já na versão que vai ser publicada
4. commit e tag, **locais**
5. `mvn -Prelease deploy` — assina e envia ao portal
6. `versions:set` para o próximo snapshot, e um segundo commit
7. **só então** empurra os dois commits e a tag
8. cria o rascunho da release

Um erro em qualquer passo até o 6 não empurrou nada: o repositório está como
estava, e rodar de novo depois de corrigir é seguro. O único resíduo possível é
um deployment pendente no portal, se o passo 5 chegou a subir — descarte-o lá.
O sumário do run avisa quando é o caso.

### Ensaiando

Marque `dry_run` e o workflow roda inteiro — versão, testes completos,
assinatura, empacotamento — sem falar com o portal e sem empurrar nada. É o
jeito de conferir que os secrets e a chave estão certos antes de queimar um
número de versão, e vale rodar uma vez antes da primeira release de verdade.

## O que o profile `release` acrescenta

Um `mvn verify` normal não assina nada e não gera javadoc — é rápido e não pede
chave nenhuma. O profile liga o que o Central exige além do jar:

- `maven-source-plugin` — o `-sources.jar`
- `maven-javadoc-plugin` — o `-javadoc.jar`
- `maven-gpg-plugin` — um `.asc` destacado por artefato, com a passphrase vindo
  de `MAVEN_GPG_PASSPHRASE` e nunca de um `settings.xml`
- `central-publishing-maven-plugin` — que toma o lugar do `maven-deploy-plugin`
  na fase `deploy` e fala com o portal

Para rodar isso na sua máquina, sem chave e sem publicar nada:

```bash
mvn -Prelease -Dgpg.skip=true clean verify
```

O plugin de publicação lê as credenciais de um bloco `<server>` de id `central`,
que é o `publishingServerId` declarado no `pom.xml` da raiz. Esse bloco não está
versionado em lugar nenhum: quem o escreve é a action `setup-java`, num
`settings.xml` descartável dentro do runner, a partir dos secrets do environment.

Todo módulo é publicado, o `stdnum4j-parent` inclusive: ele é o pai que os
outros onze declaram, e sem ele no repositório nenhum deles resolve.
