variable "bucket_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "zone_id" {
  type        = string
  description = "Cloudfront zone_id for which we are setting records"
}

variable "alias" {
  type        = string
  description = "Cloudfront alias (domain) name"
}

variable "repository" {
  type = string
}
