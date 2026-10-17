output "ecr_repository_url" {
  description = "URL do repositório ECR que recebe as imagens da mechanics-api."
  value       = aws_ecr_repository.mechanics_api.repository_url
}

resource "aws_ecr_lifecycle_policy" "mechanics_api" {
  repository = aws_ecr_repository.mechanics_api.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Mantem somente as 5 imagens mais recentes."

        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 5
        }

        action = {
          type = "expire"
        }
      }
    ]
  })
}

output "db_address" {
  description = "Endereco do PostgreSQL, sem a porta."
  value       = aws_db_instance.postgres.address
}

output "db_port" {
  description = "Porta do PostgreSQL."
  value       = aws_db_instance.postgres.port
}

output "db_name" {
  description = "Nome do banco da aplicacao."
  value       = aws_db_instance.postgres.db_name
}