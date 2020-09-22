resource "stripe_product" "insight" {
  name = "Insight"
  type = "service"
}

resource "stripe_plan" "business" {
  product  = stripe_product.my_product.id
  amount   = 199.99
  interval = "month"
  currency = "eur"
}

resource "stripe_webhook_endpoint" "event" {
  url = "${var.billing_api_base_url}/v1/billing/subscriptions/event"

  enabled_events = [
    "invoice.paid"
  ]
}
