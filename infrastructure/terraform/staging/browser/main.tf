module "browser" {
  source      = "../../modules/browser"
  bucket_name = "insight-${module.project_vars.environment}-static"
  environment = module.project_vars.environment
  alias       = "static.dev.${module.project_vars.domain}"
  repository  = module.global_vars.monorepo_repository
}
