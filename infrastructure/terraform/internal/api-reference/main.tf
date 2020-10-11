locals {
  tags = {
    name        = "API reference"
    environment = "Internal"
  }
}

resource "aws_s3_bucket" "api_reference" {
  bucket = "insight-api-reference"
  acl    = "private"

  cors_rule {
    allowed_methods = ["GET", "HEAD"]
    allowed_headers = ["*"]
    allowed_origins = ["*"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }

  tags = local.tags
}

resource "github_actions_secret" "s3_bucket_name" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "AWS_S3_API_REFERENCE"
  plaintext_value = aws_s3_bucket.api_reference.id
}

module "s3_restrict_access_to_cloudfront" {
  source        = "../../modules/s3_restrict_access_to_cloudfront"
  s3_bucket_arn = aws_s3_bucket.api_reference.arn
  s3_bucket_id  = aws_s3_bucket.api_reference.id
}

resource "aws_cloudfront_distribution" "s3_distribution" {
  origin {
    domain_name = aws_s3_bucket.api_reference.bucket_regional_domain_name
    origin_id   = "${aws_s3_bucket.api_reference.id}-origin"

    s3_origin_config {
      origin_access_identity = module.s3_restrict_access_to_cloudfront.cloudfront_access_identity_path
    }
  }

  enabled             = true
  is_ipv6_enabled     = true
  wait_for_deployment = false
  comment             = "${aws_s3_bucket.api_reference.id} (Managed by Terraform)"

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD", "OPTIONS"]
    target_origin_id = "${aws_s3_bucket.api_reference.id}-origin"

    forwarded_values {
      query_string = false
      headers = [
        "Origin",
        "Access-Control-Request-Headers",
        "Access-Control-Request-Method",
      ]

      cookies {
        forward = "none"
      }
    }

    min_ttl                = 0
    default_ttl            = 86400
    max_ttl                = 31536000
    compress               = true
    viewer_protocol_policy = "redirect-to-https"
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  aliases = local.aliases

  viewer_certificate {
    acm_certificate_arn      = module.certificate.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }

  tags = local.tags
}

resource "github_actions_secret" "cloudfront_distribution_id" {
  repository      = module.global_vars.monorepo_repository
  secret_name     = "AWS_CLOUDFRONT_API_REFERENCE_DISTRIBUTION_ID"
  plaintext_value = aws_cloudfront_distribution.s3_distribution.id
}
