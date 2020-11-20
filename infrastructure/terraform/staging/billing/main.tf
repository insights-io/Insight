module "billing" {
  source               = "../../modules/billing"
  environment          = module.project_vars.environment
  billing_api_base_url = "https://api.${module.project_vars.domain}"
}
