provider "aws" {
  region = module.global_vars.aws_region
}

provider "github" {
  organization = module.global_vars.organization_name
}

