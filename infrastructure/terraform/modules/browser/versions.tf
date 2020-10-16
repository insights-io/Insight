terraform {
  required_providers {
    acme = {
      source = "terraform-providers/acme"
      verion = "1.5.0"
    }
    aws = {
      source  = "hashicorp/aws"
      version = "3.11.0"
    }
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "2.11.0"
    }
    github = {
      source  = "hashicorp/github"
      version = "3.1.0"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "2.2.0"
    }
  }
  required_version = ">= 0.13"
}
