resource "stripe_product" "insight" {
  name = "Insight ${var.environment}"
  type = "service"
}
