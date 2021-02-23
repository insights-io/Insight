locals {
  projects = ["app", "accounts", "elements", "homepage"]
}

data "aws_route53_zone" "zone" {
  name = "${module.project_vars.domain}."
}

module "storybooks" {
  source            = "../../modules/storybook"
  for_each          = toset(local.projects)
  project           = each.key
  repository        = module.global_vars.monorepo_repository
  domain            = "${each.key}.storybook.${module.project_vars.domain}"
  zone_id           = data.aws_route53_zone.zone.zone_id
  organization_slug = module.global_vars.organization_slug
  environment       = module.project_vars.environment
}

module "composition_storybook" {
  source            = "../../modules/storybook"
  project           = "composition"
  repository        = module.global_vars.monorepo_repository
  domain            = "storybook.${module.project_vars.domain}"
  zone_id           = data.aws_route53_zone.zone.zone_id
  organization_slug = module.global_vars.organization_slug
  environment       = module.project_vars.environment
}
