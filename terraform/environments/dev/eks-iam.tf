resource "aws_iam_role" "eks_cluster" {
  name = "mechanics-api-dev-eks-cluster"

  assume_role_policy = jsonencode({

    version = "2012-10-17"
    statement = [
      {
        Effect = "Allow"
        principal = {
          service = "eks.amazonaws.com"
        }
        action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "eks_cluster" {
  role       = aws_iam_role.eks_cluster.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}