provider "aws" {
  region = module.project_vars.region
}

provider "cloudflare" {
  email = "blaz.snuderl@gmail.com"
}
