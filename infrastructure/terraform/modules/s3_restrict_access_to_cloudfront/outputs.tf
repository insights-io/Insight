output "cloudfront_access_identity_path" {
  value = aws_cloudfront_origin_access_identity.s3_bucket_origin_access_identity.cloudfront_access_identity_path
}
