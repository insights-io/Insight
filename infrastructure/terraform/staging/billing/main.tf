module "billing" {
  source               = "../../modules/billing"
  environment          = module.project_vars.environment
  billing_api_base_url = "https://billing-api.dev.${module.project_vars.domain}"
}
