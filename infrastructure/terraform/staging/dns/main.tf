# Point * to Kubernetes server
resource "cloudflare_record" "wildcard" {
  zone_id = module.project_vars.cloudflare_zone_id
  name    = "*.dev.${module.project_vars.domain}"
  value   = module.project_vars.public_ip
  type    = "A"
  ttl     = 1
}
