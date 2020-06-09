locals {
  admins = ["Meemaw", "snuderl", "botsights"]
}

resource "github_membership" "admins" {
  for_each = toset(local.admins)
  username = each.value
  role     = "admin"
}
