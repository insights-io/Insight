terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.9.0"
    }
    cloudflare = {
      source = "cloudflare/cloudflare"
    }
  }
  required_version = ">= 0.13"
}
