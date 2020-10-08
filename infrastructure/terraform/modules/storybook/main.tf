locals {
  bucket_name      = "insight-${var.project}-storybook"
  bucket_origin_id = "insight-${var.project}-storybook-origin"
  tags = {
    name        = "Storybook"
    environment = "Internal"
  }
}


resource "aws_s3_bucket" "storybook" {
  bucket = local.bucket_name
  acl    = "private"


  cors_rule {
    allowed_headers = ["*"]
    allowed_origins = ["*"]
    allowed_methods = ["GET", "HEAD"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }

  tags = local.tags
}

resource "github_actions_secret" "s3_bucket_name" {
  repository      = var.repository
  secret_name     = "AWS_S3_${upper(var.project)}_STORYBOOK"
  plaintext_value = aws_s3_bucket.storybook.id
}

resource "aws_cloudfront_distribution" "s3_distribution" {
  origin {
    domain_name = aws_s3_bucket.storybook.bucket_regional_domain_name
    origin_id   = local.bucket_origin_id

    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.bucket_origin_access_identity.cloudfront_access_identity_path
    }
  }

  enabled             = true
  is_ipv6_enabled     = true
  wait_for_deployment = false
  comment             = "${aws_s3_bucket.storybook.id} (Managed by Terraform)"

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD", "OPTIONS"]
    target_origin_id = local.bucket_origin_id

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
    cloudfront_default_certificate = true
  }


  tags = local.tags
}

resource "github_actions_secret" "cloudfront_distribution_id" {
  repository      = var.repository
  secret_name     = "AWS_CLOUDFRONT_${upper(var.project)}_STORYBOOK_DISTRIBUTION_ID"
  plaintext_value = aws_cloudfront_distribution.s3_distribution.id
}

## Restrict access only to Cloudfront
resource "aws_cloudfront_origin_access_identity" "bucket_origin_access_identity" {
  comment = "Cloudfront origin access s3 static (Managed by Terraform)"
}

data "aws_iam_policy_document" "s3_cloudfront" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.storybook.arn}/*"]

    principals {
      type        = "AWS"
      identifiers = [aws_cloudfront_origin_access_identity.bucket_origin_access_identity.iam_arn]
    }
  }
}

resource "aws_s3_bucket_policy" "s3_cloudfront" {
  bucket = aws_s3_bucket.storybook.id
  policy = data.aws_iam_policy_document.s3_cloudfront.json
}

resource "github_actions_secret" "domain_name" {
  repository      = var.repository
  secret_name     = "${upper(var.project)}_STORYBOOK_DOMAIN_NAME"
  plaintext_value = aws_cloudfront_distribution.s3_distribution.domain_name
}
