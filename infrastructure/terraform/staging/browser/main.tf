module "browser" {
  source      = "../../modules/browser"
  environment = module.project_vars.environment
  domain      = module.project_vars.domain
  repository  = module.global_vars.monorepo_repository
}
