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

d### 2.2. Estrutura de Diretórios Atual

A estrutura atual do projeto reflete a transição de um sistema de build baseado em Ant para Gradle, com uma clara separação entre a lógica do servidor e os dados do jogo.

```
/
├── L2jToren_gameserver/      # Módulo principal do servidor do jogo (lógica Java)
│   ├── config/             # Arquivos de configuração (.properties) para o servidor.
│   ├── java/               # Código-fonte Java do emulador.
│   └── build.xml           # Script de build legado (Ant), mantido para referência.
├── L2jToren_datapack/        # Módulo de dados do jogo.
│   ├── data/               # Contém todos os dados utilizados pelo servidor, como definições de NPCs, itens, skills, quests e arquivos HTML para diálogos.
│   └── sql/                # Scripts SQL para a criação e atualização do banco de dados.
├── build.gradle              # Script de build principal do Gradle, que agora orquestra a compilação e o empacotamento dos módulos.
├── settings.gradle           # Define os módulos do projeto para o Gradle (`L2jToren_gameserver` e `L2jToren_datapack`).
├── docker-compose.yml        # Arquivo para facilitar a criação de um ambiente de desenvolvimento com banco de dados MariaDB via Docker.
└── README.md                 # Documentação geral com instruções rápidas de instalação e contribuição.
```

## 3. Funcionalidades Principais

O L2jToren incorpora