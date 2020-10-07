resource "aws_s3_bucket" "storybook" {
  bucket = "insight-${var.project}-storybook"
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

resource "github_actions_secret" "s3_bucket_name" {
  repository      = var.repository
  secret_name     = "AWS_S3_${upper(var.project)}_STORYBOOK"
  plaintext_value = aws_s3_bucket.storybook.id
}
