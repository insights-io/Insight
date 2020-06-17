locals {
  s3_static_origin_id = "${var.bucket_name}-origin"
  s3_allowed_methods  = ["GET", "HEAD"]
  cf_allowed_methods  = ["GET", "HEAD", "OPTIONS"]
  static_domain       = "static${var.domain_suffix}.${var.domain}"
}

resource "aws_s3_bucket" "static" {
  bucket = var.bucket_name
  region = var.region
  acl    = "private"

  cors_rule {
    allowed_methods = local.s3_allowed_methods
    allowed_origins = ["*"]
    allowed_headers = ["*"]
    max_age_seconds = 3000
  }


  tags = {
    environment = var.environment
  }
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
  aliases             = [local.static_domain]

  default_cache_behavior {
    allowed_methods  = local.cf_allowed_methods
    cached_methods   = local.cf_allowed_methods
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
    #cloudfront_default_certificate = true
    iam_certificate_id = aws_acm_certificate.static_cert.id
  }


  tags = {
    environment = var.environment
  }
}

## Restrict access only to Cloudfront

resource "aws_cloudfront_origin_access_identity" "static_origin_access_identity" {
  comment = "Cloudfront origin access s3 static"
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
