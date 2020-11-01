resource "aws_s3_bucket" "app_previews" {
  bucket = "app-previews"
  acl    = "private"
}

resource "github_actions_secret" "app_previews" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "AWS_S3_APP_PREVIEW_BUCKET_NAME"
  plaintext_value = aws_s3_bucket.app_previews.id
}
