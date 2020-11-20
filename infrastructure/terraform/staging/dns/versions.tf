terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.11.0"
    }
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "2.13.2"
    }
  }
  required_version = ">= 0.13"
}
