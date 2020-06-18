module "browser" {
  source        = "../../modules/browser"
  bucket_name   = "insight-${module.project_vars.environment}-static"
  environment   = module.project_vars.environment
  region        = module.project_vars.region
  alias         = "static.dev.snuderls.eu"
  acme_email    = "blaz.snuderl@gmail.com"
}
