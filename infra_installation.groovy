pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                
                git url: 'https://github.com/shubham1507/SRE.git', branch: 'master'
            }
        }
        
        stage('Install Ansible') {
            steps {
               
                sh 'sudo apt-get update'
                sh 'sudo apt-get install -y ansible'
            }
        }
        
        stage('Create Inventory File') {
            steps {
              
                writeFile file: 'inventory.ini', text: '''
[jenkins_target]
10.0.1.52 ansible_ssh_user=ubuntu

[nexus]
10.0.1.188 ansible_ssh_user=ubuntu

[kubernetes]
10.0.1.35 ansible_ssh_user=ubuntu
'''
            }
        }
        
        stage('Run Ansible Playbook') {
            steps {
                // Run the Ansible playbook
                sh 'ansible-playbook -i inventory.ini install_tools.yml'
            }
        }
    }
    
    post {
        always {
            // Clean up workspace after build
            cleanWs()
        }
    }
}
