locals {
  projects = ["app", "try", "elements"]
}

module "storybooks" {
  source     = "../../modules/storybook"
  for_each   = toset(local.projects)
  project    = each.key
  repository = module.global_vars.monorepo_repository
}
