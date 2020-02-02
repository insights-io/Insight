module "client" {
  source      = "../../modules/client"
  bucket_name = "insight-${module.project_vars.environment}-static"
  environment = module.project_vars.environment
  region      = module.project_vars.region
}
