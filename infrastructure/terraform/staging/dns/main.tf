# Point * to Kubernetes server
resource "cloudflare_record" "wildcard" {
  zone_id = "5c1425c7c9c3dac30330217af5b3349c"
  name    = "*.dev.${module.project_vars.domain}"
  value   = module.project_vars.cloudflare_zone_id
  type    = "A"
  ttl     = 1
}
