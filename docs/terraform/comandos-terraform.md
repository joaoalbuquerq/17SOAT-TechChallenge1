# Comandos Terraform usados no projeto

Este guia reúne os comandos usados para administrar o bootstrap e o ambiente `dev` da infraestrutura.

```text
terraform/
├── bootstrap/          # bucket S3 que guarda os states
└── environments/
    └── dev/            # ECR e futuros recursos de desenvolvimento
```

> Execute os comandos sempre dentro da pasta Terraform correta. Não execute `terraform plan`, `apply` ou `destroy` na raiz do repositório.

## Verificar instalação

```powershell
terraform version
aws sts get-caller-identity
```

`terraform version` mostra a versão instalada. `aws sts get-caller-identity` confirma qual identidade e conta AWS serão usadas.

## Formatar e validar

Dentro da pasta que será validada:

```powershell
terraform fmt
terraform validate
```

- `terraform fmt` organiza a formatação dos arquivos `.tf`.
- `terraform validate` confere a sintaxe e as referências da configuração; não cria recursos.

Para validar o bootstrap sem configurar o backend S3:

```powershell
cd .\terraform\bootstrap
terraform init -backend=false
terraform validate
```

`terraform init -backend=false` baixa providers para validação, mas deliberadamente não acessa nem configura o backend remoto. Não o use como substituto da configuração do backend antes de `plan`, `apply` ou comandos de state.

## Inicializar o Terraform

### Primeiro uso de uma pasta sem backend configurado

```powershell
terraform init
```

O comando baixa o provider AWS e cria a pasta local `.terraform/` e o arquivo `.terraform.lock.hcl`.

### Usar o backend remoto já existente

Crie o arquivo local `backend.hcl` a partir do modelo e informe o bucket da conta:

```powershell
Copy-Item backend.hcl.example backend.hcl
```

Exemplo de conteúdo:

```hcl
bucket = "mechanics-api-tfstate-SEU-ID-DA-CONTA"
```

Então inicialize ou reconfigure:

```powershell
terraform init -reconfigure "-backend-config=backend.hcl"
```

Use `-reconfigure` quando trocar de conta AWS, bucket ou backend. Ele não cria recursos.

### Migrar o state local do bootstrap para o S3

Depois que o bucket do bootstrap existir, migre o state local:

```powershell
terraform init -migrate-state "-backend-config=backend.hcl"
```

Quando o Terraform pedir confirmação, informe `yes`. O state do bootstrap passa a ser armazenado em:

```text
bootstrap/terraform.tfstate
```

## Planejar alterações

### Bootstrap

```powershell
cd .\terraform\bootstrap
terraform plan
```

### Ambiente dev

```powershell
cd .\terraform\environments\dev
terraform plan
```

`terraform plan` compara código, state remoto e AWS. Ele não altera recursos. Revise sempre os itens `to add`, `to change` e `to destroy` antes de continuar.

Para retornar código de saída diferente quando houver alterações, útil em automações:

```powershell
terraform plan -detailed-exitcode
```

## Aplicar alterações

Depois de revisar o plano:

```powershell
terraform apply
```

Digite `yes` quando solicitado. O comando cria ou atualiza os recursos declarados naquela pasta.

No GitHub Actions, o plano é salvo e aplicado sem interação:

```bash
terraform init -reconfigure -backend-config="bucket=$TF_STATE_BUCKET"
terraform validate
terraform plan -out=tfplan
terraform apply -auto-approve tfplan
terraform output -raw ecr_repository_url
```

`TF_STATE_BUCKET` é uma Actions Variable que informa o bucket S3 da conta. O arquivo `tfplan` é temporário e não deve ser enviado ao Git. O último comando retorna a URL usada para publicar a imagem no ECR.

## Consultar state e outputs

```powershell
terraform state list
terraform output
terraform output ecr_repository_url
```

- `terraform state list` mostra os recursos administrados pela pasta atual.
- `terraform output` mostra todos os outputs declarados.
- `terraform output ecr_repository_url` retorna a URL do repositório ECR.

## Remover recursos

Primeiro veja o impacto:

```powershell
terraform plan -destroy
```

Para remover os recursos de um ambiente:

```powershell
terraform destroy
```

Use `destroy` somente dentro de `terraform/environments/dev` quando a intenção for apagar os recursos de desenvolvimento. O bootstrap possui `prevent_destroy`; o bucket de state deve permanecer depois que os ambientes forem removidos.

## Resolver lock de state

Se aparecer `Error acquiring the state lock`, não use `-lock=false`.

Primeiro confirme que não existe outro Terraform ou workflow em execução:

```powershell
Get-Process terraform -ErrorAction SilentlyContinue
```

Se não houver execução ativa, use o ID mostrado na mensagem de lock:

```powershell
terraform force-unlock ID_DO_LOCK
```

Exemplo:

```powershell
terraform force-unlock 10534f8b-7297-47f4-a0d3-5041aa45d5e8
```

`force-unlock` remove somente o arquivo de lock; não remove recursos AWS nem altera o state.

## Arquivos locais que não vão ao Git

```text
backend.hcl
.terraform/
terraform.tfstate
terraform.tfstate.backup
tfplan
```

O `backend.tf` define o padrão compartilhado do backend e pode ser versionado. O `backend.hcl` informa o bucket específico de cada conta e permanece local.
