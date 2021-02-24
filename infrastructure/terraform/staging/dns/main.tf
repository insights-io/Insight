locals {
  kubernetes_app_records = [
    module.project_vars.domain,
    "www.${module.project_vars.domain}",
    "api.${module.project_vars.domain}",
    "www.api.${module.project_vars.domain}",
    "app.${module.project_vars.domain}",
    "www.app.${module.project_vars.domain}",
    "accounts.${module.project_vars.domain}",
    "www.accounts.${module.project_vars.domain}"
  ]
}

resource "aws_route53_zone" "staging" {
  name = module.project_vars.domain
}

output "zone_id" {
  value = aws_route53_zone.staging.zone_id
}

output "domain" {
  value = module.project_vars.domain
}

module "wildcard_certificate" {
  source  = "../../modules/certificate"
  domain  = "*.${module.project_vars.domain}"
  zone_id = aws_route53_zone.staging.zone_id
}

resource "aws_route53_record" "app_records" {
  for_each = toset(local.kubernetes_app_records)
  zone_id  = aws_route53_zone.staging.zone_id
  name     = each.value
  type     = "A"
  ttl      = "300"
  records  = ["213.161.29.246"] # Point to Kubernetes cluster
}
