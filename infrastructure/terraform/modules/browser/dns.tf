resource "cloudflare_record" "static" {
  zone_id = var.zone_id
  name    = var.alias
  value   = aws_cloudfront_distribution.s3_distribution.domain_name
  type    = "CNAME"
  ttl     = 1
}

provider "acme" {
  server_url = var.letsencrypt_api_endpoint
}

resource "tls_private_key" "static_private_key" {
  algorithm = "RSA"
}

resource "acme_registration" "reg" {
  account_key_pem = tls_private_key.static_private_key.private_key_pem
  email_address   = var.acme_email
}

resource "acme_certificate" "static_cert" {
  account_key_pem = acme_registration.reg.account_key_pem
  common_name     = var.alias

  dns_challenge {
    provider = "cloudflare"
  }
}

resource "aws_acm_certificate" "static_cert" {
  private_key       = acme_certificate.static_cert.private_key_pem
  certificate_body  = acme_certificate.static_cert.certificate_pem
  certificate_chain = acme_certificate.static_cert.issuer_pem
}