 terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }
  required_version = ">= 1.2.0"
}
resource "aws_key_pair" "preethi-kcs-key" {
  key_name   = "preethi-kcs-key"
  public_key = tls_private_key.rsa.public_key_openssh
}
resource "tls_private_key" "rsa" {
  algorithm = "RSA"
  rsa_bits  = 4096
}
resource "local_file" "preethi-kcs-key" {
  content  = tls_private_key.rsa.private_key_pem 
  filename = "tfkey.pem"
}

provider "aws" {
  region  = "us-east-1"
}

resource "aws_security_group" "TF-SG" {
  name        = "security_group_terraform"
  description = "security_group_terraform"
  vpc_id      = "vpc-019c09a1a0c5b4f6b"
  #HTTPS
  ingress {
    description      = "HTTPS"
    from_port        = 443
    to_port          = 443
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
  ingress {
    description      = "HTTP"
    from_port        = 80
    to_port          = 80
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
  ingress {
    description      = "SSH"
    from_port        = 22
    to_port          = 22
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = {
    Name = "TF-SG"
  }
}


resource "aws_subnet" "preethi_subnet" {
  vpc_id = "vpc-019c09a1a0c5b4f6b"

  cidr_block = "10.0.0.48/28"

  tags = {
    "Name" = "Preethi subnet"
  }

}
resource "aws_s3_bucket" "preethi-kcs-bucket" {
  bucket = "preethi-kcs-bucket"

}
resource "aws_s3_bucket_versioning" "enabled" {
  bucket = aws_s3_bucket.preethi-kcs-bucket.id
  versioning_configuration {
    status = "Enabled"
  }
}
resource "aws_s3_bucket_server_side_encryption_configuration" "public_encryption" {
  bucket = aws_s3_bucket.preethi-kcs-bucket.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}


resource "aws_s3_bucket_public_access_block" "public_access" {
  bucket                  = aws_s3_bucket.preethi-kcs-bucket.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}



terraform {
  backend "s3" {
    bucket         = "preethi-kcs-bucket"
    region  = "us-east-1"
    key     = "s3/terraform.tfstate"
    encrypt        = true
  }
}



resource "aws_instance" "app_server" {
  ami           = "ami-00c39f71452c08778"
  instance_type = "t2.micro"
  subnet_id = aws_subnet.preethi_subnet.id
  key_name= "preethi-kcs-key"
  vpc_security_group_ids = [aws_security_group.TF-SG.id]

  associate_public_ip_address = true

  tags = {
    Name = "Preethi-EC2"
  }

  }