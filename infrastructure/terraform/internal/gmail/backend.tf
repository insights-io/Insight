terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "gmail"
    region = "us-east-1"
  }
}
