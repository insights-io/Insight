provider "aws" {
  version = "~> 2.0"
  region  = module.project_vars.region
}

provider "cloudflare" {
  version = "~> 2.0"
  email   = "blaz.snuderl@gmail.com"
  api_key = "${var.CF_API_KEY}"
}

variable "CF_API_KEY" {}
