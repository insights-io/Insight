provider "aws" {
  version = "~> 2.0"
  region  = module.project_vars.region
}
