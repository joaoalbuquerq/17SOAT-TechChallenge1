variable "aws_region" {
  description = "Regiao AWS dos recursos de infraestrutura."
  type        = string
  default     = "us-east-1"
}

variable "db_name" {
  description = "Nome da instância do banco de dados."
  type        = string
  default     = "mechanics"
}

variable "db_username" {
  description = "Nome do usuario administrador do banco."
  type        = string
  default     = "mechanics_admin"
}

variable "db_password" {
  description = "Senha do usuário do banco de dados."
  type        = string
  sensitive   = true

}