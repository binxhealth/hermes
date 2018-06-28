def application = "hermes"
def project = "atlas-dev-2001"
def registry = "us.gcr.io"
def gcrSecret = "/root/gcr_credentials"
def label = "jkn-${application}-${UUID.randomUUID().toString()}"
def version = "${env.BUILD_NUMBER}" // Todo: Handle version number logic
def tag = "${registry}/${project}/${application}:${version}"

podTemplate(
    label: label,
    cloud: 'kubernetes',
    containers: [
        containerTemplate(
            name: 'jnlp',
            image: 'jenkins/jnlp-slave:3.14-1',
            args: '${computer.jnlpmac} ${computer.name}'
        ),
        containerTemplate(
            name: 'postgres',
            image: 'postgres:10.4',
            envVars: [
                envVar(key: 'POSTGRES_DB', value: 'atlas'),
                envVar(key: 'POSTGRES_USER', value: 'atlas'),
                envVar(key: 'POSTGRES_PASSWORD', value: 'atlas')]),
        containerTemplate(
            name: 'util',
            image: "${registry}/${project}/jkn-util:1.1",
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
        containerTemplate(name: 'groovy', 
            image: "${registry}/${project}/jkn-root-groovy:2.4-jdk8",
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
        secretVolume(
            mountPath: "$gcrSecret",
            secretName: 'gcr-service-account'
        ),
        hostPathVolume(
            mountPath: '/var/run/docker.sock',
            hostPath: '/var/run/docker.sock'
        )
    ]
) {
    node(label) {
        stage('Checkout Source') { checkout scm }
        try {
            stage('Build Plugin') {
                container('groovy') {
                    sh 'cd hermes-plugin && ./grailsw test-app --stacktrace'
                } 
            }
        } finally {
            junit 'hermes-plugin/build/test-results/**/*.xml'
        }
        try {
            stage('Integration Tests') {
                container('groovy') {
                    sh 'cd hermes-integration-test-app && ./grailsw test-app --stacktrace'
                } 
            }
        } finally {
            junit 'hermes-integration-test-app/build/test-results/**/*.xml'
        }
        // If Pull Request
        if (env.CHANGE_ID) {
            stage('Build Plugin') {
                container('util') {
                    sh "echo Todo"
                }
            }
            stage('Push Plugin') {
                container('util') {
                    sh "echo todo"
                }
            }
        }
    }
}