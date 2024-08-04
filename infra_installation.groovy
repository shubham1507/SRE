pipeline {
    agent any

    environment {
        GIT_REPO_URL = 'https://github.com/shubham1507/SRE.git'
        INVENTORY_FILE = 'inventory.ini'
        ANSIBLE_PLAYBOOK = 'install_tools.yml'
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Clone the Git repository containing the Ansible playbook
                git url: "${env.GIT_REPO_URL}"
            }
        }

        stage('Prepare Inventory') {
            steps {
                script {
                    // Write the inventory file dynamically
                    writeFile file: "${env.INVENTORY_FILE}", text: '''\
[jenkins_target]
10.0.1.120

[nexus]
10.0.1.178

[kubernetes]
10.0.1.156

[all:vars]
ansible_user=ubuntu
'''
                }
            }
        }

        stage('Run Ansible Playbook') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'ubuntu_ssh', keyFileVariable: 'SSH_KEY')]) {
                    sh "ansible-playbook -i ${env.INVENTORY_FILE} ${env.ANSIBLE_PLAYBOOK} --private-key=${SSH_KEY}"
                }
            }
        }
    }

    post {
        always {
            // Clean up
            deleteDir()
        }
    }
}
