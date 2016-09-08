node {
  def mvnHome = tool 'M3'
  env.JAVA_HOME="${tool 'jdk-oracle-8'}"
  env.PATH="${env.JAVA_HOME}/bin:${mvnHome}/bin:${env.PATH}"

  stage 'Clone'
  checkout scm

  stage 'Build'
  sh "mvn -s ${env.HOME}/usethesource-maven-settings.xml -DskipTest -B clean compile"

  stage 'Test'
  sh "mvn -s ${env.HOME}/usethesource-maven-settings.xml -B test"

  stage 'Packaging'
  sh "mvn -s ${env.HOME}/usethesource-maven-settings.xml -DskipTest -B package"

  stage 'Deploy'
  sh "mvn -s ${env.HOME}/usethesource-maven-settings.xml -DskipTests -B deploy"

  // stage 'Publish to p2 Update Site [unstable]'
  // sshagent(['p2-update-site']) {
  //   sh "scp -r rascal-update-site/target/repository/* rascal@update.rascal-mpl.org:/home/update.rascal-mpl.org/unstable"
  // }

  stage 'Archive'
  step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
  // step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
}
