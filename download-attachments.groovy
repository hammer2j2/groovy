import groovyx.net.http.HTTPBuilder
import groovyx.net.http.RESTClient;
import groovyx.net.http.HttpResponseDecorator;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.HttpRequestInterceptor;
import org.apache.commons.beanutils.DynaBean
import groovy.json.JsonSlurper;
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

import net.sf.json.JSONArray
import net.sf.json.JSONObject
import net.sf.ezmorph.Morpher



def matchExp = args[0]
def user = args[1]
def pw = args[2]
def server = args[3]
 

def project = "BDO" // jira project
def basicMatch = "redirect" // search for redirect tickets based on summary line
def testNameMatch = "test"  // files with test in the filename are test files

def jiraApiUrl = "${server}" + '/rest/api/2/'
def jiraUserUrl = "${server}" + '/browse/'
def jiraDlUrl = "${server}" + '/secure/attachment/'


def http = new HTTPBuilder(jiraApiUrl);
def httpDl = new HTTPBuilder(jiraDlUrl);


http.client.addRequestInterceptor(new HttpRequestInterceptor() {
void process(HttpRequest httpRequest, HttpContext httpContext) {
httpRequest.addHeader('Authorization', 'Basic ' + "${user}:${pw}".bytes.encodeBase64().toString()) } })

httpDl.client.addRequestInterceptor(new HttpRequestInterceptor() {
void process(HttpRequest httpRequest, HttpContext httpContext) {
httpRequest.addHeader('Authorization', 'Basic ' + "${user}:${pw}".bytes.encodeBase64().toString()) } })


// http.setProxy('connsvr.foo.com', 8080, null) 
 
def jiraJson = ""
def jiraDlJson = ""

http.get( path: 'search',
        query: [jql : "project=${project} AND summary ~ ${basicMatch}","maxResults":10000,fields:['attachment','summary']],
          contentType : 'application/json',
           ) 
    { resp ->
        println "successful Jira query - ${resp.statusLine}"
        def inputStream = new InputStreamReader(resp.getEntity().getContent())
        def slurper = new JsonSlurper()
        jiraJson = slurper.parseText(inputStream.getText())
        // println "self - ${jiraJson.self}"

        def total = jiraJson.total
        def fields =  jiraJson.issues.fields;

        println "total issues returned = " + total
        /* println "fields  = " + fields 
        println "fields[2]  = " + fields[2]
        println "fields[2].summary  = " + fields[2].summary
        println " JSON:"
        // okay don't really print it
        // println jiraJson
        */

    }

// look for fields.attachment.filename matching something
// look for fields.attachment.mimeType": "text/plain",
// download fields.attachment.content
 
jiraJson.issues.each {
    if ( it.fields.summary =~ /(?i)${matchExp}/ ) {
        println 'Looking for attachments to ' +it.key +' '+jiraUserUrl+'/'+it.key+' "'+ it.fields.summary +'"'
        it.fields.attachment.each {
            println 'Checking '+ it.filename
            if ( it.filename =~ /(?i)${testNameMatch}/ && it.mimeType =~ /text.plain/ ) {
                println it.filename + ' appears to be a test file. Going to download it.'
                def attpath =  it.id + '/' + it.filename
                println "Going to get ${jiraDlUrl}${attpath}"
                httpDl.get( path:  "${attpath}" ) { resp ->
                    def inputStream = new InputStreamReader(resp.getEntity().getContent())
                    println "Text = "
                    println inputStream.getText()
                }
            }
        }
        
    } 
}



  def serverInfo = http.get(path: 'serverInfo')

  println "Using JIRA version " + serverInfo.version
