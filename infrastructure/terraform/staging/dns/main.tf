# Point * to Kubernetes server
resource "cloudflare_record" "wildcard" {
  zone_id = var.zone_id
  name    = "*${var.domain}"
  value   = "213.161.29.246"
  type    = "A"
  ttl     = 1
}
