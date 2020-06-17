module "browser" {
  source      = "../../modules/browser"
  bucket_name = "insight-${module.project_vars.environment}-static"
  environment = module.project_vars.environment
  region      = module.project_vars.region
  domain = "snuderls.eu"
  public_ip = "213.161.29.246"
}
