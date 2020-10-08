resource "github_repository" "monorepo" {
  name           = module.global_vars.monorepo_repository
  description    = "Find insights into your frontend applications."
  default_branch = "master"
  visibility     = "public"
  has_issues     = true
  has_wiki       = true
  has_projects   = true

  allow_merge_commit = false
  allow_rebase_merge = false
  allow_squash_merge = true

  delete_branch_on_merge = true
}

module "branch_protection" {
  source     = "../../modules/branch_protection"
  repository = github_repository.monorepo.name
  branch     = github_repository.monorepo.default_branch
}

resource "github_repository_webhook" "atlantis_webhook" {
  repository = github_repository.monorepo.name

  configuration {
    url          = "https://atlantis.dev.snuderls.eu/events"
    content_type = "json"
    insecure_ssl = false
    secret       = "********"
  }

  active = true
  events = ["issue_comment", "pull_request", "pull_request_review", "push"]
}

resource "github_actions_secret" "aws_region" {
  repository      = github_repository.monorepo.name
  secret_name     = "AWS_REGION"
  plaintext_value = module.global_vars.aws_region
}

resource "github_repository" "ops" {
  name           = module.global_vars.gitops_repository
  description    = "Insight operations"
  default_branch = "master"
  visibility     = "private"
  has_issues     = true
  has_wiki       = false
  has_projects   = false

  allow_merge_commit = false
  allow_rebase_merge = false
  allow_squash_merge = true

  delete_branch_on_merge = false
}
