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

resource "github_branch_protection" "insight_master" {
  repository     = github_repository.insight.name
  branch         = "master"
  enforce_admins = true


  required_status_checks {
    strict = true
  }

  required_pull_request_reviews {
    dismiss_stale_reviews           = true
    required_approving_review_count = 1
  }
}

resource "github_membership" "snuderl_membership" {
  username = "snuderl"
  role     = "admin"
}
