resource "stripe_product" "insight" {
  name = "Insight"
  type = "service"
}

resource "stripe_price" "business" {
  product     = stripe_product.insight.id
  unit_amount = 200
  currency    = "eur"
  recurring = {
    interval = "month"
  }
}

resource "stripe_webhook_endpoint" "event" {
  url = "${var.billing_api_base_url}/v1/billing/subscriptions/event"

  enabled_events = [
    "invoice.paid"
  ]
}
