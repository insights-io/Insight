locals {
  admins = ["Meemaw", "snuderl", "botsights"]
}

resource "github_repository" "insight" {
  name           = "Insight"
  description    = "Find insights into your frontend applications."
  default_branch = "master"
  private        = false
  has_issues     = true
  has_wiki       = true
  has_projects   = true

  allow_merge_commit = false
  allow_rebase_merge = false
  allow_squash_merge = true

  delete_branch_on_merge = true
}

module "branch_protection" {
  source     = "../modules/branch_protection"
  repository = github_repository.insight.name
  branch     = github_repository.insight.default_branch
}

resource "github_membership" "admins" {
  for_each = toset(local.admins)
  username = each.value
  role     = "admin"
}
