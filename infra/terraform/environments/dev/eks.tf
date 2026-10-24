resource "aws_eks_cluster" "mechanics_api" {
  name     = var.cluster_name
  version  = var.kubernetes_version
  role_arn = aws_iam_role.eks_cluster.arn

  vpc_config {
    subnet_ids              = data.aws_subnets.eks.ids
    endpoint_public_access  = true
    endpoint_private_access = true
  }

  access_config {
    authentication_mode                         = "API"
    bootstrap_cluster_creator_admin_permissions = true
  }

  depends_on = [aws_iam_role_policy_attachment.eks_cluster]
}

resource "aws_eks_node_group" "mechanics_api" {
  cluster_name    = aws_eks_cluster.mechanics_api.name
  node_group_name = "mechanics-api-dev-nodes"
  node_role_arn   = aws_iam_role.eks_node.arn
  subnet_ids      = data.aws_subnets.eks.ids
  instance_types  = var.node_instance_types
  capacity_type   = "ON_DEMAND"

  scaling_config {
    desired_size = var.node_desired_size
    min_size     = var.node_min_size
    max_size     = var.node_max_size
  }

  update_config {
    max_unavailable = 1
  }

  depends_on = [aws_iam_role_policy_attachment.eks_node]
}

resource "aws_eks_addon" "metrics_server" {
  cluster_name = aws_eks_cluster.mechanics_api.name
  addon_name   = "metrics-server"

  depends_on = [aws_eks_node_group.mechanics_api]
}

resource "aws_eks_access_entry" "admins" {
  for_each = toset(var.eks_admin_principal_arns)

  cluster_name  = aws_eks_cluster.mechanics_api.name
  principal_arn = each.value
}

resource "aws_eks_access_policy_association" "admins" {
  for_each = toset(var.eks_admin_principal_arns)

  cluster_name  = aws_eks_cluster.mechanics_api.name
  principal_arn = each.value
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"

  access_scope {
    type = "cluster"
  }

  depends_on = [aws_eks_access_entry.admins]
}
