pipeline {
    agent any

    environment {
        GIT_REPO_URL = 'https://github.com/shubham1507/SRE.git'
        INVENTORY_FILE_URL = 'https://github.com/shubham1507/SRE/raw/master/inventory.ini'
        ANSIBLE_PLAYBOOK = 'install_tools.yml'
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Clone the Git repository containing the Ansible playbook and inventory file
                git url: "${env.GIT_REPO_URL}"
            }
        }

        stage('Run Ansible Playbook') {
            steps {
                script {
                    // Download the inventory file from the specified URL
                    sh "wget ${env.INVENTORY_FILE_URL}"
                }

                // Run Ansible playbook
                withCredentials([sshUserPrivateKey(credentialsId: 'ubuntu_ssh', keyFileVariable: 'SSH_KEY')]) {
                    sh "ansible-playbook -i inventory.ini ${env.ANSIBLE_PLAYBOOK} --private-key=${SSH_KEY}"
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
