locals {
  name = "cert-manager"
  tags = {
    name        = local.name
    environment = module.project_vars.environment
  }
}

resource "aws_iam_policy" "cert_manager" {
  name        = local.name
  path        = "/"
  description = "cert-manager policy (Managed by Terraform)"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "route53:GetChange",
      "Resource": "arn:aws:route53:::change/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "route53:ChangeResourceRecordSets",
        "route53:ListResourceRecordSets"
      ],
      "Resource": "arn:aws:route53:::hostedzone/${aws_route53_zone.staging.zone_id}"
    }
  ]
}
EOF
}

resource "aws_iam_role" "cert_manager" {
  name        = local.name
  description = "${local.name} role (Managed by Terraform)"


  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF

  tags = local.tags
}

resource "aws_iam_user" "cert_manager" {
  name = local.name
  tags = local.tags
}

resource "aws_iam_policy_attachment" "cert_manager" {
  name       = local.name
  users      = [aws_iam_user.cert_manager.name]
  policy_arn = aws_iam_policy.cert_manager.arn
}

resource "aws_iam_access_key" "cert_manager" {
  user = aws_iam_user.cert_manager.name
}

output "secret_access_key" {
  value = aws_iam_access_key.cert_manager.secret
}
