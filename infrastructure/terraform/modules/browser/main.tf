locals {
  s3_static_origin_id = "${var.bucket_name}-origin"
}

resource "aws_s3_bucket" "static" {
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

resource "github_actions_secret" "s3_bucket_name" {
  repository      = var.repository
  secret_name     = "AWS_S3_STATIC_${upper(var.environment)}"
  plaintext_value = aws_s3_bucket.static.id
}

resource "aws_cloudfront_distribution" "s3_distribution" {
  origin {
    domain_name = aws_s3_bucket.static.bucket_regional_domain_name
    origin_id   = local.s3_static_origin_id

    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.static_origin_access_identity.cloudfront_access_identity_path
    }
  }

  enabled             = true
  is_ipv6_enabled     = true
  wait_for_deployment = false
  comment             = "${var.bucket_name} (Managed by Terraform)"
  aliases             = [var.alias]

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD", "OPTIONS"]
    target_origin_id = local.s3_static_origin_id

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

  viewer_certificate {
    acm_certificate_arn      = module.certificate.arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2019"
  }


  tags = {
    environment = var.environment
  }
}

resource "github_actions_secret" "cloudfront_distribution_id" {
  repository      = var.repository
  secret_name     = "AWS_CLOUDFRONT_STATIC_${upper(var.environment)}_DISTRIBUTION_ID"
  plaintext_value = aws_cloudfront_distribution.s3_distribution.id
}

## Restrict access only to Cloudfront

resource "aws_cloudfront_origin_access_identity" "static_origin_access_identity" {
  comment = "Cloudfront origin access s3 static (Managed by Terraform)"
}

data "aws_iam_policy_document" "s3_cloudfront_static" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.static.arn}/*"]

    principals {
      type        = "AWS"
      identifiers = [aws_cloudfront_origin_access_identity.static_origin_access_identity.iam_arn]
    }
  }
}

resource "aws_s3_bucket_policy" "s3_cloudfront_static" {
  bucket = aws_s3_bucket.static.id
  policy = data.aws_iam_policy_document.s3_cloudfront_static.json
}
