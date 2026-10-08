resource "aws_ecr_repository" "mechanics_api" {
  name                 = "mechanics-api"
  image_tag_mutability = "IMMUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

}