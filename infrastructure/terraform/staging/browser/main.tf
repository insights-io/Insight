module "browser" {
  source        = "../../modules/browser"
  bucket_name   = "insight-${module.project_vars.environment}-static"
  environment   = module.project_vars.environment
  region        = module.project_vars.region
  zone_id       = "5c1425c7c9c3dac30330217af5b3349c"
  alias         = "static.dev.snuderls.eu"
  acme_email    = "blaz.snuderl@gmail.com"
}
