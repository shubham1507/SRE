pipeline {
    agent any
    
    environment {
        HELM_HOME = "${HOME}/.helm" // adjust this if Helm is in a different location
        MICROK8S_CHANNEL = '1.26/stable' // Adjust to the desired MicroK8s version
    }
    
    stages {
        stage('Install MicroK8s') {
            steps {
                script {
                    // Install MicroK8s
                    sh '''
                        sudo snap install microk8s --classic --channel=${MICROK8S_CHANNEL}
                        sudo microk8s status --wait-ready
                        sudo microk8s kubectl get nodes
                    '''
                }
            }
        }
        
        stage('Install Helm') {
            steps {
                script {
                    // Install Helm
                    sh '''
                        curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
                        helm version
                    '''
                }
            }
        }
        
        stage('Configure Helm Repositories') {
            steps {
                script {
                    // Add Helm repositories
                    sh '''
                        helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                        helm repo add grafana https://grafana.github.io/helm-charts
                        helm repo update
                    '''
                }
            }
        }
        
        stage('Deploy Prometheus') {
            steps {
                script {
                    // Deploy Prometheus using Helm
                    sh '''
                        microk8s kubectl create namespace monitoring || true
                        helm upgrade --install prometheus prometheus-community/prometheus --namespace monitoring
                    '''
                }
            }
        }
        
        stage('Deploy Grafana') {
            steps {
                script {
                    // Deploy Grafana using Helm
                    sh '''
                        helm upgrade --install grafana grafana/grafana --namespace monitoring
                    '''
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                script {
                    // Verify the deployment
                    sh '''
                        microk8s kubectl get pods --namespace monitoring
                        microk8s kubectl get svc --namespace monitoring
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            sh '''
                helm repo remove prometheus-community
                helm repo remove grafana
                microk8s stop
            '''
        }
    }
}
