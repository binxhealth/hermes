def application = "hermes"
def project = "atlas-dev-2001"
def registry = "us.gcr.io"

def label = "jkn-${application}-${UUID.randomUUID().toString()}"
podTemplate(label: label, cloud: 'kubernetes', containers: [
    containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:3.14-1', 
        args: '${computer.jnlpmac} ${computer.name}'),
    containerTemplate(name: 'postgres', image: 'postgres:10.4', 
        envVars: [
            envVar(key: 'POSTGRES_DB', value: 'atlas'),
            envVar(key: 'POSTGRES_USER', value: 'atlas'),
            envVar(key: 'POSTGRES_PASSWORD', value: 'kittens')]),
    containerTemplate(name: 'util', image: 'gcr.io/atlas-dev-2001/jkn-util:1.0',
        ttyEnabled: true,
        privileged: true,
        alwaysPullImage: false,
        workingDir: '/home/jenkins',
        resourceRequestCpu: '400m',
        resourceRequestMemory: '512Mi',
        resourceLimitCpu: '2',
        resourceLimitMemory: '2048Mi',
        command: 'cat',
    ),
    containerTemplate(name: 'groovy', image: 'gcr.io/atlas-dev-2001/jkn-root-groovy:2.4-jdk8',
        ttyEnabled: true,
        privileged: true,
        alwaysPullImage: false,
        workingDir: '/home/jenkins',
        resourceRequestCpu: '400m',
        resourceRequestMemory: '512Mi',
        resourceLimitCpu: '2',
        resourceLimitMemory: '2048Mi',
        command: 'cat',
    )
],
volumes: [
    secretVolume(mountPath: '/root/.config/', secretName: 'gcr-service-account'),
    hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
]) {

  node(label) {
    stage('Checkout Source') { checkout scm }
    stage('Build Plugin') {
      container('groovy') {
        sh 'groovy -v'
        sh 'cd hermes-plugin'
        sh './grailsw install'
        sh 'cd ..'
      } 
    }
    stage('Integration Tests') {
      container('groovy') {
        sh 'cd hermes-integration-test-app'
        sh './gradlew test'
      } 
    }
  }
}