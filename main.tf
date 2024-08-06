provider "aws" {
  region = "ap-south-1"
}

# Use existing VPC
data "aws_vpc" "tool_installer_vpc" {
  filter {
    name   = "vpc-id"
    values = ["vpc-01cf80d6a27c5bb2d"]
  }
}

# Subnet
resource "aws_subnet" "tool_installer_subnet" {
  vpc_id                  = "vpc-01cf80d6a27c5bb2d"
  cidr_block              = "10.0.1.0/24"
  map_public_ip_on_launch = true

  tags = {
    Name = "tool_installer_subnet"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "tool_installer_igw" {
  vpc_id = "vpc-01cf80d6a27c5bb2d"

  tags = {
    Name = "tool_installer_igw"
  }
}

# Route Table
resource "aws_route_table" "tool_installer_route_table" {
  vpc_id = "vpc-01cf80d6a27c5bb2d"

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

# Use existing Security Group
data "aws_security_group" "existing_sg" {
  filter {
    name   = "group-name"
    values = ["launch-wizard-16"]
  }
}

# Jenkins Target EC2 Instance
resource "aws_instance" "jenkins_target" {
  ami                         = "ami-0ad21ae1d0696ad58"
  instance_type               = "t3.medium"
  subnet_id                   = aws_subnet.tool_installer_subnet.id
  key_name                    = "NexaJenkins"
  vpc_security_group_ids      = [data.aws_security_group.existing_sg.id]
  associate_public_ip_address = true

  tags = {
    Name = "Jenkins-target"
  }
}

# Nexus EC2 Instance
resource "aws_instance" "nexus" {
  ami                         = "ami-0ad21ae1d0696ad58"
  instance_type               = "t3.medium"
  subnet_id                   = aws_subnet.tool_installer_subnet.id
  key_name                    = "NexaJenkins"
  vpc_security_group_ids      = [data.aws_security_group.existing_sg.id]
  associate_public_ip_address = true

  tags = {
    Name = "Nexus"
  }
}

# Kubernetes EC2 Instance
resource "aws_instance" "kubernetes" {
  ami                         = "ami-0ad21ae1d0696ad58"
  instance_type               = "t3.medium"
  subnet_id                   = aws_subnet.tool_installer_subnet.id
  key_name                    = "NexaJenkins"
  vpc_security_group_ids      = [data.aws_security_group.existing_sg.id]
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
