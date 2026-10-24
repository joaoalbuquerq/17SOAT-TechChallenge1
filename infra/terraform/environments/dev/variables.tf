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

  validation {
    condition     = length(var.db_password) >= 8 && can(regex("^[!#-.0-?A-~]+$", var.db_password))
    error_message = "A senha deve ter ao menos 8 caracteres ASCII imprimiveis, sem '/', '@', '\"' ou espaco."
  }
}

variable "cluster_name" {
  description = "Nome do cluster EKS."
  type        = string
  default     = "mechanics-api-dev"
}

variable "kubernetes_version" {
  description = "Versao do Kubernetes do cluster EKS."
  type        = string
  default     = "1.37"
}

variable "node_instance_types" {
  description = "Tipos de instancia dos nodes."
  type        = list(string)
  default     = ["c7i-flex.large"]
}

variable "node_desired_size" {
  description = "Quantidade desejada de nodes."
  type        = number
  default     = 2
}

variable "node_min_size" {
  description = "Quantidade minima de nodes."
  type        = number
  default     = 1
}

variable "node_max_size" {
  description = "Quantidade maxima de nodes."
  type        = number
  default     = 3
}

variable "eks_admin_principal_arns" {
  description = "Usuarios/roles IAM com acesso de admin ao cluster, alem de quem cria o cluster."
  type        = list(string)
  default = [
    "arn:aws:iam::570903461728:role/github-actions-mechanics-api-dev",
  ]
}
