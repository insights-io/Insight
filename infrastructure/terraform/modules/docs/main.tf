resource "aws_s3_bucket" "docs" {
  bucket = var.bucket_name
  acl    = "private"

  cors_rule {
    allowed_methods = ["GET", "HEAD"]
    allowed_origins = ["*"]
    allowed_headers = ["*"]
    max_age_seconds = 3000
  }


  tags = {
    environment = var.environment
  }
}
