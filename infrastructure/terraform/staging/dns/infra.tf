resource "aws_route53_record" "metabase" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "metabase.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "metabase_www" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "www.metabase.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "auth" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "auth.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "dashboard" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "dashboard.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "dashboard_www" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "www.dashboard.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "prometheus" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "prometheus.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "prometheus_www" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "www.prometheus.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "grafana" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "grafana.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "grafana_www" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "www.grafana.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "tracing" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "tracing.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "tracing_www" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "www.tracing.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "atlantis" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "atlantis.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "kibana" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "kibana.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "argo" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "argo.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "alertmanager" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "alertmanager.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}

resource "aws_route53_record" "alertmanager_www" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "www.alertmanager.${module.project_vars.domain}"
  type    = "A"
  ttl     = "300"
  records = ["213.161.29.246"] # Point to Kubernetes cluster
}
