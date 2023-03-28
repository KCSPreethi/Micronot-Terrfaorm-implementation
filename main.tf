  terraform {
    required_providers {
    aws = {
    source  = "hashicorp/aws"
    version = "~> 4.16"
    }
    }
    required_version = ">= 1.2.0"
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
   terraform {
      backend "s3" {
        bucket         = "gurukul-preethi"
        region  = "us-east-1"
        key     = "s3/terraform.tfstate"
        encrypt        = true
      }
    }

resource "aws_subnet" "preethi_subnet" {
  vpc_id = "vpc-019c09a1a0c5b4f6b"

  cidr_block = "10.0.0.128/28"

  tags = {
    "Name" = "Preethi subnet"
  }

}