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

resource "aws_route53_record" "api" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "api.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}
