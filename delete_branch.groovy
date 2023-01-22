import jenkins.*
import jenkins.model.* 
import hudson.*
import hudson.model.*


properties([
  parameters([
    string(description: 'Please enter application name', name: 'appName', trim: true),
    choice(choices: ['feature branch', 'release tags', 'all feature Branches'], description: 'Do you want to delete feature branch or release tags ? select any one Type ', name: 'Type'), 
    [
      $class: 'CascadeChoiceParameter', 
      choiceType: 'PT_MULTI_SELECT', 
      filterLength: 1, 
      filterable: true, 
      name: 'Head', 
      randomName: 'choice-parameter-3449262279747', 
      referencedParameters: 'appName,Type', 
      script: [$class: 'GroovyScript', 
      fallbackScript: [classpath: [], 
      oldScript: '', 
      sandbox: true, 
      script: 'return  [\'Error\']'], 
      script: [classpath: [], 
      oldScript: '', 
      sandbox: false, 
      script: '''
import jenkins.*
import jenkins.model.* 
import hudson.*
import hudson.model.*
def jenkinsCredentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
        com.cloudbees.plugins.credentials.Credentials.class,
        Jenkins.instance,
        null,
        null
);

def git_username
def git_password
//Read credential from Jenkins configuration
for (creds in jenkinsCredentials) {
    if (creds.id == \'GitHubApp\') {
        git_username = creds.username
        git_password = creds.password
    }
}

def results = []
if (Type == \'feature branch\') {
    def url = "https://${git_username}:${git_password}@github.com/ram-repo/${appName}.git"
    def gettags = ("git ls-remote -h ${url} ").execute()
   branch_values = gettags.text.readLines().collect {
        it.split()[1].replaceAll(\'refs/heads/\', \'\').replaceAll(\'refs/tags/\', \'\').replaceAll("\\\\^\\\\{\\\\}", \'\')
    }
    def branch_bugfix = []
    def branch_hotfix = []
    def branch_fea =[]
    branch_feature = branch_values.findAll { it.startsWith("fe") }
    branch_bugfix = branch_values.findAll { it.startsWith("bu") }
    branch_hotfix = branch_values.findAll { it.startsWith("ho") }
    results = branch_feature+branch_bugfix+branch_hotfix
    return results
} else if (Type == \'release tags\') {
    def url = "https://${git_username}:${git_password}@github.com/ram-repo/${appName}.git"
    def gettags = ("git ls-remote -t ${url} ").execute()
    results = gettags.text.readLines().collect {
        it.split()[1].replaceAll(\'refs/heads/\', \'\').replaceAll(\'refs/tags/\', \'\').replaceAll("\\\\^\\\\{\\\\}", \'\')
    }
    return results
}else {
    results = [\'all-branches\']
    return  results
}
return  results
''']]]])])

pipeline {
    agent any
    stages {
    stage('Deleting selected Branches or release tags') {
      steps {
          script {
            withCredentials([usernamePassword(credentialsId: 'GitHubApp', 
                        passwordVariable: 'Password', usernameVariable: 'UserName')]) {
                          // 
                dir ("${params['appName']}-$env.BUILD_ID") {
                   if ( params['Type'] == 'feature branch' ) {
                       
                        echo "selected feature branch is: [${Head}]"
                        selected_branch = params.Head.split(",")
                        for (int i = 0; i < selected_branch.size(); i++) {
                            def sb = selected_branch[i]
                            echo "${sb}"
                      sh """
                    curl -s -X DELETE -u ${UserName}:${Password} https://api.github.com/repos/ram-repo/${params['appName']}/git/refs/heads/${sb}
                    """
                    }} else if ( params['Type'] == 'release tags' ){
                      echo "selected feature branch is: [${Head}]"
                      selected_branch = params.Head.split(",")
                        for (int i = 0; i < selected_branch.size(); i++) {
                            def sb = selected_branch[i]
                            sh """
                            curl -s -X DELETE -u ${UserName}:${Password} https://api.github.com/repos/ram-repo/${params['appName']}/git/refs/tags/${sb}                    
                            """
                        }
                       echo "Successfully deleted selected release tags"
                    } else if ( params['Type'] == 'all feature Branches' ){
                        def list = []
                       list = getbranches("${params['appName']}")
                       echo list
                       
                       selected_branch = list.split(",")
                        for (int i = 0; i < selected_branch.size(); i++) {
                            def sb = selected_branch[i]
                           
                            sh """
                            curl -s -X DELETE -u ${UserName}:${Password} https://api.github.com/repos/ram-repo/${params['appName']}/git/refs/tags/${sb}                    
                            """
                        }
                        
                    } else {
                      echo "Selected Type is not found"
                    }
                }
              }
            }
          }
        }
  }
}

def getbranches(appName){
def jenkinsCredentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
        com.cloudbees.plugins.credentials.Credentials.class,
        Jenkins.instance,
        null,
        null
);

def git_username
def git_password
//Read credential from Jenkins configuration
for (creds in jenkinsCredentials) {
    if (creds.id == 'GitHubApp') {
        git_username = creds.username
        git_password = creds.password
    }
}

def results = []
    def url = "https://${git_username}:${git_password}@github.com/ram-repo/${appName}.git"
    def gettags = ("git ls-remote -h ${url} ").execute()
   branch_values = gettags.text.readLines().collect {
                            it.split()[1].replaceAll('refs/heads/', '').replaceAll('refs/tags/', '').replaceAll("\\^\\{\\}", '')
                        }
    def branch_bugfix = []
    def branch_hotfix = []
    def branch_fea =[]
    branch_feature = branch_values.findAll { it.startsWith("fe") }
    branch_bugfix = branch_values.findAll { it.startsWith("bu") }
    branch_hotfix = branch_values.findAll { it.startsWith("ho") }
    results = branch_feature+branch_bugfix+branch_hotfix
   String stringIds=results.join(",") 
   def output = []
   output = stringIds
    return output
}
