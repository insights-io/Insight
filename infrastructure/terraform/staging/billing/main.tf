module "billing" {
  source      = "../../modules/billing"
  environment = module.project_vars.environment
}
