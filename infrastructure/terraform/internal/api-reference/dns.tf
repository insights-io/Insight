locals {
  domain                    = "api-reference.${module.project_vars.domain}"
  subject_alternative_names = ["www.${local.domain}"]
  aliases                   = concat([local.domain], local.subject_alternative_names)
}

data "aws_route53_zone" "zone" {
  name = "${module.project_vars.domain}."
}

module "certificate" {
  source                    = "../../modules/certificate"
  domain                    = local.domain
  subject_alternative_names = local.subject_alternative_names
  zone_id                   = data.aws_route53_zone.zone.zone_id
}

resource "aws_route53_record" "cdn_records" {
  zone_id  = data.aws_route53_zone.zone.zone_id
  for_each = toset(local.aliases)
  name     = each.value
  type     = "A"

  alias {
    name                   = aws_cloudfront_distribution.s3_distribution.domain_name
    zone_id                = aws_cloudfront_distribution.s3_distribution.hosted_zone_id
    evaluate_target_health = false
  }
}

resource "github_actions_secret" "domain_name" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "API_REFERENCE_DOMAIN_NAME"
  plaintext_value = local.domain
}
