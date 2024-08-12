pipeline {
    agent any

    environment {
        K8S_CONFIG_DIR = 'k8s-configs'
        REPO_URL = 'https://github.com/shubham1507/SRE.git'
        KUBECONFIG = '/var/snap/microk8s/current/credentials/client.config' // Add the kubeconfig path
    }

    stages {
        stage('Checkout Repository') {
            steps {
                git url: "${REPO_URL}"
            }
        }

        stage('Install MicroK8s') {
            steps {
                script {
                    def microk8sInstalled = sh(script: 'which microk8s', returnStatus: true) == 0

                    if (!microk8sInstalled) {
                        echo 'MicroK8s not found. Installing MicroK8s...'
                        try {
                            sh '''
                            sudo snap install microk8s --classic
                            sudo microk8s status --wait-ready
                            sudo microk8s enable dns storage
                            '''
                        } catch (Exception e) {
                            error "MicroK8s installation failed: ${e}"
                        }
                    } else {
                        echo 'MicroK8s is already installed.'
                    }
                }
            }
        }

        stage('Install Helm') {
            steps {
                script {
                    def helmInstalled = sh(script: 'which helm', returnStatus: true) == 0

                    if (!helmInstalled) {
                        echo 'Helm not found. Installing Helm...'
                        try {
                            sh '''
                            curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
                            '''
                        } catch (Exception e) {
                            error "Helm installation failed: ${e}"
                        }
                    } else {
                        echo 'Helm is already installed.'
                    }
                }
            }
        }

        stage('Setup Kubernetes Configuration') {
            steps {
                script {
                    try {
                        sh '''
                        sudo snap alias microk8s.kubectl kubectl
                        kubectl config set-context --current --namespace=default
                        kubectl config use-context microk8s
                        kubectl cluster-info
                        '''
                    } catch (Exception e) {
                        error "Kubernetes configuration setup failed: ${e}"
                    }
                }
            }
        }

        stage('Verify Kubernetes Access') {
            steps {
                script {
                    try {
                        sh '''
                        kubectl cluster-info
                        kubectl get nodes
                        '''
                    } catch (Exception e) {
                        error "Failed to connect to Kubernetes cluster: ${e}"
                    }
                }
            }
        }

        stage('Test Helm Access') {
            steps {
                script {
                    try {
                        sh '''
                        helm repo update
                        helm search repo
                        '''
                    } catch (Exception e) {
                        error "Helm is not able to access the Kubernetes cluster: ${e}"
                    }
                }
            }
        }

        stage('Package Helm Charts') {
            steps {
                script {
                    try {
                        sh '''
                        helm package prometheus-chart --version 1.0.0
                        helm package grafana-chart --version 1.0.0
                        '''
                        sh 'ls -l *.tgz'
                    } catch (Exception e) {
                        error "Failed to package Helm charts: ${e}"
                    }
                }
            }
        }

        stage('Verify Helm Package') {
            steps {
                script {
                    sh '''
                    if [ ! -f prometheus-1.0.0.tgz ]; then
                        echo "Prometheus chart package not found!"
                        exit 1
                    fi

                    if [ ! -f grafana-1.0.0.tgz ]; then
                        echo "Grafana chart package not found!"
                        exit 1
                    fi
                    '''
                }
            }
        }

        stage('Deploy Prometheus and Grafana') {
            steps {
                script {
                    sh 'ls -l' // Debugging step to list files before deployment
                    try {
                        sh '''
                        helm install my-prometheus ./prometheus-1.0.0.tgz -f ${K8S_CONFIG_DIR}/prometheus-config.yaml
                        helm install my-grafana ./grafana-1.0.0.tgz -f ${K8S_CONFIG_DIR}/grafana-config.yaml
                        '''
                    } catch (Exception e) {
                        error "Deployment of Prometheus and Grafana failed: ${e}"
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Cleaning up...'
        }
    }
}
