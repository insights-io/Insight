module "browser" {
  source      = "../../modules/browser"
  bucket_name = "insight-${module.project_vars.environment}-static"
  environment = module.project_vars.environment
  zone_id     = module.project_vars.cloudflare_zone_id
  alias       = "static.dev.${module.project_vars.domain}"
  acme_email  = "blaz.snuderl@gmail.com"
  repository  = module.global_vars.monorepo_repository
}
