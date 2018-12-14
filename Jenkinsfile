@Library('gsl')
def utils = new com.binxhealth.Utils()

buildTemplate(
  timeoutMinutes: 20,
  // JNLP and Utility automatically included
  containers: [
    containerGroovy(),
    containerPostgres()
  ],
  envVars: [
    envVar(key: 'DB_USER', value: 'atlas'),
    envVar(key: 'DB_PASS', value: 'atlas'),
    envVar(key: 'DB_NAME', value: 'atlas'),
    envVar(key: 'POSTGRES_DB', value: 'atlas'),
    envVar(key: 'POSTGRES_USER', value: 'atlas'),
    envVar(key: 'POSTGRES_PASSWORD', value: 'atlas')
  ]
) {
  // If master and no artifact exists or PR then build required
  if (isBuildRequired()) {
    try {
      stage('Plugin Tests') {
        container('groovy') {
          sh 'cd hermes-plugin && ./grailsw test-app'
        } 
      }
    } finally {
      junit 'hermes-plugin/build/test-results/**/*.xml'
    }
    try {
      stage('Integration Tests from Test App') {
        container('groovy') {
          sh 'cd hermes-integration-test-app && ./grailsw test-app'
        } 
      }
    } finally {
      junit 'hermes-integration-test-app/build/test-results/**/*.xml'
    }
  }
}
