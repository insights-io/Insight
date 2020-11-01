terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "previews"
    region = "us-east-1"
  }
}
