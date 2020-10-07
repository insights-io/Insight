resource "aws_s3_bucket" "storybook" {
  bucket = "insight-storybook"
  acl    = "public-read"


  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "HEAD"]
    allowed_origins = ["*"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }

  tags = {
    name        = "Storybook"
    environment = "Internal"
  }
}
