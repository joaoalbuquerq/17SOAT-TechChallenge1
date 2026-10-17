# Bootstrap Terraform

Este diretório prepara o bucket S3 que guardará os estados remotos do Terraform da infraestrutura da aplicação. Ele não sobe a API e não é necessário para executar o projeto localmente com Docker Compose.

## Quando usar

Execute o bootstrap uma única vez para cada conta AWS que terá infraestrutura própria. Em uma conta compartilhada pela equipe, a pessoa responsável pela infraestrutura executa esse processo; os demais desenvolvedores usam o fluxo principal de Terraform e o GitHub Actions.

O nome do bucket é calculado automaticamente a partir do ID da conta AWS:

```text
mechanics-api-tfstate-<ID_DA_CONTA_AWS>
```

## Pré-requisitos

- Terraform `>= 1.10`;
- AWS CLI instalada;
- uma identidade AWS com permissão para criar e configurar buckets S3;
- região AWS padrão do projeto: `us-east-1`.

Não use credenciais da conta root. Prefira AWS IAM Identity Center (SSO), uma role temporária ou uma identidade IAM com as permissões mínimas necessárias.

## Validar antes de criar

No terminal, a partir da raiz do projeto:

```powershell
cd .\infra\terraform\bootstrap
terraform fmt
terraform init -backend=false
terraform validate
```

Os comandos acima não criam recursos. `terraform init -backend=false` baixa os providers e prepara a validação, mas pula deliberadamente a configuração do backend remoto.

> `terraform init -backend=false` serve para `terraform validate`. Ele não transforma uma configuração com backend S3 em uma configuração de state local e não deve ser usado antes de `terraform plan`, `terraform apply` ou comandos de state.

## Primeiro provisionamento em uma conta nova

O bucket de state ainda não existe no primeiro provisionamento. Portanto, esse primeiro `plan` e `apply` devem partir de uma configuração que ainda não tenha um bloco `backend "s3"` ativo. O Terraform usa o backend local padrão somente nesse momento inicial.

Após criar o bucket, adicione a configuração de backend e migre o state local para o S3. A separação impede uma dependência circular: o Terraform não pode ler um backend S3 que ele próprio ainda precisa criar.

## Criar o bucket inicial

Depois de inicializar uma configuração sem backend e confirmar a identidade AWS, execute:

```powershell
aws sts get-caller-identity
terraform plan
```

Revise o plano. Ele deve propor a criação do bucket S3, bloqueio de acesso público, versionamento e criptografia. Então execute:

```powershell
terraform apply
```

Confirme o plano digitando `yes`. O bootstrap cria um bucket S3 com:

- acesso público bloqueado;
- versionamento habilitado;
- criptografia em repouso com SSE-S3 (`AES256`);
- proteção contra destruição acidental pelo Terraform.

O comando exibirá o nome e o ARN do bucket ao final.

## Configurar o backend por conta

No primeiro `apply`, o state do bootstrap é criado localmente como `terraform.tfstate`. Após o bucket existir, configure o backend remoto. Os arquivos versionados `backend.tf.example` e `backend.hcl.example` são modelos; as cópias `backend.tf` e `backend.hcl` são locais e não vão para o Git.

Crie suas configurações locais a partir dos modelos:

```powershell
Copy-Item backend.hcl.example backend.hcl
```

Edite `backend.hcl` e informe o bucket da conta que será usada:

```hcl
bucket = "mechanics-api-tfstate-SEU-ID-DA-CONTA"
```

Para uma conta que acabou de criar o bucket, migre o state local para o S3:

```powershell
terraform init -migrate-state -backend-config=backend.hcl
```

Quando o Terraform solicitar a cópia do state local para o backend S3, confirme com `yes`. Não exclua manualmente `terraform.tfstate` antes da migração.

O state remoto passará a ficar neste caminho:

```text
s3://mechanics-api-tfstate-SEU-ID-DA-CONTA/bootstrap/terraform.tfstate
```

### Verificar a migração

Execute os comandos abaixo após migrar o state:

```powershell
terraform state list
terraform plan
```

O primeiro comando deve listar o bucket e suas três configurações de segurança. O segundo deve informar que não há alterações na infraestrutura.

### Fluxos para outras pessoas

Uma pessoa que clonou o projeto e quer uma infraestrutura na própria conta começa sem os arquivos locais de backend, autentica a AWS CLI, executa `terraform init`, cria o bucket com `terraform apply`, configura o próprio backend e migra o state.

Membros da equipe que usam a mesma conta AWS não executam o bootstrap novamente. Eles configuram `backend.hcl` com o bucket compartilhado e inicializam o backend:

```powershell
terraform init -reconfigure -backend-config=backend.hcl
```

O acesso ao bucket é controlado pelas permissões IAM de cada identidade, não pelo clone do repositório.

## Organização recomendada para clones independentes

Para permitir que uma pessoa crie infraestrutura em outra conta AWS, mantenha o backend real fora do Git:

```text
backend.tf.example   # modelo compartilhado do backend S3
backend.hcl.example  # modelo do nome do bucket

backend.tf           # configuração local; ignorada
backend.hcl          # bucket real da conta; ignorado
```

O clone começa sem os arquivos locais de backend, cria o próprio bucket usando state local e, somente depois, copia os arquivos de exemplo, informa o bucket calculado para a própria conta e executa `terraform init -migrate-state -backend-config=backend.hcl`.

## Arquivos do bootstrap

| Arquivo | Finalidade |
| --- | --- |
| `versions.tf` | Fixa as versões compatíveis do Terraform e do provider AWS. |
| `provider.tf` | Configura a região AWS e tags padrão. |
| `variables.tf` | Define a região como valor configurável. |
| `state-bucket.tf` | Declara o bucket e suas proteções. |
| `outputs.tf` | Exibe o nome e o ARN do bucket criado. |

## Git e arquivos locais

Devem ser versionados os arquivos `.tf` da infraestrutura, `backend.tf.example`, `backend.hcl.example` e `.terraform.lock.hcl`.

Não envie ao Git:

```text
.terraform/
*.tfstate
*.tfstate.*
*.tfplan
*.tfvars
backend.tf
backend.hcl
```

Após a criação do bucket, o próximo passo é configurar os ambientes Terraform para armazenarem seus states nesse bucket, em chaves separadas como `dev/terraform.tfstate` e `prod/terraform.tfstate`.
