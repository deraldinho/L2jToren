# gemini.md

## Visão Geral do Projeto

O L2jToren é um emulador de servidor de código aberto para o popular MMORPG Lineage 2. Desenvolvido em Java, este projeto permite que entusiastas e desenvolvedores criem, personalizem e hospedem seus próprios servidores privados de Lineage 2. Como um fork da família de projetos L2J (Lineage 2 Java), o L2jToren se beneficia de uma base de código madura e de uma comunidade de desenvolvimento ativa, focada em recriar e expandir a experiência do jogo.

Este tipo de projeto é ideal para quem deseja:

* **Aprender sobre o desenvolvimento de jogos online:** A arquitetura de um emulador de MMORPG é complexa e oferece uma excelente oportunidade de aprendizado.
* **Criar uma experiência de jogo personalizada:** É possível modificar taxas de experiência, drops de itens, NPCs, quests e até mesmo criar eventos e funcionalidades exclusivas.
* **Preservar uma crônica específica do jogo:** Muitos servidores privados se concentram em crônicas clássicas, como Interlude ou High Five, que não estão mais disponíveis nos servidores oficiais.

## Crônicas Populares: Interlude vs. High Five

A escolha da crônica é uma das decisões mais importantes ao criar um servidor de Lineage 2, pois ela define a experiência de jogo. As duas crônicas mais populares no cenário de servidores privados são Interlude e High Five.

### Interlude

* **Foco no "Classic Hardcore":** Interlude é conhecida por sua jogabilidade desafiadora, que exige *grinding* (caça repetitiva de monstros) e uma forte interdependência entre as classes.
* **Mundo Aberto:** A maior parte do conteúdo é encontrada no mundo aberto, incentivando a competição por *spots* de caça e a interação (e o conflito) entre os jogadores.
* **Balanceamento Distinto:** O balanceamento de classes é único e, para muitos, nostálgico. Classes como *Spellsingers* e *Treasure Hunters* têm um papel de destaque no PvP.

### High Five

* **Qualidade de Vida e Acessibilidade:** High Five introduziu inúmeras melhorias de qualidade de vida, como um sistema de *buffs* mais conveniente, novas habilidades e uma interface aprimorada.
* **Conteúdo Instanciado:** Adicionou e popularizou as *instanced dungeons* (masmorras instanciadas), como as versões de Zaken e Freya, permitindo que grupos enfrentem *bosses* sem a interferência de outros jogadores.
* **Balanceamento de Classes Refinado:** Muitas classes receberam novas habilidades e ajustes, resultando em um PvP mais dinâmico e variado.

## Pilha de Tecnologia (Tech Stack)

O L2jToren utiliza uma pilha de tecnologia robusta e bem estabelecida no ecossistema Java:

* **Linguagem de Programação Principal:** [**Java**](https://www.java.com/): Oferece portabilidade entre diferentes sistemas operacionais e um vasto ecossistema de bibliotecas.
* **Ferramenta de Compilação (Build Tool):** [**Maven**](https://maven.apache.org/) ou [**Ant**](https://ant.apache.org/): Ferramentas para gerenciar dependências e o ciclo de vida da compilação.
* **Banco de Dados:** [**MySQL**](https://www.mysql.com/) ou [**MariaDB**](https://mariadb.org/): Armazena todos os dados persistentes do jogo (contas, personagens, itens, etc.).
* **Sistema de Controle de Versão:** [**Git**](https://git-scm.com/): Utilizado para gerenciar o código-fonte, com o projeto hospedado no [GitHub](https://github.com/deraldinho/L2jToren).

## Instalação e Configuração

### Pré-requisitos

* **Java Development Kit (JDK):** Versão 11 ou superior (verifique a documentação do projeto para a versão exata).
* **MySQL Server** ou **MariaDB Server**.
* **Git**.
* Uma ferramenta de gerenciamento de banco de dados como **HeidiSQL** ou **DBeaver**.

### Passos de Instalação

1.  **Clonar o Repositório:**
    ```bash
    git clone [https://github.com/deraldinho/L2jToren.git](https://github.com/deraldinho/L2jToren.git)
    cd L2jToren
    ```
2.  **Compilar o Servidor:** Se o projeto usar Maven, os comandos serão semelhantes a:
    ```bash
    # Compilar o Login Server
    cd login
    mvn clean install

    # Compilar o Game Server
    cd ../game
    mvn clean install
    ```
3.  **Configurar o Banco de Dados:** Crie dois bancos de dados (ex: `l2jls` e `l2jgs`) e execute os scripts SQL de instalação encontrados nos diretórios do servidor.
4.  **Configurar o Servidor:** Edite os arquivos de configuração (geralmente em `config/`) para inserir as credenciais do seu banco de dados e o endereço IP do servidor.
5.  **Executar o Servidor:** Inicie o **Login Server** e, em seguida, o **Game Server**.

## Administração e Melhores Práticas

### Segurança Avançada

* **Proteção Anti-Cheat:** É altamente recomendável o uso de uma solução como **SmartGuard** ou **Active Anticheat** para garantir um jogo justo.
* **Proteção contra DDoS:** Utilize um serviço de mitigação de DDoS de um provedor de hospedagem especializado para manter o servidor online durante ataques.
* **Firewall e Acesso Restrito:** Configure regras de firewall e limite o acesso SSH e ao banco de dados apenas a IPs confiáveis.

### Otimização de Performance

* **Ajuste da JVM (Java Virtual Machine):** Configure o tamanho do *heap* (`-Xms`, `-Xmx`) e experimente diferentes *Garbage Collectors* (como G1GC) para otimizar a performance.
* **Otimização do Banco de Dados:** Aloque memória suficiente para o `innodb_buffer_pool_size` e ative o log de *queries* lentas para identificar e otimizar gargalos.

## Licença

Este projeto é tipicamente distribuído sob a [**GNU General Public License v3.0 (GPLv3)**](https://www.gnu.org/licenses/gpl-3.0.en.html). Isso garante que o software permaneça livre e de código aberto. Verifique o arquivo `LICENSE` no repositório para detalhes.

## Como Contribuir

1.  **Relatando Problemas (Issues):** Use a seção "Issues" do repositório no GitHub para relatar bugs ou sugerir novas funcionalidades, fornecendo o máximo de detalhes possível.
2.  **Submetendo Alterações (Pull Requests):**
    * Faça um **fork** do repositório.
    * Crie uma nova **branch** para sua funcionalidade ou correção.
    * Envie um **Pull Request** para o repositório principal com uma descrição clara das suas alterações.