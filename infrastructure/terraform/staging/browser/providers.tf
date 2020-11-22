provider "aws" {
  region = module.global_vars.aws_region
}

provider "github" {
  organization = module.global_vars.github_organization_name
}
