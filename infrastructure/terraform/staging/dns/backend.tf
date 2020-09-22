terraform {
  backend "s3" {
    bucket = "insights-terraform"
    key    = "staging/dns"
    region = "us-east-1"
  }
}
