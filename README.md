# KAP Mechanics API

API REST para o MVP de gestão de uma oficina mecânica. O sistema centraliza o cadastro de clientes, veículos, serviços, itens de estoque, orçamentos e ordens de serviço, reduzindo controles manuais e dando rastreabilidade ao atendimento.

## Objetivo

Este projeto foi desenvolvido para o **Tech Challenge — Fase 1** da FIAP. A solução aplica os conceitos de DDD, qualidade de software e segurança para apoiar o fluxo de atendimento da oficina, desde o orçamento até a entrega do veículo.

## Funcionalidades

- Cadastro e manutenção de clientes, veículos e usuários internos.
- Cadastro de serviços e itens de estoque (peças e insumos).
- Registro e consulta de movimentações de estoque.
- Geração e aprovação de orçamentos.
- Inclusão de serviços e itens de estoque no orçamento, com valores congelados.
- Criação de ordens de serviço a partir de orçamentos aprovados.
- Congelamento dos itens aprovados na O.S. e baixa transacional do estoque ao iniciar a execução.
- Acompanhamento dos status da OS: `RECEBIDA`, `EM_DIAGNOSTICO`, `AGUARDANDO_APROVACAO`, `EM_EXECUCAO`, `FINALIZADA` e `ENTREGUE`.
- Persistência e consulta do histórico de status das ordens de serviço.
- Indicador de tempo médio de execução das OS por serviço.
- Autenticação JWT e controle de acesso por perfil: `ADMIN`, `ATENDENTE` e `ESTOQUISTA`.
- Documentação interativa via Swagger/OpenAPI.

## Tecnologias

- Java 21 e Spring Boot 4.1
- Spring Web, Spring Data JPA, Spring Security e Validation
- PostgreSQL 16 e Flyway
- JWT
- Docker e Docker Compose
- Swagger/OpenAPI (Springdoc)
- JUnit, JaCoCo e SonarQube

## Pré-requisitos

- Java 21
- Docker e Docker Compose

> O Maven Wrapper está incluído no projeto; não é necessário instalar o Maven globalmente.

## Como executar

1. Inicie o ambiente completo:

   ```powershell
   docker compose up --build
   ```

As migrations do Flyway são executadas na inicialização. A API ficará disponível em `http://localhost:8080`.

### Execução híbrida (banco em Docker e API local)

Caso prefira depurar a aplicação pela IDE, inicie apenas o banco e execute a API pelo Maven Wrapper:

   ```powershell
   docker compose up -d db
   .\mvnw.cmd spring-boot:run
   ```

> O `Dockerfile` gera o artefato da API em uma etapa de build e a executa em uma imagem Java reduzida. O serviço `app` do `compose.yaml` conecta-se ao PostgreSQL pelo nome do serviço `db`, formando o ambiente completo exigido pelo projeto.

## Bootstrap da infraestrutura AWS com Terraform

O bootstrap prepara o bucket S3 que armazenará os estados remotos do Terraform. Ele é opcional para executar a API localmente e deve ser executado uma única vez por conta AWS, pela pessoa responsável pela infraestrutura.

### Pré-requisitos

- Terraform `>= 1.10`;
- AWS CLI instalada e autenticada em uma identidade com permissão para criar buckets S3;
- região AWS `us-east-1`.

> Não use credenciais da conta root. Prefira AWS IAM Identity Center (SSO), uma role temporária ou uma identidade IAM com as permissões mínimas necessárias.

### Validar o bootstrap

No terminal, a partir da raiz do projeto, execute:

```powershell
cd .\infra\terraform\bootstrap
terraform fmt
terraform init -backend=false
terraform validate
```

O comando `terraform init -backend=false` inicializa apenas os providers para permitir a validação da configuração. Ele não configura nem acessa o backend S3 e não deve ser usado antes de `terraform plan` ou `terraform apply`.

O nome do bucket é calculado automaticamente com o ID da conta AWS:

```text
mechanics-api-tfstate-<ID_DA_CONTA_AWS>
```

As instruções para o primeiro provisionamento, configuração do backend S3 por conta e migração do state estão em [docs/terraform/terraform-bootstrap.md](docs/terraform/terraform-bootstrap.md).

### Configuração do banco

Por padrão, a aplicação usa o banco criado pelo `compose.yaml`:

| Propriedade | Valor |
| --- | --- |
| Banco | `oficina_db` |
| Usuário | `oficina_user` |
| Porta | `5432` |

Para usar outra instância PostgreSQL, ajuste as propriedades `spring.datasource.*` em `src/main/resources/application.properties`.

### Primeiro acesso local

Em uma base nova, crie o usuário administrador de desenvolvimento abaixo diretamente no PostgreSQL antes de realizar o login:

```sql
INSERT INTO usuario (nome, login, senha_hash, tipo, data_criacao)
VALUES (
    'Administrador Local',
    'admin',
    '$2a$10$Tkvd6E6bTQgy7znCFW3l9eYkH8BZKHbK2XrSw6p8pElb8tz1x1sw6',
    'ADMIN',
    CURRENT_TIMESTAMP
)
ON CONFLICT (login) DO UPDATE
SET nome = EXCLUDED.nome,
    senha_hash = EXCLUDED.senha_hash,
    tipo = EXCLUDED.tipo;
```

Credenciais locais: `admin` / `admin`. Altere ou remova esse usuário fora do ambiente de desenvolvimento.

## Autenticação e documentação da API

O login é realizado em `POST /api/auth/login` com `login` e `senha`. Use o token retornado nas rotas protegidas:

```http
Authorization: Bearer <token>
```

A documentação completa dos endpoints, payloads e respostas está disponível após iniciar a aplicação:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

As rotas de autenticação e documentação são públicas; as demais exigem JWT e respeitam as permissões de cada perfil.

## Principais recursos da API

| Recurso | Base da rota |
| --- | --- |
| Autenticação | `/api/auth` |
| Clientes | `/api/cliente` |
| Veículos | `/api/veiculo` |
| Usuários | `/api/usuario` |
| Serviços | `/api/servico` |
| Itens de estoque | `/api/item-estoque` |
| Movimentações de estoque | `/api/movimentacao-estoque` |
| Orçamentos | `/api/orcamento` |
| Ordens de serviço | `/api/ordem-servico` |
| Histórico de status da OS | `GET /api/ordem-servico/{ordemServicoId}/historico-status` |
| Indicadores | `GET /api/indicadores/tempo-medio-execucao/por-servico/{servicoId}` |

### Itens do orçamento e da O.S.

`POST /api/orcamento/{id}/itens` inclui um serviço ou item de estoque em um orçamento
`PENDENTE`. Informe exatamente um de `servicoId` ou `itemEstoqueId`, além de
`quantidade`. O preço é congelado e o total do orçamento é recalculado.

`POST /api/ordem-servico/{id}/itens` inclui um serviço ou item adicional durante
uma O.S. `EM_EXECUCAO`. A inclusão de um item de estoque, inclusive os componentes
de um serviço, registra imediatamente a saída no ledger e reduz o saldo. Ao entrar
em execução, a O.S. copia os itens aprovados do orçamento e baixa seus itens de
estoque de uma só vez.

Para consultar as linhas e o status use `GET /api/orcamento/{id}/itens` ou
`GET /api/ordem-servico/{id}/itens`. Ambas as respostas trazem `status`, `itens`,
quantidade, preço unitário congelado e total da linha; a resposta da OS também
traz o vínculo `orcamentoItemId` quando a linha veio do orçamento.

## Testes e qualidade

Execute a suíte de testes:

```powershell
.\mvnw.cmd test
```

Para executar os testes e gerar o relatório de cobertura JaCoCo:

```powershell
.\mvnw.cmd verify
```

O relatório fica em `target/site/jacoco/index.html`.

Opcionalmente, inicie o SonarQube local:

```powershell
docker compose --profile quality up -d sonarqube
```

Em seguida, acesse `http://localhost:9000`. No primeiro acesso, entre com `admin` / `admin` e altere a senha solicitada.

Antes da primeira análise, crie manualmente um projeto no SonarQube com a chave:

```text
com.kap:mechanics-api
```

Em **Project settings > Permissions**, conceda ao usuário que executará a análise a permissão **Execute Analysis**. Depois, gere um token em **My Account > Security** e execute:

```powershell
.\mvnw.cmd sonar:sonar "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=SEU_NOVO_TOKEN"
```

Substitua `SEU_NOVO_TOKEN` pelo token criado nessa instância local do SonarQube. Não use colchetes, links Markdown ou barras (`\\`) no comando. Se o projeto tiver sido criado com outra chave, informe-a explicitamente:

```powershell
.\mvnw.cmd sonar:sonar "-Dsonar.host.url=http://localhost:9000" "-Dsonar.projectKey=SUA_CHAVE_DO_PROJETO" "-Dsonar.token=SEU_NOVO_TOKEN"
```

> Se ocorrer o erro de autorização, confirme se a chave do projeto é `com.kap:mechanics-api` e se o token pertence a um usuário com a permissão **Execute Analysis**. Tokens expostos devem ser revogados e substituídos imediatamente.

## Análise de segurança com OWASP ZAP

O projeto também pode ser analisado com o [OWASP ZAP](https://www.zaproxy.org/), uma ferramenta de teste dinâmico de segurança (DAST). Enquanto o SonarQube analisa o código-fonte, o ZAP verifica a API em execução e gera um relatório visual com alertas, risco, evidência e recomendações.

Com o Docker Desktop em execução, inicie a API localmente. Caso o banco ainda não esteja ativo, inicie-o antes:

```powershell
docker compose up -d db
.\mvnw.cmd spring-boot:run
```

Em outro terminal, na raiz do projeto, execute a análise segura baseada na especificação OpenAPI disponível em `http://localhost:8080/v3/api-docs`:

```powershell
docker run --rm -v "${PWD}:/zap/wrk/:rw" -t ghcr.io/zaproxy/zaproxy:stable zap-api-scan.py -t http://host.docker.internal:8080/v3/api-docs -f openapi -S -r zap-api-report.html -I
```

O relatório será gerado como `zap-api-report.html` na raiz do projeto. Abra-o no navegador para visualizar os achados de segurança.

### Análise autenticada com JWT

A análise anterior cobre principalmente endpoints públicos. Para incluir as rotas protegidas, obtenha um JWT de um usuário local com perfil `ADMIN` e passe-o ao ZAP no header `Authorization: Bearer`.

Em outro terminal PowerShell, com a API ainda em execução, gere o token. Substitua os valores de exemplo pelas credenciais locais e não os adicione ao repositório:

```powershell
$body = @{ login = "SEU_LOGIN_ADMIN"; senha = "SUA_SENHA_ADMIN" } | ConvertTo-Json

$token = (
  Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/auth/login" `
    -ContentType "application/json" `
    -Body $body
).token

$token.Length
```

Se o último comando retornar um número maior que zero, execute a análise autenticada:

```powershell
docker run --rm `
  -e "ZAP_AUTH_HEADER_VALUE=Bearer $token" `
  -e "ZAP_AUTH_HEADER_SITE=host.docker.internal" `
  -v "${PWD}:/zap/wrk/:rw" `
  -t ghcr.io/zaproxy/zaproxy:stable `
  zap-api-scan.py `
  -t http://host.docker.internal:8080/v3/api-docs `
  -f openapi `
  -S `
  -r zap-api-report-authenticated.html `
  -I
```

O relatório autenticado será gerado como `zap-api-report-authenticated.html` na raiz do projeto. A presença do JWT permite que o ZAP acesse endpoints compatíveis com o perfil usado; respostas `4xx` ainda podem ocorrer quando uma rota exigir IDs ou dados de negócio específicos.

> O parâmetro `-S` executa apenas a análise segura, sem ataques ativos. Para varredura ativa, remova esse parâmetro **somente em ambiente local ou de homologação autorizado**, pois esse modo pode enviar requisições que alteram dados.

## Relatório de segurança

O relatório consolidado de segurança, com escopo, metodologia, resultados do OWASP ZAP, análise dos achados e espaço para a consolidação do SonarQube, está disponível em [docs/seguranca/relatorio-seguranca.md](docs/seguranca/relatorio-seguranca.md).

Os relatórios HTML do ZAP são evidências geradas localmente e não são versionados; gere-os novamente pelos comandos desta documentação sempre que for necessário atualizar a análise.

## Estrutura do projeto

```text
src/main/java/com/kap/mechanics_api
├── controller     # Endpoints REST
├── service        # Regras de negócio
├── domain         # Entidades do domínio
├── repository     # Persistência
├── dto            # Contratos de entrada e saída
├── security       # Autenticação JWT
└── config         # Segurança e OpenAPI
```

As migrations do banco estão em `src/main/resources/db/migration`.
