pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/yourusername/your-repo.git'
        ANSIBLE_PLAYBOOK = 'install_tools.yml'
        INVENTORY_FILE = 'inventory.ini'
        SSH_KEY_PATH = '/var/lib/jenkins/.ssh/id_rsa'
        SSH_PUBLIC_KEY_PATH = '/var/lib/jenkins/.ssh/id_rsa.pub'
        USER = 'jenkins'
    }

    stages {
        stage('Generate SSH Key Pair') {
            steps {
                script {
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
                    def publicKey = sh(script: "cat ${env.SSH_PUBLIC_KEY_PATH}", returnStdout: true).trim()

                    def commands = [
                        "ssh -i ${env.SSH_KEY_PATH} ${env.USER}@10.0.1.217 'mkdir -p ~/.ssh && echo \"${publicKey}\" >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys'",
                        "ssh -i ${env.SSH_KEY_PATH} ${env.USER}@10.0.1.11 'mkdir -p ~/.ssh && echo \"${publicKey}\" >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys'",
                        "ssh -i ${env.SSH_KEY_PATH} ${env.USER}@3.110.136.0 'mkdir -p ~/.ssh && echo \"${publicKey}\" >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys'"
                    ]

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
                    sh """
                    ansible-playbook -i ${env.INVENTORY_FILE} ${env.ANSIBLE_PLAYBOOK} --private-key=${env.SSH_KEY_PATH}
                    """
                }
            }
        }

        stage('Update Firewall Rules') {
            steps {
                script {
                    def commands = [
                        "ssh -i ${env.SSH_KEY_PATH} ${env.USER}@10.0.1.217 'sudo ufw allow 8080/tcp'",
                        "ssh -i ${env.SSH_KEY_PATH} ${env.USER}@10.0.1.11 'sudo ufw allow 8081/tcp'",
                        "ssh -i ${env.SSH_KEY_PATH} ${env.USER}@3.110.136.0 'sudo ufw allow 8082/tcp'"
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
            archiveArtifacts artifacts: '**/ansible.log', allowEmptyArchive: true
            cleanWs()
        }
    }
}
