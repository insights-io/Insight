terraform {
  required_providers {
    stripe = {
      source  = "franckverrot/stripe"
      version = "1.6.1"
    }
  }

  required_version = ">= 0.13"
}
