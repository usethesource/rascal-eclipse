node {
  def mvnHome = tool 'M3'
  env.JAVA_HOME="${tool 'jdk-oracle-8'}"
  env.PATH="${env.JAVA_HOME}/bin:${mvnHome}/bin:${env.PATH}"

  try {
    stage 'Clone'
    checkout scm

    stage 'Build'
    sh "mvn -s ${env.HOME}/usethesource-maven-settings.xml -DskipTest -B clean compile"

  	// NOTE: it seems that the multimodule structure causes a build error when invoking 
    // 'test' without 'package', because the interdependencies between the modules are 
    // not yet deployed to the maven repository.
    stage 'Package and Test'
    sh "mvn -s ${env.HOME}/usethesource-maven-settings.xml -B package test"
    
    stage 'Deploy'
    sh "mvn -s ${env.HOME}/usethesource-maven-settings.xml -DskipTests -B deploy"
        
  	slackSend (color: '#5bc0de', message: "New unstable update available: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>")
  } catch (e) {
    slackSend (color: '#d9534f', message: "FAILED: <${env.BUILD_URL}|${env.JOB_NAME} [${env.BUILD_NUMBER}]>")
    throw e
  }
}   
