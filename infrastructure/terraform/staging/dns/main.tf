# Point * to Kubernetes server
resource "cloudflare_record" "wildcard" {
  zone_id = var.zone_id
  name    = "*${var.domain}"
  value   = var.public_ip
  type    = "A"
  ttl     = 1
}
