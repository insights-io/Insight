resource "aws_cloudfront_origin_access_identity" "s3_bucket_origin_access_identity" {
  comment = "Cloudfront S3 bucket origin access identity (Managed by Terraform)"
}

data "aws_iam_policy_document" "restrict_s3_to_cloudfront" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${var.s3_bucket_arn}/*"]

    principals {
      type        = "AWS"
      identifiers = [aws_cloudfront_origin_access_identity.s3_bucket_origin_access_identity.iam_arn]
    }
  }
}

resource "aws_s3_bucket_policy" "restrict_s3_to_cloudfront" {
  bucket = var.s3_bucket_id
  policy = data.aws_iam_policy_document.restrict_s3_to_cloudfront.json
}
