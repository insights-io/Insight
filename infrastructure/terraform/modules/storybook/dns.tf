locals {
  domain                    = var.domain
  subject_alternative_names = ["www.${local.domain}"]
  aliases                   = concat([local.domain], local.subject_alternative_names)
}

module "certificate" {
  source                    = "../certificate"
  domain                    = local.domain
  subject_alternative_names = local.subject_alternative_names
  zone_id                   = var.zone_id
}

resource "aws_route53_record" "cdn_records" {
  zone_id  = var.zone_id
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
  repository      = var.repository
  secret_name     = "${upper(var.project)}_STORYBOOK_DOMAIN_NAME"
  plaintext_value = local.domain
}
