node {
  try {
    stage('Clone') {
        checkout scm
    }

    withMaven(maven: 'M3') {
        stage('Build') {
            sh "mvn -DskipTest clean compile"
        }

        // NOTE: it seems that the multimodule structure causes a build error when invoking 
        // 'test' without 'package', because the interdependencies between the modules are 
        // not yet deployed to the maven repository.
        stage('Package and Test') {
            sh "mvn package test"
        }
    
        stage('Deploy') {
            sh "mvn -DskipTests deploy"
        }
    }
        
  	slackSend (color: '#5bc0de', message: "New unstable update available: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>")
  } catch (e) {
    slackSend (color: '#d9534f', message: "FAILED: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>")
    throw e
  }
}   
