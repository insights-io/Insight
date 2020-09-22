terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "staging/billing"
    region = "us-east-1"
  }
}
