pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/shubham1507/SRE.git'
        ANSIBLE_PLAYBOOK = 'install_tools.yml'
        INVENTORY_FILE = 'inventory.ini'
        SSH_KEY_PATH = '/var/lib/jenkins/.ssh/id_rsa'
        SSH_PUBLIC_KEY_PATH = '/var/lib/jenkins/.ssh/id_rsa.pub'
        USER = 'jenkins'  // Change to your username if different
    }

    stages {
        stage('Generate SSH Key Pair') {
            steps {
                script {
                    // Generate SSH key pair if it does not exist
                    sh """
                    if [ ! -f ${env.SSH_KEY_PATH} ]; then
                        ssh-keygen -t rsa -b 4096 -C "${env.USER}@localhost" -f ${env.SSH_KEY_PATH} -N ""
                    fi
                    """
                }
            }
        }

        stage('Add Public Key to Remote Servers') {
            steps {
                script {
                    // Read the public key
                    def publicKey = sh(script: "cat ${env.SSH_PUBLIC_KEY_PATH}", returnStdout: true).trim()

                    // Commands to add public key to remote servers
                    def commands = [
                        "sshpass -p 'password' ssh ${env.USER}@10.0.1.133 'mkdir -p ~/.ssh && echo \"${publicKey}\" >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys'",
                        "sshpass -p 'password' ssh ${env.USER}@10.0.1.219 'mkdir -p ~/.ssh && echo \"${publicKey}\" >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys'",
                        "sshpass -p 'password' ssh ${env.USER}@10.0.1.234 'mkdir -p ~/.ssh && echo \"${publicKey}\" >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys'"
                    ]

                    // Execute commands
                    commands.each { command ->
                        sh command
                    }
                }
            }
        }

        stage('Checkout Ansible Playbook') {
            steps {
                git url: "${env.GIT_REPO}"
            }
        }

        stage('Install Tools') {
            steps {
                script {
                    // Run Ansible Playbook
                    sh """
                    ansible-playbook -i ${env.INVENTORY_FILE} ${env.ANSIBLE_PLAYBOOK} --private-key=${env.SSH_KEY_PATH}
                    """
                }
            }
        }

        stage('Update Firewall Rules') {
            steps {
                script {
                    // Define commands to update firewall rules
                    def commands = [
                        "ssh -i ${env.SSH_KEY_PATH} ${env.USER}@10.0.1.133 'sudo ufw allow 8080/tcp'",
                        "ssh -i ${env.SSH_KEY_PATH} ${env.USER}@10.0.1.219 'sudo ufw allow 8081/tcp'",
                        "ssh -i ${env.SSH_KEY_PATH} ${env.USER}@10.0.1.234 'sudo ufw allow 8082/tcp'"
                    ]

                    commands.each { command ->
                        sh command
                    }
                }
            }
        }
    }

    post {
        always {
            // Archive Ansible logs if available
            archiveArtifacts artifacts: '**/ansible.log', allowEmptyArchive: true
            // Clean up workspace
            cleanWs()
        }
    }
}
