# Minecraft Plugins

Plugins gratuitos para servidores **Paper 1.21+** (Java 21), feitos por **auoifhiaifhi Minecraft Development**.

| Plugin | O que faz |
| --- | --- |
| [SimpleHomes](simple-homes) | Casas por jogador com limite, atraso de teleporte que cancela se o jogador se mover e mensagens configuráveis. |
| [TpsInfo](tps-info) | Relatório de TPS, MSPT, memória e carga de cada mundo, com alerta para a staff quando o servidor fica lento. |
| [WelcomeKit](welcome-kit) | Título de boas-vindas, kit inicial automático, comando `/kit` com tempo de espera e mensagens de entrada e saída. |

Todos usam [MiniMessage](https://docs.advntr.dev/minimessage/format.html) nas mensagens e guardam as configurações em `config.yml`.

## SimpleHomes

| Comando | Descrição | Permissão |
| --- | --- | --- |
| `/sethome [nome]` | Salva uma casa na sua posição (padrão: `home`) | `simplehomes.use` (todos) |
| `/home [nome]` | Teleporta para uma casa | `simplehomes.use` |
| `/delhome <nome>` | Remove uma casa | `simplehomes.use` |
| `/homes` | Lista suas casas | `simplehomes.use` |

Permissões extras (padrão: operadores): `simplehomes.unlimited` ignora o limite de casas e `simplehomes.bypass-delay` teleporta sem esperar.

## TpsInfo

| Comando | Descrição | Permissão |
| --- | --- | --- |
| `/tpsinfo` (ou `/tpsi`) | Mostra TPS (1m/5m/15m), MSPT, memória e chunks, entidades e jogadores por mundo | `tpsinfo.use` |
| `/tpsinfo reload` | Recarrega a configuração | `tpsinfo.reload` |

Quem tem `tpsinfo.alert` recebe um aviso no chat quando o TPS médio de 1 minuto cai abaixo do limite definido em `alert.threshold`.

## WelcomeKit

| Comando | Descrição | Permissão |
| --- | --- | --- |
| `/kit` | Recebe o kit (respeita o tempo de espera) | `welcomekit.kit` (todos) |
| `/welcomekit reload` | Recarrega a configuração | `welcomekit.reload` |

Os itens do kit ficam em `kit.items` no `config.yml`, no formato `MATERIAL:quantidade`.

## Como instalar

1. Baixe o `.jar` do plugin (veja abaixo como gerar).
2. Coloque na pasta `plugins` do servidor Paper.
3. Reinicie o servidor. As configurações aparecem em `plugins/<NomeDoPlugin>/config.yml`.

## Como gerar os .jar

**Pelo GitHub (sem instalar nada):** depois de enviar este projeto, abra a aba **Actions**, entre na execução mais recente do **Build** e baixe o arquivo `plugins` na seção **Artifacts**. Dentro dele estão os três `.jar`.

**No seu computador:** com o JDK 21 e o [Gradle](https://gradle.org/install/) instalados, rode `gradle build`. Os arquivos ficam em `simple-homes/build/libs`, `tps-info/build/libs` e `welcome-kit/build/libs`.

## Licença

[MIT](LICENSE)
