terraform {
  required_providers {
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "2.11.0"
    }
  }
}

provider "aws" {
  version = "3.7.0"
  region  = module.project_vars.region
}

provider "cloudflare" {
  email = "blaz.snuderl@gmail.com"
}
