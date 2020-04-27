terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "staging/browser"
    region = "us-east-1"
  }

  required_version = "~> 0.12"
}
