provider "aws" {
  region = "ap-south-1"
}

# VPC
resource "aws_vpc" "sre_vpc" {
  cidr_block = "10.0.0.0/16"

  tags = {
    Name = "sre_vpc"
  }
}

# Subnet
resource "aws_subnet" "sre_subnet" {
  vpc_id                  = aws_vpc.sre_vpc.id
  cidr_block              = "10.0.1.0/24"
  map_public_ip_on_launch = true  

  tags = {
    Name = "sre_subnet"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "sre_igw" {
  vpc_id = aws_vpc.sre_vpc.id

  tags = {
    Name = "sre_igw"
  }
}

# Route Table
resource "aws_route_table" "sre_route_table" {
  vpc_id = aws_vpc.sre_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.sre_igw.id
  }

  tags = {
    Name = "sre_route_table"
  }
}

# Associate Route Table with Subnet
resource "aws_route_table_association" "sre_route_table_association" {
  subnet_id      = aws_subnet.sre_subnet.id
  route_table_id = aws_route_table.sre_route_table.id
}

# Security Group
resource "aws_security_group" "sre_sg" {
  vpc_id = aws_vpc.sre_vpc.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = -1
    to_port     = -1
    protocol    = "icmp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "sre_sg"
  }
}

# Jenkins EC2 Instance
resource "aws_instance" "jenkins_target" {
  ami           = "ami-0ad21ae1d0696ad58"  # Replace with a valid AMI ID
  instance_type = "t3.medium"
  subnet_id     = aws_subnet.sre_subnet.id
  key_name       = "SRE"  # Key pair name
  vpc_security_group_ids = [aws_security_group.sre_sg.id]

  tags = {
    Name = "Jenkins"
  }
}

# Nexus EC2 Instance
resource "aws_instance" "nexus" {
  ami           = "ami-0ad21ae1d0696ad58"  # Replace with a valid AMI ID
  instance_type = "t3.medium"
  subnet_id     = aws_subnet.sre_subnet.id
  key_name       = "SRE"  # Key pair name
  vpc_security_group_ids = [aws_security_group.sre_sg.id]

  tags = {
    Name = "Nexus"
  }
}

# Kubernetes EC2 Instance
resource "aws_instance" "kubernetes" {
  ami           = "ami-0ad21ae1d0696ad58"  # Replace with a valid AMI ID
  instance_type = "t3.medium"
  subnet_id     = aws_subnet.sre_subnet.id
  key_name       = "SRE"  # Key pair name
  vpc_security_group_ids = [aws_security_group.sre_sg.id]

  tags = {
    Name = "Kubernetes"
  }
}

# Outputs
output "jenkins_target_public_ip" {
  value = aws_instance.jenkins_target.public_ip
}

output "jenkins_target_public_dns" {
  value = aws_instance.jenkins_target.public_dns
}

output "nexus_public_ip" {
  value = aws_instance.nexus.public_ip
}

output "nexus_public_dns" {
  value = aws_instance.nexus.public_dns
}

output "kubernetes_public_ip" {
  value = aws_instance.kubernetes.public_ip
}

output "kubernetes_public_dns" {
  value = aws_instance.kubernetes.public_dns
}
