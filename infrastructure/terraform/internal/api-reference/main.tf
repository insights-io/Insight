resource "aws_s3_bucket" "api_reference" {
  bucket = "insight-api-reference"
  acl    = "public-read"

  cors_rule {
    allowed_methods = ["GET", "HEAD"]
    allowed_headers = ["*"]
    allowed_origins = ["*"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }

  tags = {
    name        = "API reference"
    environment = "Internal"
  }
}

resource "github_actions_secret" "s3_bucket_name" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "AWS_S3_API_REFERENCE"
  plaintext_value = aws_s3_bucket.api_reference.id
}
