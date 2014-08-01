import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient;
import groovyx.net.http.HttpResponseDecorator;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.HttpRequestInterceptor;
import groovy.json.JsonSlurper;
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

import net.sf.json.JSONArray

 
def issueId = args[0]
def user = args[1]
def pw = args[2]
def server = args[3]
def login = "${user}:${pw}"

def jiraApiUrl = "${server}" + '/rest/api/2/'
// /rest/api/2/'
// def jiraClient = new RESTClient(jiraApiUrl);
// def serverInfo = jiraClient.get(path: 'serverInfo')



def jira = new HTTPBuilder(jiraApiUrl);


jira.client.addRequestInterceptor(new HttpRequestInterceptor() {
void process(HttpRequest httpRequest, HttpContext httpContext) {
httpRequest.addHeader('Authorization', 'Basic ' + login.bytes.encodeBase64().toString())
}
})

 

  def serverInfo = jira.get(path: 'serverInfo')

 println "Using JIRA version " + serverInfo.version

 
//   def JSONArray issue  = jira.get(path: 'project');

  // def JSONArray projects = jira.get(path: 'project');

  // println projects

  

   // def int numberOfProjects = projects.size()

   // println "Got $numberOfProjects projects in JIRA";

   

    // projects.each { println it.dump() }
