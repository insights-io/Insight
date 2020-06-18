# Point * to Kubernetes server
resource "cloudflare_record" "wildcard" {
  zone_id = "5c1425c7c9c3dac30330217af5b3349c"
  name    = "*dev.snuderls"
  value   = "213.161.29.246"
  type    = "A"
  ttl     = 1
}
