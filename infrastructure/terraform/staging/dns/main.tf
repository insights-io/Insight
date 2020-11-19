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

resource "aws_route53_record" "api_www" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "www.api.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "app" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "app.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "app_www" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "www.app.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "try" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "try.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "try_www" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "www.try.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}
