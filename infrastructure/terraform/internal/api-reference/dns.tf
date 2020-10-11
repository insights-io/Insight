locals {
  domain                    = "api-reference.${module.project_vars.domain}"
  subject_alternative_names = ["www.${local.domain}"]
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

resource "github_actions_secret" "domain_name" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "API_REFERENCE_DOMAIN_NAME"
  plaintext_value = local.domain
}
