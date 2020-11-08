terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "staging/cdn"
    region = "us-east-1"
  }
}
