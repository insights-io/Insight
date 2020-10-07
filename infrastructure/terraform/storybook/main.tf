module "app_storybook" {
  source     = "../modules/storybook"
  project    = "app"
  repository = module.global_vars.code_repository_name
}

module "try_storbook" {
  source     = "../modules/storybook"
  project    = "try"
  repository = module.global_vars.code_repository_name
}

module "elements_storybook" {
  source     = "../modules/storybook"
  project    = "elements"
  repository = module.global_vars.code_repository_name
}
