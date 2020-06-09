locals {
  infrastructure_maintainers = ["Meemaw", "snuderl"]
}

resource "github_team" "infrastructure" {
  name = "infrastructure"
}

resource "github_team_membership" "infrastructure_maintainers" {
  for_each = toset(local.infrastructure_maintainers)
  team_id  = github_team.infrastructure.id
  username = each.value
  role     = "maintainer"
}
