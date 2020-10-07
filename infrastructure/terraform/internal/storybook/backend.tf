terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "storybook"
    region = "us-east-1"
  }
}
