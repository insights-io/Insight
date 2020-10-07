terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "api-reference"
    region = "us-east-1"
  }
}
