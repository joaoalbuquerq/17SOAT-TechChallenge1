output "state_bucket_name" {
  description = "Nome do bucket criado para guardar o state do Terraform."
  value       = aws_s3_bucket.terraform_state.bucket
}

output "state_bucket_arn" {
  description = "ARN do bucket criado."
  value       = aws_s3_bucket.terraform_state.arn
}