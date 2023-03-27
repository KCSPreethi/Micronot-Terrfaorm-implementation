terraform {
  backend "s3" {
    bucket  = "preethi-tf-test-bucket"
    key     = "s3/terraform.tfstate"
    region  = "us-east-1"
    encrypt = true
  }
}