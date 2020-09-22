terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "management/budget"
    region = "us-east-1"
  }
}
