terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.16.0"
    }
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "2.11.0"
    }
  }
  required_version = ">= 0.13"
}
