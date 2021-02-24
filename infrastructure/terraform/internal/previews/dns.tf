data "aws_route53_zone" "zone" {
  name = "${module.project_vars.domain}."
}

module "certificate" {
  source  = "../../modules/certificate"
  domain  = "*.app.${module.project_vars.domain}"
  zone_id = data.aws_route53_zone.zone.zone_id
}

resource "github_actions_secret" "certificate_arn" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "AWS_APP_PREVIEW_CERTIFICATE_ARN"
  plaintext_value = module.certificate.arn
}

resource "github_actions_secret" "hosted_zone_id" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "AWS_DEV_HOSTED_ZONE_ID"
  plaintext_value = data.aws_route53_zone.zone.id
}

resource "github_actions_secret" "dev_domain" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "DEV_DOMAIN_NAME"
  plaintext_value = module.project_vars.domain
}

module "accounts_previews_wildcard_certificate" {
  source  = "../../modules/certificate"
  domain  = "*.accounts.${module.project_vars.domain}"
  zone_id = data.aws_route53_zone.zone.zone_id
}

resource "github_actions_secret" "accounts_previews_wildcard_certificate_arn" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "AWS_ACCOUNTS_PREVIEW_CERTIFICATE_ARN"
  plaintext_value = module.accounts_previews_wildcard_certificate.arn
}

data "aws_acm_certificate" "wildcard_certificate" {
  domain = "*.${module.project_vars.domain}"
}

resource "github_actions_secret" "homepage_previews_wildcard_certificate_arn" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "AWS_HOMEPAGE_PREVIEW_CERTIFICATE_ARN"
  plaintext_value = data.aws_acm_certificate.wildcard_certificate.arn
}

