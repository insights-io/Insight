resource "stripe_product" "insight" {
  name = "Insight"
  type = "service"
}

resource "stripe_plan" "business" {
  product  = stripe_product.insight.id
  amount   = 200
  interval = "month"
  currency = "eur"
}

resource "stripe_webhook_endpoint" "event" {
  url = "${var.billing_api_base_url}/v1/billing/subscriptions/event"

  enabled_events = [
    "invoice.paid"
  ]
}
