# L2jToren

O **L2jToren** é um emulador de servidor de código aberto para o MMORPG Lineage 2, desenvolvido em Java. O projeto é um fork da família L2J e foca em estabilidade, novas funcionalidades e uma experiência de jogo aprimorada.

## Funcionalidades Exclusivas

Este projeto se destaca por suas modificações customizadas, que incluem:

- **Ícone de Data no Inventário:** Exibe a data de aquisição nos itens.
- **Sistema de Auto-Farm:** Permite a configuração de farm automático em áreas designadas.
- **Comandos de Voz:**
  - `.online`: Mostra o número de jogadores online.
  - `.menu`: Abre um painel de configurações do jogador (recusar trades, buffs, etc.).
  - `.deposit`/`.withdraw`: Acessa um sistema de banco virtual.
- **NPC Buffer:** Um NPC customizável que fornece buffs aos jogadores.

## Pilha de Tecnologia

- **Linguagem:** Java 11+
- **Build Tool:** Ant
- **Banco de Dados:** MySQL ou MariaDB
- **Controle de Versão:** Git

## Instalação Rápida

### Pré-requisitos

- JDK 21 ou superior
- MySQL/MariaDB Server
- Git

### 1. Clone o Repositório

```bash
git clone https://github.com/deraldinho/L2jToren.git
cd L2jToren
```

### 2. Compile o Servidor

```bash
cd L2jToren_gameserver
ant dist
```

Isso irá gerar os builds do Login Server e do Game Server no diretório `L2jToren_gameserver/dist`.

### 3. Configure o Banco de Dados

1. **Crie dois bancos de dados** (ex: `l2jls_toren` e `l2jgs_toren`).
2. **Importe o arquivo** `L2jToren_datapack/tools/full_install.sql` para popular o banco de dados do Game Server.

### 4. Configure os Servidores

Edite os arquivos abaixo com as informações do seu banco de dados e IP:

- `L2jToren_gameserver/config/loginserver.properties`
- `L2jToren_gameserver/config/server.properties`

### 5. Inicie os Servidores

- **Login Server:**

  ```bash
  cd L2jToren_gameserver/dist/login
  ./startLoginServer.sh  # ou startLoginServer.bat para Windows
  ```

- **Game Server:**

  ```bash
  cd L2jToren_gameserver/dist/gameserver
  ./startGameServer.sh  # ou startGameServer.bat para Windows
  ```

## Como Contribuir

Sua contribuição é muito bem-vinda!

1. **Faça um Fork** deste repositório.
2. Crie uma nova **Branch** (`git checkout -b feature/sua-feature`).
3. **Commite** suas alterações (`git commit -m 'Adiciona nova feature'`).
4. **Faça o Push** para a sua branch (`git push origin feature/sua-feature`).
5. Abra um **Pull Request**.

## Licença

Este projeto está licenciado sob a [GNU General Public License v3.0 (GPLv3)](https://www.gnu.org/licenses/gpl-3.0.en.html).

## Contato

- **Discord:** L2jToren Discord
- **Email:** [contato@l2jtorren.com](mailto:contato@l2jtorren.com)