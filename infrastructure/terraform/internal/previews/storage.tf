resource "aws_s3_bucket" "frontend_previews" {
  bucket = "frontend-previews"
  acl    = "private"
}

resource "github_actions_secret" "frontend_previews" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "AWS_S3_FRONTEND_PREVIEWS_BUCKET_NAME"
  plaintext_value = aws_s3_bucket.frontend_previews.id
}
