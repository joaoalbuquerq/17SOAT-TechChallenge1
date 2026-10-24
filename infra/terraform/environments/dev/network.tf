data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

resource "aws_db_subnet_group" "mechanics_api" {
  name       = "mechanics-api"
  subnet_ids = data.aws_subnets.default.ids

  tags = {
    Name        = "mechanics-api"
    Environment = "dev"
  }

}

resource "aws_security_group" "postgres" {
  name        = "mechanics-api-dev-postgres"
  description = "Acesso ao PostgreSQL do ambiente dev."
  vpc_id      = data.aws_vpc.default.id

  egress {
    description = "Permite trafego de saida."
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "mechanics-api-dev-postgres"
  }
}

data "aws_subnets" "eks" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }

  filter {
    name   = "availability-zone"
    values = ["us-east-1a", "us-east-1b", "us-east-1c", "us-east-1d", "us-east-1f"]
  }
}

resource "aws_vpc_security_group_ingress_rule" "postgres_from_eks" {
  security_group_id            = aws_security_group.postgres.id
  referenced_security_group_id = aws_eks_cluster.mechanics_api.vpc_config[0].cluster_security_group_id
  from_port                    = 5432
  to_port                      = 5432
  ip_protocol                  = "tcp"
  description                  = "PostgreSQL a partir dos nodes do EKS."
}
