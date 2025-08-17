# Documentação do Projeto L2jToren

Este documento detalha a arquitetura, funcionalidades e diretrizes de desenvolvimento do projeto L2jToren, um emulador de servidor de código aberto para o MMORPG Lineage 2.

## 1. Visão Geral do Projeto

O L2jToren é um fork da família L2J, focado em oferecer uma experiência de jogo aprimorada através de estabilidade, otimizações e a adição de funcionalidades customizadas. Desenvolvido em Java, o projeto visa ser uma plataforma robusta e flexível para servidores privados de Lineage 2.

## 2. Arquitetura do Sistema

O projeto L2jToren é composto por dois módulos principais:

-   **L2jToren_gameserver**: Responsável pela lógica principal do jogo, incluindo interações com jogadores, NPCs, combate, itens, habilidades e o mundo do jogo.
-   **L2jToren_datapack**: Contém os dados do jogo, como configurações de NPCs, itens, habilidades, mapas, quests e scripts SQL para inicialização do banco de dados.

### 2.1. Estrutura de Módulos (Refatoração Proposta)

Para melhorar a manutenibilidade e escalabilidade, o `L2jToren_gameserver` está em processo de refatoração para uma arquitetura modular. Os módulos propostos incluem:

-   **`core`**: Componentes fundamentais e utilitários globais.
-   **`world`**: Gerenciamento do ambiente do jogo, incluindo mapas, zonas e spawns.
-   **`combat`**: Lógica de combate, cálculo de dano, habilidades e efeitos.
-   **`item`**: Gerenciamento de itens, inventário e propriedades de itens.
-   **`npc`**: Comportamento e interações de NPCs.
-   **`network`**: Camada de comunicação com os clientes do jogo.
-   **`player`**: Gerenciamento de jogadores, personagens e suas ações.
-   **`system`**: Módulos de sistema, como logging, configuração e agendamento de tarefas.

Esta modularização visa reduzir o acoplamento entre as diferentes partes do sistema, facilitando o desenvolvimento paralelo e a manutenção.

### 2.2. Estrutura de Diretórios Atual

A estrutura atual do projeto reflete a transição de um sistema de build baseado em Ant para Gradle, com uma clara separação entre a lógica do servidor e os dados do jogo.

```
/
├── L2jToren_gameserver/      # Módulo principal do servidor do jogo (lógica Java)
│   ├── bin/                # Scripts de inicialização e arquivos de configuração para execução local.
│   ├── config/             # Arquivos de configuração (.properties) para o servidor.
│   ├── dist/               # Diretório de saída da compilação, contendo as distribuições do Login Server e Game Server.
│   ├── java/               # Código-fonte Java do emulador.
│   ├── lib/                # Bibliotecas e dependências (.jar) do projeto.
│   └── build.xml           # Script de build legado (Ant), mantido para referência.
├── L2jToren_datapack/        # Módulo de dados do jogo.
│   ├── data/               # Contém todos os dados utilizados pelo servidor, como definições de NPCs, itens, skills, quests e arquivos HTML para diálogos.
│   ├── sql/                # Scripts SQL para a criação e atualização do banco de dados.
│   └── tools/              # Ferramentas e scripts para auxiliar no gerenciamento do servidor.
├── .settings/              # Arquivos de configuração do Eclipse.
├── .gitignore              # Arquivo com a lista de arquivos e pastas a serem ignorados pelo Git.
├── .project                # Arquivo de configuração do projeto Eclipse.
├── docker-compose.yml      # Arquivo para facilitar a criação de um ambiente de desenvolvimento com banco de dados MariaDB via Docker.
├── Documentacao.md         # Este arquivo.
├── L2jToren.iml            # Arquivo de configuração do projeto IntelliJ IDEA.
└── README.md               # Documentação geral com instruções rápidas de instalação e contribuição.
```

## 3. Funcionalidades Principais

O L2jToren incorpora uma série de funcionalidades customizadas e melhorias de qualidade de vida, detalhadas no arquivo `README.md`.

## 4. Sistema de Build (Ant - `build.xml`)

O projeto historicamente utiliza o Apache Ant como ferramenta de build, orquestrado pelo arquivo `L2jToren_gameserver/build.xml`. Embora o projeto esteja em transição para o Gradle, entender o `build.xml` é útil para manutenção e para compreender o processo de compilação legado.

O script é dividido em "targets" (alvos), que são tarefas específicas. As principais são executadas em uma cadeia de dependências.

### 4.1. Propriedades Principais

O script começa definindo variáveis (propriedades) para os diretórios mais importantes:
-   **`src`**: Aponta para `java/`, onde fica o código-fonte.
-   **`lib`**: Aponta para `lib/`, onde ficam as bibliotecas/dependências `.jar`.
-   **`build`**: Aponta para `build/`, o diretório de saída da compilação.
-   **`dist`**: Aponta para `dist/` dentro do diretório `build`, onde a versão final pronta para distribuição é montada.

### 4.2. Alvos (Targets) do Build

O fluxo de execução é definido pela dependência entre os alvos. O alvo padrão, executado pelo comando `ant` ou `ant dist`, é o `dist`.

-   **`clean`**: Remove o diretório `build/` para garantir que a compilação seja feita do zero, sem arquivos antigos.

-   **`checkRequirements`**: Verifica se o ambiente de desenvolvimento possui o JDK 21 ou superior. Se a versão for incompatível, o build falha com uma mensagem de erro.

-   **`init`**: Depende de `clean` e `checkRequirements`. Cria a estrutura de diretórios necessária dentro de `build/` (`classes`, `dist`, `dist/login`, `dist/gameserver`) para receber os arquivos compilados e a distribuição final.

-   **`compile`**: Depende de `init`. É o coração do processo: compila todo o código-fonte Java (`.java`) do diretório `src/` e coloca os arquivos de bytecode (`.class`) resultantes em `build/classes/`.

-   **`jar`**: Depende de `compile`. Agrupa todos os arquivos `.class` compilados em um único arquivo executável `l2jserver.jar`. Ele também configura o `MANIFEST.MF` dentro do JAR, especificando a classe principal e o classpath das dependências.

-   **`dist`** (Alvo Padrão): Depende de `jar`. É a etapa final que monta os pacotes prontos para execução do Login Server e do Game Server. Suas ações incluem:
    1.  Copiar o `l2jserver.jar` e todas as bibliotecas da pasta `lib/` para as subpastas `libs/` de cada servidor (`login` e `gameserver`).
    2.  Copiar os scripts de inicialização (`.bat` e `.sh`) para as pastas raiz de cada servidor.
    3.  Copiar os arquivos de configuração (`.properties`) da pasta `config/`, separando os que pertencem ao Login Server e ao Game Server.
    4.  Criar diretórios de `log` vazios para cada servidor.

Ao final do processo, o diretório `L2jToren_gameserver/build/dist/` conterá as pastas `login` e `gameserver` completas e prontas para serem executadas.

## 5. Arquivos de Configuração

A pasta `L2jToren_gameserver/config` contém todos os arquivos de configuração do servidor. Abaixo está uma descrição de cada arquivo e suas principais propriedades.

### 5.1. `autofarm.properties`

Este arquivo controla o sistema de "auto farm", que permite que os jogadores configurem seus personagens para caçar monstros automaticamente.

-   `ENABLED`: Ativa ou desativa o sistema de auto farm.
-   `MAX_ZONE_AREA`: A área máxima de uma zona de farm.
-   `MAX_ROUTE_PERIMETER`: O perímetro máximo de uma rota de farm.
-   `MAX_OPEN_RADIUS`: O raio máximo para zonas de farm abertas.
-   `MAX_ZONES`: O número máximo de zonas de farm que um jogador pode criar.
-   `MAX_ROUTES`: O número máximo de rotas de farm que um jogador pode criar.
-   `HP_HEAL_RATE`: A porcentagem de HP em que o sistema tentará usar poções ou habilidades de cura.
-   `MP_HEAL_RATE`: A porcentagem de MP em que o sistema tentará usar poções ou habilidades de cura.
-   `ALLOW_DUALBOX`: Permite ou não que jogadores usem o sistema de auto farm em mais de uma conta ao mesmo tempo.
-   `CHANGE_PLAYER_TITLE`: Altera o título do jogador enquanto ele está usando o sistema de auto farm.

### 5.2. `banned_ips.properties`

Este arquivo está vazio por padrão e é usado para listar os endereços de IP que estão banidos do servidor.

### 5.3. `clans.properties`

Este arquivo controla as configurações relacionadas a clãs e alianças.

-   `DaysBeforeJoinAClan`: O número de dias que um jogador deve esperar antes de poder entrar em outro clã.
-   `DaysBeforeCreateAClan`: O número de dias que um jogador deve esperar antes de poder criar um novo clã.
-   `DaysToPassToDissolveAClan`: O número de dias que leva para dissolver um clã.
-   `MaxNumOfClansInAlly`: O número máximo de clãs em uma aliança.
-   `MembersCanWithdrawFromClanWH`: Permite ou não que os membros do clã retirem itens do armazém do clã.

### 5.4. `events.properties`

Este arquivo controla vários eventos do jogo, como a Olimpíada, Seven Signs, Four Sepulchers e outros.

-   `OlyStartTime`: A hora em que a Olimpíada começa.
-   `OlyCPeriod`: A duração do período de competição da Olimpíada.
-   `OlyMinMatchesToBeClassed`: O número mínimo de partidas para ser classificado como herói.
-   `SevenSignsBypassPrerequisites`: Se `True`, qualquer um pode se juntar a qualquer lado do Seven Signs.
-   `FestivalMinPlayer`: O número mínimo de jogadores para participar do Festival Seven Signs.
-   `NeededPartyMembers`: O número de membros do grupo necessários para entrar no Four Sepulchers.
-   `RiftMinPartySize`: O tamanho mínimo do grupo para entrar no Dimension Rift.
-   `LotteryPrize`: O prêmio inicial da loteria.

### 5.5. `geoengine.properties`

Este arquivo controla as configurações do motor de geodata, que é responsável por controlar o movimento e a linha de visão no mundo do jogo.

-   `GeoDataPath`: O caminho para os arquivos de geodata.
-   `GeoDataType`: O tipo de arquivos de geodata a serem usados (L2J ou L2OFF).
-   `MaxGeopathFailCount`: O número máximo de falhas de geopath autorizadas antes de enviar um aviso de log.
-   `PartOfCharacterHeight`: A porcentagem da altura do personagem em que a linha de visão começa.

### 5.6. `logging.properties`

Este arquivo controla as configurações de log do servidor.

-   `handlers`: Uma lista de classes de manipuladores de log a serem usadas.
-   `.level`: O nível de log global padrão.
-   `java.util.logging.FileHandler.pattern`: O padrão para os nomes dos arquivos de log.
-   `chat.handlers`: O manipulador de log para o chat do jogo.
-   `gmaudit.handlers`: O manipulador de log para auditoria de GM.
-   `item.handlers`: O manipulador de log para itens.

### 5.7. `loginserver.properties`

Este arquivo controla as configurações do servidor de login.

-   `Hostname`: O nome do host do servidor de login.
-   `LoginserverHostname`: O nome do host do servidor de login para vincular.
-   `LoginserverPort`: A porta do servidor de login.
-   `LoginTryBeforeBan`: O número de tentativas de login inválidas antes que o IP seja banido.
-   `AutoCreateAccounts`: Se `True`, as contas serão criadas automaticamente.
-   `EnableFloodProtection`: Ativa ou desativa a proteção contra flood.

### 5.8. `npcs.properties`

Este arquivo controla as configurações relacionadas a NPCs, como o Class Master, o sistema de casamento e o buffer de esquema.

-   `SpawnMultiplier`: Um multiplicador para o número de monstros que aparecem.
-   `AllowEntireTree`: Permite que os Class Masters mudem para qualquer ocupação em qualquer nível dentro da árvore de classes.
-   `ConfigClassMaster`: A configuração para as mudanças de ocupação do Class Master.
-   `WeddingPrice`: O custo do casamento.
-   `BufferMaxSchemesPerChar`: O número máximo de esquemas de buffer disponíveis por jogador.
-   `GuardAttackAggroMob`: Se `True`, os guardas atacarão monstros agressivos à vista.

### 5.9. `players.properties`

Este arquivo controla as configurações relacionadas aos jogadores, como regeneração, inventário, encantamentos e karma.

-   `HpRegenMultiplier`: Um multiplicador para a velocidade de regeneração de HP.
-   `PlayerSpawnProtection`: A proteção do jogador após o teletransporte ou login em segundos.
-   `MaximumSlotsForNoDwarf`: O número máximo de slots de inventário para não anões.
-   `EnchantChanceMagicWeapon`: A chance de sucesso para encantar uma arma mágica.
-   `KarmaPlayerCanShop`: Permite ou não que jogadores com karma comprem em lojas.
-   `PartyXpCutoffMethod`: O método de corte de distribuição de XP do grupo.
-   `AutoLearnSkills`: Se `True`, as habilidades serão aprendidas automaticamente.

### 5.10. `server.properties`

Este arquivo controla as configurações gerais do servidor de jogo.

-   `Hostname`: O nome do host do servidor de jogo.
-   `GameserverHostname`: O nome do host do servidor de jogo para vincular.
-   `GameserverPort`: A porta do servidor de jogo.
-   `RequestServerID`: O ID do servidor que o servidor de jogo solicitará.
-   `MaximumOnlineUsers`: O número máximo de jogadores online permitidos.
-   `AutoLoot`: Se `True`, o loot será coletado automaticamente.
-   `RateXp`: A taxa de experiência do servidor.
-   `RateSp`: A taxa de pontos de habilidade do servidor.

### 5.11. `siege.properties`

Este arquivo contém os valores de configuração para os cercos (sieges) de castelos e salões de clã.

-   `SiegeLength`: A duração do cerco em minutos.
-   `SiegeClanMinLevel`: O nível mínimo do clã para se registrar em um cerco.
-   `AttackerMaxClans`: O número máximo de clãs que podem se registrar como atacantes.
-   `ChSiegeClanMinLevel`: O nível mínimo do clã para se registrar em um cerco de salão de clã.

### 5.12. `vip.properties`

Este arquivo controla as configurações do sistema VIP.

-   `NEW_CHAR_VIP`: Se `True`, novos personagens começarão como VIPs.
-   `DEFAULT_VIP_LEVEL`: O nível VIP padrão para novos personagens.
-   `DEFAULT_VIP_TIME`: O tempo VIP padrão para novos personagens em segundos.

### 5.13. `VoicedCommand.properties`

Este arquivo controla os comandos de voz disponíveis para os jogadores.

-   `CommandOnline`: Ativa ou desativa o comando `.online`.
-   `BankingSystem`: Ativa ou desativa o sistema bancário.

## 6. Scripts SQL

A pasta `L2jToren_datapack/sql` contém todos os scripts SQL necessários para criar e popular o banco de dados do servidor. Os scripts estão organizados da seguinte forma:

### 6.1. Tabelas de Personagens

Estes scripts criam as tabelas que armazenam os dados dos personagens dos jogadores.

-   `accounts.sql`: Armazena os dados da conta.
-   `characters.sql`: A tabela principal de personagens.
-   `character_hennas.sql`: Armazena as hennas dos personagens.
-   `character_macroses.sql`: Armazena as macros dos personagens.
-   `character_memo.sql`: Armazena os memos dos personagens.
-   `character_quests.sql`: Armazena o progresso das quests dos personagens.
-   `character_raid_points.sql`: Armazena os pontos de raid dos personagens.
-   `character_recipebook.sql`: Armazena o livro de receitas dos personagens.
-   `character_recommends.sql`: Armazena as recomendações dos personagens.
-   `character_relations.sql`: Armazena as relações dos personagens.
-   `character_shortcuts.sql`: Armazena os atalhos dos personagens.
-   `character_skills.sql`: Armazena as habilidades dos personagens.
-   `character_skills_save.sql`: Armazena as habilidades salvas dos personagens.
-   `character_subclasses.sql`: Armazena as subclasses dos personagens.
-   `heroes.sql`: Armazena os heróis da olimpíada.
-   `heroes_diary.sql`: Armazena o diário dos heróis.
-   `olympiad_nobles.sql`: Armazena os nobres da olimpíada.
-   `olympiad_nobles_eom.sql`: Armazena os nobres da olimpíada no final do mês.
-   `olympiad_fights.sql`: Armazena as lutas da olimpíada.
-   `pets.sql`: Armazena os animais de estimação dos personagens.
-   `bookmarks.sql`: Armazena os marcadores de teletransporte dos personagens.
-   `vip_system.sql`: Armazena os dados do sistema VIP.

### 6.2. Tabelas de Clãs

Estes scripts criam as tabelas que armazenam os dados dos clãs.

-   `clan_data.sql`: A tabela principal de dados do clã.
-   `clan_privs.sql`: Armazena os privilégios do clã.
-   `clan_skills.sql`: Armazena as habilidades do clã.
-   `clan_subpledges.sql`: Armazena os sub-juramentos do clã.
-   `clan_wars.sql`: Armazena as guerras de clãs.
-   `clanhall.sql`: A tabela principal do salão do clã.
-   `clanhall_functions.sql`: Armazena as funções do salão do clã.
-   `clanhall_siege_attackers.sql`: Armazena os atacantes do cerco do salão do clã.
-   `clanhall_flagwar_attackers.sql`: Armazena os atacantes da guerra de bandeiras do salão do clã.
-   `clanhall_flagwar_members.sql`: Armazena os membros da guerra de bandeiras do salão do clã.
-   `clanhall_flagwar_owner_npcs.sql`: Armazena os NPCs proprietários da guerra de bandeiras do salão do clã.

### 6.3. Tabelas de Itens

Estes scripts criam as tabelas que armazenam os dados dos itens.

-   `items.sql`: A tabela principal de itens.
-   `items_on_ground.sql`: Armazena os itens no chão.
-   `augmentations.sql`: Armazena as augmentations dos itens.
-   `buylists.sql`: Armazena as listas de compra dos NPCs.
-   `cursed_weapons.sql`: Armazena as armas amaldiçoadas.

### 6.4. Tabelas de NPCs e Spawns

Estes scripts criam as tabelas que armazenam os dados dos NPCs e seus spawns.

-   `spawn_data.sql`: Armazena os dados de spawn dos NPCs.
-   `grandboss_list.sql`: Armazena a lista de grand bosses.
-   `raidboss_spawnlist.sql`: (Não existe no diretório, mas é referenciado no código) Armazena a lista de spawn de raid bosses.

### 6.5. Tabelas de Sieges e Castelos

Estes scripts criam as tabelas que armazenam os dados dos cercos e castelos.

-   `castle.sql`: A tabela principal do castelo.
-   `castle_doorupgrade.sql`: Armazena as atualizações das portas do castelo.
-   `castle_manor_procure.sql`: Armazena a procura do manor do castelo.
-   `castle_manor_production.sql`: Armazena a produção do manor do castelo.
-   `castle_trapupgrade.sql`: Armazena as atualizações das armadilhas do castelo.
-   `siege_clans.sql`: Armazena os clãs do cerco.

### 6.6. Tabelas de Seven Signs

Estes scripts criam as tabelas que armazenam os dados do evento Seven Signs.

-   `seven_signs.sql`: A tabela principal do Seven Signs.
-   `seven_signs_festival.sql`: Armazena os dados do festival do Seven Signs.
-   `seven_signs_status.sql`: Armazena o status do Seven Signs.

### 6.7. Tabelas do Fórum da Comunidade (Community Board)

Estes scripts criam as tabelas para o fórum da comunidade.

-   `bbs_favorite.sql`: Armazena os favoritos do fórum.
-   `bbs_forum.sql`: Armazena os fóruns.
-   `bbs_mail.sql`: Armazena os e-mails do fórum.
-   `bbs_post.sql`: Armazena as postagens do fórum.
-   `bbs_topic.sql`: Armazena os tópicos do fórum.

### 6.8. Tabelas de Sistema e Diversas

Estes scripts criam várias tabelas de sistema e diversas.

-   `auctions.sql`: Armazena os leilões.
-   `autofarm_areas.sql`: Armazena as áreas de auto farm.
-   `autofarm_nodes.sql`: Armazena os nós de auto farm.
-   `buffer_schemes.sql`: Armazena os esquemas de buffer.
-   `fishing_championship.sql`: Armazena o campeonato de pesca.
-   `games.sql`: Armazena os jogos.
-   `gameservers.sql`: Armazena os servidores de jogo.
-   `mdt_bets.sql`: Armazena as apostas do Monster Derby Track.
-   `mdt_history.sql`: Armazena o histórico do Monster Derby Track.
-   `mods_wedding.sql`: Armazena os casamentos.
-   `petition.sql`: Armazena as petições.
-   `petition_message.sql`: Armazena as mensagens das petições.
-   `rainbowsprings_attacker_list.sql`: Armazena a lista de atacantes de Rainbow Springs.
-   `server_memo.sql`: Armazena os memos do servidor.

## 7. Dados do Jogo

A pasta `L2jToren_datapack/data` contém todos os dados do jogo que não estão no banco de dados.

### 7.1. `geodata`

Esta pasta contém os arquivos de geodata, que são usados para controlar o movimento e a linha de visão no mundo do jogo.

### 7.2. `html`

Esta pasta contém os arquivos HTML que são usados para exibir informações no jogo, como diálogos de NPC, janelas da comunidade e muito mais.

### 7.3. `xml`

Esta pasta contém os arquivos XML que são usados para definir vários dados do jogo, como estatísticas de itens, definições de habilidades, spawns de NPC e muito mais.

### 7.4. `serverNames.xml`

Este arquivo contém os nomes dos servidores que são exibidos na tela de login.

## 8. Ferramentas

A pasta `L2jToren_datapack/tools` contém ferramentas para ajudar a instalar e gerenciar o servidor.

-   `database_installer.bat`: Um script de lote do Windows para instalar o banco de dados.
-   `database_installer.sh`: Um script de shell do Linux para instalar o banco de dados.
-   `full_install.sql`: Um script SQL que executa todos os outros scripts SQL para criar e popular o banco de dados.
-   `gs_install.sql`: Um script SQL que executa os scripts SQL necessários para o servidor de jogo.

## 9. Bibliotecas

A pasta `L2jToren_gameserver/lib` contém as bibliotecas Java (.jar) que o servidor de jogo usa.

-   `mariadb-java-client-3.1.4.jar`: O driver JDBC para se conectar a um banco de dados MariaDB ou MySQL.

## 10. Código Fonte (Java)

A pasta `L2jToren_gameserver/java` contém o código fonte do servidor de jogo. O código é organizado em pacotes, seguindo a estrutura `net.sf.l2j`.

### 10.1. `net.sf.l2j.accountmanager`

Este pacote contém as classes relacionadas ao gerenciamento de contas.

### 10.2. `net.sf.l2j.commons`

Este pacote contém classes utilitárias que são usadas em todo o projeto.

### 10.3. `net.sf.l2j.gameserver`

Este é o pacote principal do servidor de jogo. Ele contém a lógica para todos os aspectos do jogo, incluindo personagens, NPCs, itens, habilidades, etc.

### 10.4. `net.sf.l2j.gsregistering`

Este pacote contém as classes relacionadas ao registro do servidor de jogo no servidor de login.

### 10.5. `net.sf.l2j.loginserver`

Este pacote contém as classes para o servidor de login.

### 10.6. `Config.java`

Esta classe é responsável por carregar e armazenar as configurações do servidor.
