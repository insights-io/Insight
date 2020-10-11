variable "domain" {
  type = string
}

variable "zone_id" {
  type = string
}

variable "subject_alternative_names" {
  type = list(string)
}
