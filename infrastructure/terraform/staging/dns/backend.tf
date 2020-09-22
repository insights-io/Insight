terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "staging/dns"
    region = "us-east-1"
  }

  required_version = "0.13.3"
}
