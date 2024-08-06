provider "aws" {
  region = "ap-south-1"
}

# VPC
resource "aws_vpc" "tool_installer_vpc" {
  cidr_block = "10.0.0.0/16"

  tags = {
    Name = "tool_installer_vpc"
  }
}

# Subnet
resource "aws_subnet" "tool_installer_subnet" {
  vpc_id                  = aws_vpc.tool_installer_vpc.id
  cidr_block              = "10.0.1.0/24"
  map_public_ip_on_launch = true

  tags = {
    Name = "tool_installer_subnet"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "tool_installer_igw" {
  vpc_id = aws_vpc.tool_installer_vpc.id

  tags = {
    Name = "tool_installer_igw"
  }
}

# Route Table
resource "aws_route_table" "tool_installer_route_table" {
  vpc_id = aws_vpc.tool_installer_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.tool_installer_igw.id
  }

  tags = {
    Name = "tool_installer_route_table"
  }
}

# Associate Route Table with Subnet
resource "aws_route_table_association" "tool_installer_route_table_association" {
  subnet_id      = aws_subnet.tool_installer_subnet.id
  route_table_id = aws_route_table.tool_installer_route_table.id
}

# Security Group
resource "aws_security_group" "tool_installer_sg" {
  vpc_id = aws_vpc.tool_installer_vpc.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # Replace with your IP range or trusted IP addresses
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

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8081
    to_port     = 8081
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
    Name = "tool_installer_sg"
  }
}

# Jenkins Target EC2 Instance
resource "aws_instance" "jenkins_target" {
  ami                         = "ami-0ad21ae1d0696ad58"
  instance_type               = "t3.medium"
  subnet_id                   = aws_subnet.tool_installer_subnet.id
  key_name                    = "NexaJenkins"
  vpc_security_group_ids      = [aws_security_group.tool_installer_sg.id]
  associate_public_ip_address = true

  tags = {
    Name = "Jenkins-runner"
  }
}

# Nexus EC2 Instance
resource "aws_instance" "nexus" {
  ami                         = "ami-0ad21ae1d0696ad58"
  instance_type               = "t3.medium"
  subnet_id                   = aws_subnet.tool_installer_subnet.id
  key_name                    = "NexaJenkins"
  vpc_security_group_ids      = [aws_security_group.tool_installer_sg.id]
  associate_public_ip_address = true

  tags = {
    Name = "Nexus-sonatype"
  }
}

# Kubernetes EC2 Instance
resource "aws_instance" "kubernetes" {
  ami                         = "ami-0ad21ae1d0696ad58"
  instance_type               = "t3.medium"
  subnet_id                   = aws_subnet.tool_installer_subnet.id
  key_name                    = "NexaJenkins"
  vpc_security_group_ids      = [aws_security_group.tool_installer_sg.id]
  associate_public_ip_address = true

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
