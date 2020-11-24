output "environment" {
  value = "internal"
}

output "domain" {
  value = module.global_vars.staging_domain
}
