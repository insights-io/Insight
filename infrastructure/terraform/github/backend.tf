terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "github"
    region = "us-east-1"
  }
}
