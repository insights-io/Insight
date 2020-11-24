locals {
  organization_name = "Rebrowse"
  organization_slug = lower(local.organization_name)
}

output "organization_name" {
  value = local.organization_name
}

output "organization_slug" {
  value = local.organization_slug
}

output "github_organization_name" {
  value = "insights-io"
}

output "monorepo_repository" {
  value = "Insight"
}

output "gitops_repository" {
  value = "ops"
}

output "aws_region" {
  value = "us-east-1"
}

output "staging_domain" {
  value = "rebrowse.dev"
}
