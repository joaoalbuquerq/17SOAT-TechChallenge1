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