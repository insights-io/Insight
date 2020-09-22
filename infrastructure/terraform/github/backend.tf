terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "github"
    region = "us-east-1"
  }

  required_version = "0.13.0"
}
