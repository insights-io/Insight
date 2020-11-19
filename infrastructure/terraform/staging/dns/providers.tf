provider "aws" {
  region = module.global_vars.aws_region
}

provider "cloudflare" {
  email = "blaz.snuderl@gmail.com"
}
