resource "github_branch_protection" "branch_protection" {
  repository             = var.repository
  branch                 = var.branch
  enforce_admins         = true
  require_signed_commits = true

  required_status_checks {
    strict = true
  }

  required_pull_request_reviews {
    dismiss_stale_reviews           = true
    required_approving_review_count = 1
  }
}
