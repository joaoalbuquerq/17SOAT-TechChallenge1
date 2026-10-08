provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "mechanics-api"
      ManagedBy   = "terraform"
      Environment = "dev"
    }
  }
}