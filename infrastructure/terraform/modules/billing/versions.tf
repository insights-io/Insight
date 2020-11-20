terraform {
  required_providers {
    stripe = {
      source  = "franckverrot/stripe"
      version = "1.7.0"
    }
  }

  required_version = ">= 0.13"
}
