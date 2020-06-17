variable "bucket_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "region" {
  type = string
}

variable "domain" {
  type = string
}

variable "public_ip" {
  type = string
  description = "Public ip the domain should be pointed to."
}

variable "letsencrypt_api_endpoint" {
  default = "https://acme-v02.api.letsencrypt.org/directory"
  description = "API endpoint.  default to prod.  for staging use: https://acme-staging-v02.api.letsencrypt.org/directory"
}

