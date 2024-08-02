provider "aws" {
  region = "ap-south-1"  # Change to your desired region
}

# VPC
resource "aws_vpc" "main_vpc" {
  cidr_block = "10.0.0.0/16"

  tags = {
    Name = "main_vpc"
  }
}

# Subnet
resource "aws_subnet" "main_subnet" {
  vpc_id     = aws_vpc.main_vpc.id
  cidr_block = "10.0.1.0/24"

  tags = {
    Name = "main_subnet"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "main_igw" {
  vpc_id = aws_vpc.main_vpc.id

  tags = {
    Name = "main_igw"
  }
}

# Route Table
resource "aws_route_table" "main_route_table" {
  vpc_id = aws_vpc.main_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main_igw.id
  }

  tags = {
    Name = "main_route_table"
  }
}

# Associate Route Table with Subnet
resource "aws_route_table_association" "main_route_table_association" {
  subnet_id      = aws_subnet.main_subnet.id
  route_table_id = aws_route_table.main_route_table.id
}

# Security Group
resource "aws_security_group" "main_sg" {
  vpc_id = aws_vpc.main_vpc.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "main_sg"
  }
}

# Jenkins Target EC2 Instance
resource "aws_instance" "jenkins_target" {
  ami           = "ami-0ad21ae1d0696ad58"  # Replace with a valid AMI ID
  instance_type = "t3.medium"
  subnet_id     = aws_subnet.main_subnet.id
  security_groups = [aws_security_group.main_sg.name]

  tags = {
    Name = "Jenkins-target"
  }
}

# Nexus EC2 Instance
resource "aws_instance" "nexus" {
  ami           = "ami-0ad21ae1d0696ad58"  # Replace with a valid AMI ID
  instance_type = "t3.medium"
  subnet_id     = aws_subnet.main_subnet.id
  security_groups = [aws_security_group.main_sg.name]

  tags = {
    Name = "Nexus"
  }
}

# Kubernetes EC2 Instance
resource "aws_instance" "kubernetes" {
  ami           = "ami-0ad21ae1d0696ad58"  # Replace with a valid AMI ID
  instance_type = "t3.medium"
  subnet_id     = aws_subnet.main_subnet.id
  #security_group_id = ["sg-0dc9dba3e16acb461"]
  security_groups = [aws_security_group.main_sg.name]

  tags = {
    Name = "Kubernetes"
  }
}

# Outputs
output "jenkins_target_public_ip" {
  value = aws_instance.jenkins_target.public_ip
}

output "nexus_public_ip" {
  value = aws_instance.nexus.public_ip
}

output "kubernetes_public_ip" {
  value = aws_instance.kubernetes.public_ip
}
