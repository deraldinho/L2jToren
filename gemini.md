# Guia do Projeto L2jToren

Bem-vindo ao **L2jToren**, um emulador de servidor de código aberto para o MMORPG Lineage 2. Este guia foi criado para ajudar desenvolvedores e entusiastas a configurar, personalizar e contribuir com o projeto.

## Índice

- [Visão Geral do Projeto](#visão-geral-do-projeto)
- [Funcionalidades Exclusivas](#funcionalidades-exclusivas)
- [Crônicas Populares](#crônicas-populares-interlude-vs-high-five)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pilha de Tecnologia (Tech Stack)](#pilha-de-tecnologia-tech-stack)
- [Instalação e Configuração](#instalação-e-configuração)
- [Administração e Melhores Práticas](#administração-e-melhores-práticas)
- [Licença](#licença)
- [Como Contribuir](#como-contribuir)

## Visão Geral do Projeto

O L2jToren é um fork da família de projetos L2J (Lineage 2 Java), focado em recriar e expandir a experiência do jogo. Desenvolvido em Java, ele permite que qualquer pessoa crie e hospede seu próprio servidor privado de Lineage 2.

Este projeto é ideal para:

- **Aprender sobre desenvolvimento de jogos online:** A arquitetura de um emulador de MMORPG é uma excelente oportunidade de aprendizado.
- **Criar uma experiência de jogo personalizada:** Modifique taxas, drops, NPCs, quests e crie eventos exclusivos.
- **Preservar uma crônica específica:** Muitos servidores se concentram em crônicas clássicas, como Interlude ou High Five.

## Funcionalidades Exclusivas

Este projeto inclui as seguintes modificações e funcionalidades customizadas:

- **Ícone de Data no Inventário:** Exibe a data de aquisição nos itens.
- **Sistema de Auto-Farm:** Permite a configuração de farm automático em áreas designadas.
- **Comandos de Voz:**
  - `.online`: Mostra o número de jogadores online.
  - `.menu`: Abre um painel de configurações do jogador com opções para:
    - Recusar convites de grupo (Party Refusal).
    - Recusar negociações (Trade Refusal).
    - Recusar buffs (Buffs Refusal).
    - Bloquear mensagens privadas (Message Refusal).
    - Conversão automática de Adena para Gold Bar (Auto GoldBar).
  - `.deposit`/`.withdraw`: Sistema de banco para guardar itens.
- **NPC Buffer:** Um NPC que fornece buffs aos jogadores.

## Crônicas Populares: Interlude vs. High Five

A escolha da crônica define a experiência de jogo. As mais populares são:

### Interlude

- **Foco no "Classic Hardcore":** Jogabilidade desafiadora que exige *grinding* e forte interdependência entre as classes.
- **Mundo Aberto:** Incentiva a competição por *spots* de caça e a interação entre jogadores.
- **Balanceamento Distinto:** Classes como *Spellsingers* e *Treasure Hunters* têm um papel de destaque no PvP.

### High Five

- **Qualidade de Vida:** Introduziu melhorias como um sistema de *buffs* mais conveniente e uma interface aprimorada.
- **Conteúdo Instanciado:** Popularizou as *instanced dungeons*, permitindo que grupos enfrentem *bosses* sem interferência.
- **Balanceamento Refinado:** Resultou em um PvP mais dinâmico e variado.

## Estrutura do Projeto

O repositório está organizado nos seguintes diretórios principais:

- **`L2jToren_gameserver/`**: Contém o código-fonte do servidor do jogo (Game Server) e do servidor de login (Login Server). É aqui que a lógica principal do jogo é implementada.
- **`L2jToren_datapack/`**: Contém todos os dados que o servidor utiliza, como definições de NPCs, itens, skills, quests, mapas e configurações de geodata.

## Pilha de Tecnologia (Tech Stack)

- **Linguagem Principal:** [**Java**](https.www.java.com/) (Versão 11+)
- **Ferramenta de Compilação:** [**Ant**](https://ant.apache.org/)
- **Banco de Dados:** [**MySQL**](https://www.mysql.com/) ou [**MariaDB**](https://mariadb.org/)
- **Controle de Versão:** [**Git**](https://git-scm.com/)

## Instalação e Configuração

### Pré-requisitos

- **Java Development Kit (JDK):** Versão 21 ou superior.
- **MySQL Server** ou **MariaDB Server**.
- **Git**.
- Uma ferramenta de gerenciamento de banco de dados (ex: **HeidiSQL**, **DBeaver**).

### 1. Clonar o Repositório

```bash
git clone https://github.com/deraldinho/L2jToren.git
cd L2jToren
```

### 2. Compilar o Servidor

O projeto utiliza **Ant** para compilação. O `build.xml` principal está no diretório `L2jToren_gameserver`.

```bash
cd L2jToren_gameserver
ant dist
```

Este comando irá compilar o código-fonte e criar os diretórios de distribuição (`dist`) com os arquivos necessários para executar os servidores.

### 3. Configurar o Banco de Dados

1.  Crie dois bancos de dados no seu servidor MySQL/MariaDB. Use nomes descritivos, como `l2jls_toren` (para o Login Server) e `l2jgs_toren` (para o Game Server).
2.  Execute o script de instalação completa, localizado em `L2jToren_datapack/tools/full_install.sql`, para criar todas as tabelas necessárias.

### 4. Configurar os Servidores

Edite os arquivos de configuração em `L2jToren_gameserver/config/` para inserir as credenciais do seu banco de dados e o endereço IP do servidor.

- **Para o Login Server:** Edite `loginserver.properties`.
- **Para o Game Server:** Edite `server.properties`.

### 5. Executar os Servidores

Após a compilação, os scripts de inicialização estarão no diretório `L2jToren_gameserver/dist/`.

**Para iniciar o Login Server:**

```bash
cd L2jToren_gameserver/dist/login
./startLoginServer.sh  # Para Linux/macOS
startLoginServer.bat   # Para Windows
```

**Para iniciar o Game Server:**

```bash
cd L2jToren_gameserver/dist/gameserver
./startGameServer.sh  # Para Linux/macOS
startGameServer.bat   # Para Windows
```

## Uso com Docker (Opcional)

Para facilitar a configuração do ambiente, o projeto inclui um arquivo `docker-compose.yml` que provisiona um banco de dados MariaDB e uma ferramenta de gerenciamento (Adminer). É uma alternativa à instalação manual do MySQL/MariaDB.

### Pré-requisitos

- **Docker** e **Docker Compose** instalados.

### Como Usar

1.  **Iniciar os Contêineres:**

    Na raiz do projeto, execute o comando:

    ```bash
    docker-compose up -d
    ```

    Isso irá baixar as imagens e iniciar os serviços do MariaDB e do Adminer em segundo plano.

2.  **Acessar o Banco de Dados:**

    - **Via Adminer:** Acesse `http://localhost:8080` em seu navegador.
    - **Credenciais:**
        - **Sistema:** MariaDB
        - **Servidor:** L2jToren
        - **Usuário:** L2jToren
        - **Senha:** 9643

3.  **Configurar o Servidor:**

    Ao configurar os arquivos `loginserver.properties` e `server.properties`, use as credenciais e o nome do serviço Docker (`L2jToren`) como o host do banco de dados.

    **Importante:** O `Dockerfile` no projeto está atualmente vazio e serve como um ponto de partida para quem desejar containerizar os servidores do jogo no futuro.

## Administração e Melhores Práticas

### Segurança

- **Proteção Anti-Cheat:** Considere usar soluções como **SmartGuard** ou **Active Anticheat**.
- **Proteção contra DDoS:** Utilize um serviço de mitigação de DDoS.
- **Firewall:** Limite o acesso SSH e ao banco de dados apenas a IPs confiáveis.

### Visão Geral dos Arquivos de Configuração

A pasta `L2jToren_gameserver/config/` contém todos os arquivos de configuração do servidor. Abaixo está uma descrição dos arquivos mais importantes:

-   **`server.properties`**: Configurações principais do Game Server, como taxas (XP, SP, drop), endereço do servidor, número máximo de jogadores e configurações de rates.
-   **`loginserver.properties`**: Configurações do Login Server, incluindo o IP, a porta e as configurações de conexão com o banco de dados de contas.
-   **`autofarm.properties`**: Ativa e configura o sistema de auto-farm, incluindo limites, custos e comportamento da IA.
-   **`clans.properties`**: Define regras para clãs e alianças, como tempo de espera para criar/entrar em um clã e número máximo de membros.
-   **`events.properties`**: Controla eventos automáticos como Olympiads, Seven Signs e loterias.
-   **`npcs.properties`**: Configurações relacionadas a NPCs, como o Class Master, NPC Buffer, sistema de casamento e comportamento de bosses.
-   **`players.properties`**: Ajustes que afetam diretamente os jogadores, como limites de inventário, chances de encantamento, regras de Karma/PvP e aprendizado de skills.
-   **`siege.properties`**: Define as regras para cercos a castelos e clã halls.
-   **`vip.properties`**: Configura o sistema VIP, incluindo os bônus de cada nível e como os jogadores obtêm o status VIP.
-   **`VoicedCommand.properties`**: Ativa e configura os comandos de voz, como `.online` e o sistema de banco.
-   **`geoengine.properties`**: Configurações do motor de geodata, essencial para o cálculo de movimento e linha de visão no mundo do jogo.
-   **`logging.properties`**: Define como os logs do servidor são gerados e armazenados, útil para depuração e monitoramento.

### Otimização

- **Ajuste da JVM:** Configure o tamanho do *heap* (`-Xms`, `-Xmx`) e experimente diferentes *Garbage Collectors* (como G1GC).
- **Otimização do Banco de Dados:** Aloque memória suficiente para o `innodb_buffer_pool_size` e monitore *queries* lentas.

### Melhorias no Sistema de Logging

O sistema de logging foi aprimorado para oferecer maior clareza, rastreabilidade e robustez no registro de eventos e erros. As principais melhorias incluem:

-   **Maior Especificidade no Tratamento de Exceções:** Substituição de capturas genéricas de exceções (`catch (Exception e)`) por tipos de exceção mais específicos (ex: `IOException`, `GeneralSecurityException`, `SQLException`, `UnknownHostException`). Isso permite um tratamento de erros mais preciso e evita a supressão de problemas inesperados.
-   **Registro de Erros Aprimorado:** Troca de `e.printStackTrace()` ou falhas silenciosas por chamadas explícitas a `LOGGER.error`. Agora, as mensagens de erro incluem o objeto da exceção, fornecendo informações de depuração mais detalhadas e facilitando a identificação da causa raiz dos problemas.
-   **Registro Informativo Melhorado:** Adição de mensagens `LOGGER.info` e `LOGGER.warn` para eventos importantes do sistema, como interrupções de threads, desconexões de clientes, falhas de checksum e processos de desligamento do servidor. Isso melhora a visibilidade do comportamento do servidor e auxilia na monitorização.
-   **Padronização do Logging:** Substituição de `System.out.println` por `LOGGER.warn` para o registro de opcodes desconhecidos. Todas as mensagens de log agora utilizam a estrutura de logging configurada, garantindo consistência e permitindo o controle centralizado dos níveis de log e destinos.
-   **Nome do Logger Corrigido:** O nome do logger na classe `IpBanManager.java` foi corrigido para refletir corretamente a classe, garantindo que as mensagens de log dessa classe sejam atribuídas ao logger apropriado.

## Licença

Este projeto é distribuído sob a [**GNU General Public License v3.0 (GPLv3)**](https://www.gnu.org/licenses/gpl-3.0.en.html).

## Como Contribuir

1.  **Relate Problemas (Issues):** Use a seção "Issues" do GitHub para relatar bugs ou sugerir funcionalidades.
2.  **Submeta Alterações (Pull Requests):**
    - Faça um **fork** do repositório.
    - Crie uma nova **branch** para sua alteração.
    - Envie um **Pull Request** com uma descrição clara do que foi feito.
