# L2jToren

![L2jToren Logo](https://example.com/logo.png)

## Descrição

O L2jToren é um servidor de Lineage II baseado na engine L2jaCis 409. Nosso objetivo é criar um servidor divertido e estável, com uma comunidade ativa e uma experiência de jogo enriquecedora.

## Índice

- [Requisitos](#requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Uso](#uso)
- [Contribuindo](#contribuindo)
- [Licença](#licença)
- [Contato](#contato)

## Requisitos

Antes de começar, certifique-se de ter os seguintes requisitos instalados no seu sistema:

- **Java Development Kit (JDK)**: Versão 11 ou superior.
- **MySQL**: Versão 5.7 ou superior.
- **Git**: Para clonar o repositório.
- **ANT**: Para compilar o projeto.

## Instalação

Siga os passos abaixo para instalar e configurar o servidor L2jToren:

1. **Clone o Repositório**

   ```bash
   git clone https://github.com/deraldinho/L2jToren.git
   cd L2jToren

Instale as Dependências

Certifique-se de ter o ANT instalado e execute:

mvn clean install
Configure o Banco de Dados

Crie um banco de dados no MySQL com o nome l2jdb ou qualquer outro nome de sua preferência.

Importe o arquivo SQL fornecido no repositório para inicializar o banco de dados:

mysql -u seu_usuario -p seu_banco_de_dados < data/sql/l2jdb.sql
Configurar Arquivos de Configuração

Copie os arquivos de configuração de exemplo para os arquivos de configuração reais:

cp config/server.properties.example config/server.properties
cp config/loginserver.properties.example config/loginserver.properties
cp config/game.properties.example config/game.properties
Edite os arquivos de configuração conforme necessário, especialmente as configurações de banco de dados e servidor.

Configuração
Configuração do Banco de Dados
Edite o arquivo config/server.properties e configure as seguintes propriedades:

jdbcUrl=jdbc:mysql://localhost/seu_banco_de_dados?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
jdbcLogin=seu_usuario
jdbcPassword=sua_senha

Configuração do Servidor
Edite o arquivo config/server.properties e configure as seguintes propriedades:

ServerPort=7777
LoginPort=2106

Uso
Iniciar o Login Server

cd login
./startLoginServer.sh
Iniciar o Game Server

cd game
./startGameServer.sh
Conectar-se ao Servidor

Use o cliente oficial do Lineage II para se conectar ao servidor usando os endereços IP e portas configurados.

Contribuindo
Sua contribuição é muito bem-vinda! Para contribuir com o projeto, siga os passos abaixo:

Faça um Fork do Repositório

Clique no botão "Fork" no canto superior direito do repositório.

Clone o Repositório Forkado

git clone https://github.com/seu_usuario/L2jToren.git
cd L2jToren
Crie uma Nova Branch

git checkout -b feature/nome_da_sua_feature
Faça as Alterações e Commite

git add .
git commit -m "Adiciona nova feature"
Faça Push para o Repositório Forkado

git push origin feature/nome_da_sua_feature
Crie um Pull Request

Vá para o repositório original em GitHub e clique em "New Pull Request". Siga as instruções para criar o pull request.

Licença
Este projeto está licenciado sob a MIT License.

Contato
Desenvolvedores: Lista de Desenvolvedores
Discord: L2jToren Discord
Email: contato@l2jtorren.com
