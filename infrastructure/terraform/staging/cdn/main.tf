module "cdn" {
  source      = "../../modules/cdn"
  environment = module.project_vars.environment
  domain      = module.project_vars.domain
}
