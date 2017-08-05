import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.CredentialsScope

String credentials_id = 'jenkins-docker-cloud-credentials'
String jenkins_agent_user = 'jenkins'
String jenkins_agent_pass = 'jenkins'

boolean modified_creds = false
Domain domain
SystemCredentialsProvider system_creds = SystemCredentialsProvider.getInstance()
system_creds.metaClass.methods*.name.sort().unique()
Map system_creds_map = system_creds.getDomainCredentialsMap()
(system_creds_map.keySet() as List).each {
    //name of the global domain is null
    if(it.getName() == null) {
        domain = it
    }
}
if(!system_creds_map[domain] || (system_creds_map[domain].findAll {credentials_id.equals(it.id)}).size() < 1) {
    UsernamePasswordCredentialsImpl jenkins_docker_creds = new UsernamePasswordCredentialsImpl(
            CredentialsScope.GLOBAL,
            credentials_id,
            "${credentials_id} credentials",
            jenkins_agent_user,
            jenkins_agent_pass)
    if(system_creds_map[domain] && system_creds_map[domain].size() > 0) {
        //other credentials exist so should only append
        system_creds_map[domain] << jenkins_docker_creds
    }
    else {
        system_creds_map[domain] = [jenkins_docker_creds]
    }
    modified_creds = true
}
//save any modified credentials
if(modified_creds) {
    println 'Credentials for Docker agent configured.'
    system_creds.setDomainCredentialsMap(system_creds_map)
    system_creds.save()
}
else {
    println 'Nothing changed.  Docker agent credentials already configured.'
}
