locals {
  bucket_name   = "rebrowse-${var.environment}-cdn"
  bucket_origin = "${local.bucket_name}-origin"
  cdn_domain    = "cdn.${var.domain}"
  aliases       = [local.cdn_domain, "www.${local.cdn_domain}"]
  tags = {
    environment = var.environment
  }
}

resource "aws_s3_bucket" "cdn" {
  bucket = local.bucket_name
  acl    = "private"

  cors_rule {
    allowed_methods = ["GET", "HEAD"]
    allowed_origins = ["*"]
    allowed_headers = ["*"]
    max_age_seconds = 3000
  }

  tags = local.tags
}

data "aws_route53_zone" "zone" {
  name = "${var.domain}."
}

module "certificate" {
  source                    = "../../modules/certificate"
  domain                    = local.cdn_domain
  subject_alternative_names = ["www.${local.cdn_domain}"]
  zone_id                   = data.aws_route53_zone.zone.zone_id
}

module "s3_restrict_access_to_cloudfront" {
  source        = "../../modules/s3_restrict_access_to_cloudfront"
  s3_bucket_arn = aws_s3_bucket.cdn.arn
  s3_bucket_id  = aws_s3_bucket.cdn.id
}

resource "aws_cloudfront_distribution" "cdn" {
  origin {
    domain_name = aws_s3_bucket.cdn.bucket_regional_domain_name
    origin_id   = local.bucket_origin

    s3_origin_config {
      origin_access_identity = module.s3_restrict_access_to_cloudfront.cloudfront_access_identity_path
    }
  }

  enabled             = true
  is_ipv6_enabled     = true
  wait_for_deployment = false
  comment             = "Rebrowse ${var.environment} CDN distribution (Managed by Terraform)"
  aliases             = local.aliases

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD", "OPTIONS"]
    target_origin_id = local.bucket_origin

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

  tags = local.tags
}

resource "aws_route53_record" "cdn_aliases" {
  zone_id  = data.aws_route53_zone.zone.zone_id
  for_each = toset(local.aliases)
  name     = each.value
  type     = "A"

  alias {
    name                   = aws_cloudfront_distribution.cdn.domain_name
    zone_id                = aws_cloudfront_distribution.cdn.hosted_zone_id
    evaluate_target_health = false
  }
}
