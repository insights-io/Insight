locals {
  domain                   = "${var.project}.storybook.${var.domain}"
  alternative_domain_names = ["www.${local.domain}"]
  aliases                  = concat([local.domain], local.alternative_domain_names)
}


data "aws_route53_zone" "zone" {
  name = "${var.domain}."
}

resource "aws_acm_certificate" "certificate" {
  domain_name               = local.domain
  subject_alternative_names = local.alternative_domain_names
  validation_method         = "DNS"
}

resource "aws_route53_record" "validation_records" {
  for_each = {
    for dvo in aws_acm_certificate.certificate.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  name    = each.value.name
  records = [each.value.record]
  type    = each.value.type
  zone_id = data.aws_route53_zone.zone.zone_id
  ttl     = 60
}

resource "aws_acm_certificate_validation" "certificate_validation" {
  certificate_arn         = aws_acm_certificate.certificate.arn
  validation_record_fqdns = [for record in aws_route53_record.validation_records : record.fqdn]
}


resource "aws_route53_record" "cdn_records" {
  zone_id  = data.aws_route53_zone.zone.zone_id
  for_each = toset(local.aliases)
  name     = each.value
  type     = "A"

  alias {
    name                   = aws_cloudfront_distribution.s3_distribution.domain_name
    zone_id                = aws_cloudfront_distribution.s3_distribution.hosted_zone_id
    evaluate_target_health = false
  }
}
