module "docs" {
  source      = "../../modules/docs"
  bucket_name = "insight-${module.project_vars.environment}-docs"
  environment = module.project_vars.environment
}
