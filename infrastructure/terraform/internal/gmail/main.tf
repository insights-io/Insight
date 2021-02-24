locals {
  google_mx_server_records = [
    "1 ASPMX.L.GOOGLE.COM.",
    "5 ALT1.ASPMX.L.GOOGLE.COM.",
    "5 ALT2.ASPMX.L.GOOGLE.COM.",
    "10 ALT3.ASPMX.L.GOOGLE.COM.",
    "10 ALT4.ASPMX.L.GOOGLE.COM.",
    "15 tllayf32mjo6ngrxjcyiw2d6ru3ibla5hd7l5i6kvgbum5ttezaa.mx-verification.google.com."
  ]
}

variable "VERIFICATION_RECORD" {
  type = string
}

data "aws_route53_zone" "zone" {
  name = "${module.project_vars.domain}."
}

resource "aws_route53_record" "gmail_mail_exchange" {
  zone_id = data.aws_route53_zone.zone.zone_id
  name    = ""
  type    = "MX"
  ttl     = 300
  records = local.google_mx_server_records
}
