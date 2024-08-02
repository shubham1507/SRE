provider "aws" {
  region = "ap-south-1"  
}

resource "aws_instance" "jenkins_target" {
  ami           = "ami-0ad21ae1d0696ad58"  
  instance_type = "t3.medium"
  tags = {
    Name = "Jenkins-target"
  }
}

resource "aws_instance" "nexus" {
  ami           = "ami-0ad21ae1d0696ad58"  
  instance_type = "t3.medium"
  tags = {
    Name = "Nexus"
  }
}

resource "aws_instance" "kubernetes" {
  ami           = "ami-0ad21ae1d0696ad58" 
  instance_type = "t3.medium"
  tags = {
    Name = "Kubernetes"
  }
}

output "jenkins_target_public_ip" {
  value = aws_instance.jenkins_target.public_ip
}

output "nexus_public_ip" {
  value = aws_instance.nexus.public_ip
}

output "kubernetes_public_ip" {
  value = aws_instance.kubernetes.public_ip
}
