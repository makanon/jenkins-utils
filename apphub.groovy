// JDK 1.8.171 installed already an on path
// export PATH=/Users/mak/Dev/Groovy/groovy-2.4.7/bin:$PATH
// export ROOVY_HOME=/Users/mak/Dev/Groovy/groovy-2.4.7

// Each Jenkins page has a REST API hyperlink at the bottom, this is because each page has its own endpoint.

// http://localhost:8080/me
// configure
// Click 'Show API Token'
// 78e21f82a9e137614fef5b9593bcf827 = API Token

// HTTP Check 
// status=$(curl -u admin:admin --write-out "%{http_code}" --silent --output /dev/null "http://127.0.0.1:8080")
// echo "$status"
// --max-time 30 (30 s)

// Jenkins crumb with CURL
// CRUMB=$(curl -u admin:admin "http://127.0.0.1:8080/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,%22:%22,//crumb)")
// GROOVYSCRIPT=$("println hello world")
// touch /tmp/myscript.groovy
// echo "println(\"Hello World\")"> /tmp/myscript.groovy
// curl -u admin:admin -H "$CRUMB" -d "script=println(\"Hello World\")" http://127.0.0.1:8080/scriptText
// curl -u admin:admin -H "$CRUMB" -d "script=$(<./tmp/myscript.groovy)" http://127.0.0.1:8080/scriptText
// rm -rf /tmp/myscript.groovy

// Jenkins crumb with CURL with python reading xml
// mytoken=$(curl --user admin:admin -s http://127.0.0.1:8080/crumbIssuer/api/json | python -c 'import sys,json;j=json.load(sys.stdin);print j["crumbRequestField"] + ":" + j["crumb"]')



// curl -d "script=$(cat /tmp/script.groovy)" -v --user username:ApiToken http://localhost:8080/scriptText
//script=println new Date()
// --data-urlencode


// curl -s -u goll:78e21f82a9e137614fef5b9593bcf827 http://localhost:8080/crumbIssuer/api/json
// curl -s -u goll:78e21f82a9e137614fef5b9593bcf827 -H 'Jenkins-Crumb:0f23a062e0b9b9d13295e26bc8c8e206' http://localhost:8080/job/foo2/buildWithParameters -d 'directoryName=zaid222'

// curl http://127.0.0.1:8080/crumbIssuer/api/xml\?xpath\=concat\(//crumbRequestField,%22:%22,//crumb\) --user admin:admin
 
// curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8080 to get http_code back


// Quick Tip: If you have to send GET/POST requests over HTTPS protocol, then all you need is to use javax.net.ssl.HttpsURLConnection instead of java.net.HttpURLConnection. 
// Rest all the steps will be same as above, HttpsURLConnection will take care of SSL handshake and encryption.

// https://mvnrepository.com/artifact/com.cloudbees/groovy-cps
@Grapes(
    @Grab(group='com.cloudbees', module='groovy-cps', version='1.9')
)
import com.cloudbees.groovy.cps.NonCPS
import java.util.Base64;
import javax.xml.bind.DatatypeConverter;
// import org.apache.commons.codec.binary.Base64;
println "Print this to run as script"


class appHub {
    String body="";    
    String message="";    
    String cookie="";   
    String url="";   
    String userid="";
    String pw=""; 
    Integer statusCode;    
    boolean failure = false;
    
	@NonCPS
	def _getURL(text) {
		def matcher = text =~ '<serverURL>(.+)</serverURL>'
			matcher ? matcher[0][1] : null
	}
			
	def getURL(env) {
		def config = "${env.JENKINS_HOME}/org.jenkinsci.plugins.deployhub.DeployHub.xml";
				return _getURL(new File(config).text);
	}
				    
// ************************************************************************************************
// parseResponse
//      With connection object, extract and get response
// ************************************************************************************************
    def parseResponse(HttpURLConnection connection){    
        this.statusCode = connection.responseCode;    
        this.message = connection.responseMessage;    
        this.failure = false;
        
        if(statusCode == 200 || statusCode == 201){    
            this.body = connection.content.text;//this would fail the pipeline if there was a 400    
            println "status 200 or 201"
        }else{    
            this.failure = true;    
            this.body = connection.getErrorStream().text;
            println "failure response code"
        }
        /*        Map<String, List<String>> map = connection.getHeaderFields();
        
        if (cookie.length() == 0)
        { 
         for (Map.Entry<String, List<String>> entry : map.entrySet()) 
         {
          if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("Set-Cookie")) 
          {                  
            String c = entry.getValue();
            if  (c.contains("p1=") || c.contains("p2="))
            {
              cookie = c;
            }  
          }
         }     
        } */
    }   
    
// ************************************************************************************************
// doGetHttpRequest with requestUrl
//      With connection object, extract and get response
// ************************************************************************************************
    def doGetHttpRequest(String requestUrl){    
        URL url = new URL(requestUrl);    
        HttpURLConnection connection = url.openConnection();    
       
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Cookie", cookie); 
        connection.doOutput = true;   

        //get the request    
        connection.connect();    

        //parse the response    
        parseResponse(connection);    

        if(failure){    
            error("\nGET from URL: $requestUrl\n  HTTP Status: $resp.statusCode\n  Message: $resp.message\n  Response Body: $resp.body");    
        }    

        this.printDebug("Request (GET):\n  URL: $requestUrl");    
        this.printDebug("Response:\n  HTTP Status: $resp.statusCode\n  Message: $resp.message\n  Response Body: $resp.body");    
    }  


// ************************************************************************************************
// Entry point to doGet, doGetWithCrumb, doPost, doPut
//      
// ************************************************************************************************
    /**    
     * Gets the json content to the given url and ensures a 200 or 201 status on the response.    
     * If a negative status is returned, an error will be raised and the pipeline will fail.    
     */    
    def Object doGetHttpRequestWithJson(String userid, String pw, String requestUrl){    
        return doHttpRequestWithJson(userid,pw,"", requestUrl, "GET");    
    } 

    /**    
     * Gets the json content to the given url and ensures a 200 or 201 status on the response.    
     * If a negative status is returned, an error will be raised and the pipeline will fail.    
     */    
    def Object doGetHttpRequestWithJsonWithCrumb(String userid, String pw, String requestUrl){    
        return doHttpRequestWithJsonWithCrumb(userid,pw,"", requestUrl, "GET");    
    } 

    /**    
     * Posts the json content to the given url and ensures a 200 or 201 status on the response.    
     * If a negative status is returned, an error will be raised and the pipeline will fail.    
     */    
    def Object doPostHttpRequestWithJson(String userid, String pw, String json, String requestUrl){    
        return doHttpRequestWithJson(userid,pw,json, requestUrl, "POST");    
    }    

    /**    
     * Posts the json content to the given url and ensures a 200 or 201 status on the response.    
     * If a negative status is returned, an error will be raised and the pipeline will fail.    
     */    
    def Object doPutHttpRequestWithJson(String userid, String pw, String json, String requestUrl){    
        return doHttpRequestWithJson(userid,pw,json, requestUrl, "PUT");    
    }

    /**    
     * Post/Put the json content to the given url and ensures a 200 or 201 status on the response.    
     * If a negative status is returned, an error will be raised and the pipeline will fail.    
     * verb - PUT or POST    
     */    
    def String enc(String p)
    {
     return java.net.URLEncoder.encode(p, "UTF-8");
    }
    
// ************************************************************************************************
// Actual workers for HttpRequest with the verb
//      
// ************************************************************************************************
    def Object doHttpRequestWithJson(String userid, String pw, String json, String requestUrl, String verb){ 

        println ("userid: "+ userid)
        println ("pw: "+ pw)
        println ("json: "+json)  
        println ("requestUrl: "+requestUrl)
        println ("verb: "+verb)
        URL url = new URL(requestUrl);    
       
        HttpURLConnection connection = url.openConnection();    

        // Try to use Base64
        //if (userid != null && pw != null){
        //    String user_pass = userid + ":" + pw;
        //    String encoded = Base64.getEncoder().encode( user_pass.getBytes() );
        //    connection.setRequestProperty("Authorization", "Basic " + encoded);
        //  }

      if (userid != null && pw != null){
        String authStr = userid +":"+  pw;
        String encoding = DatatypeConverter.printBase64Binary(authStr.getBytes("utf-8"));
        connection.setRequestProperty("Content-Type", "application/json"); 
        connection.setRequestProperty("Authorization", "Basic " + encoding);
      }

      //HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      //connection.setRequestMethod("POST");
      //connection.setDoOutput(true);
      //connection.setRequestProperty("Authorization", "Basic " + encoding);

       connection.setRequestMethod(verb);    
       // connection.setRequestProperty("Authorization", "Basic " + encoded);
      

        //connection.setRequestProperty("Cookie", "p1=$userid; p2=$pw"); 
        connection.doOutput = true;    
       
        println (connection.outputStream)
        println ("....sending stream")

        if (json.length() > 0)
        {
         //write the payload to the body of the request    
         def writer = new OutputStreamWriter(connection.outputStream);    
         writer.write(json);    
         writer.flush();    
         writer.close();    
        }
    
        //post the request    
        connection.connect();    

        //parse the response    
        parseResponse(connection);    

        if(failure){    
            error("\n$verb to URL: $requestUrl\n    JSON: $json\n    HTTP Status: $statusCode\n    Message: $message\n    Response Body: $body");
            return null;    
        }   
                
        return jsonParse(body);
        
    //      println("Request ($verb):\n  URL: $requestUrl\n  JSON: $json");    
    //      println("Response:\n  HTTP Status: $statusCode\n  Message: $message\n  Response Body: $body");      
    } 

// ***********************************************************************************************************************
    def Object doHttpRequestWithJsonWithCrumb(String userid, String pw, String json, String requestUrl, String verb){ 

        println ("userid: "+ userid)
        println ("pw: "+ pw)
        println ("json: "+json)  
        println ("requestUrl: "+requestUrl)
        println ("verb: "+verb)
        URL url = new URL(requestUrl);    
       
        HttpURLConnection connection = url.openConnection();    

        // Try to use Base64
        //if (userid != null && pw != null){
        //    String user_pass = userid + ":" + pw;
        //    String encoded = Base64.getEncoder().encode( user_pass.getBytes() );
        //    connection.setRequestProperty("Authorization", "Basic " + encoded);
        //  }
      
      String authStr = userid +":"+  pw;
      String encoding = DatatypeConverter.printBase64Binary(authStr.getBytes("utf-8"));
      
      //HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      //connection.setRequestMethod("POST");
      //connection.setDoOutput(true);
      //connection.setRequestProperty("Authorization", "Basic " + encoding);

       connection.setRequestMethod(verb);    
       // connection.setRequestProperty("Authorization", "Basic " + encoded);
       connection.setRequestProperty("Content-Type", "application/json"); 
       connection.setRequestProperty("Authorization", "Basic " + encoding);

        //connection.setRequestProperty("Cookie", "p1=$userid; p2=$pw"); 
        connection.doOutput = true;    
       
        //println (connection.text)
        println ("....sending stream with crumb")


        if (json.length() > 0)
        {
         //write the payload to the body of the request    
         def writer = new OutputStreamWriter(connection.outputStream);    
         writer.write(json);    
         writer.flush();    
         writer.close();    
        }
    
        //post the request    
        connection.connect();    

        //parse the response    
        parseResponse(connection);    

        println ("Body of response: " + this.body);

        if(failure){    
            error("\n$verb to URL: $requestUrl\n    JSON: $json\n    HTTP Status: $statusCode\n    Message: $message\n    Response Body: $body");
            return null;    
        }   
                
        return jsonParse(body);
        
    //      println("Request ($verb):\n  URL: $requestUrl\n  JSON: $json");    
    //      println("Response:\n  HTTP Status: $statusCode\n  Message: $message\n  Response Body: $body");      
    } 

   def createAppHubESIndex(){
       /* curl -X PUT "localhost:9200/my_temp_index" -H 'Content-Type: application/json' -d'
                {
                    "settings": {
                                "number_of_shards" :   1,
                                "number_of_replicas" : 0
                                }
                }
                '
        */
        def builder = new groovy.json.JsonBuilder()
        def root = builder.settings {
                    number_of_shards 1
                    number_of_replicas 0
        }
        println(builder.toString())    
        def data = doPutHttpRequestWithJson(userid,pw,builder.toString(),"http://127.0.0.1:9200/my_test_index");
    }


   def updateNumReplicas(){
        /* curl -X PUT "localhost:9200/my_temp_index/_settings" -H 'Content-Type: application/json' -d'
                {
                    "number_of_replicas": 1
                }
        '
        */
       def builder = new groovy.json.JsonBuilder()
        def root = builder {
                    number_of_replicas 1
        }
        println(builder.toString())    
        // def data = doPutHttpRequestWithJson(userid,pw,builder.toString(),"http://127.0.0.1:9200/my_test_index/_settings");
   }

    @NonCPS
    def jsonParse(def json) {
        new groovy.json.JsonSlurperClassic().parseText(json)
    }

    def jsonBuildExample(){
       def builder = new groovy.json.JsonBuilder()
       def root = builder.people {
           person {
               firstName 'Guillame'
               lastName 'Laforge'
               // Named arguments are valid values for objects too
               address(
                       city: 'Paris',
                       country: 'France',
                       zip: 12345,
               )
               married true
               // a list of values
               conferences 'JavaOne', 'Gr8conf'
           }
       }

       // creates a data structure made of maps (Json object) and lists (Json array)
       assert root instanceof Map

       assert builder.toString() == '{"people":{"person":{"firstName":"Guillame","lastName":"Laforge","address":{"city":"Paris","country":"France","zip":12345},"married":true,"conferences":["JavaOne","Gr8conf"]}}}'
       return builder.toString()
       /*
       {
        "people":{
            "person":{
                "firstName":"Guillame",
                "lastName":"Laforge",
                "address":{
                            "city":"Paris",
                            "country":"France",
                            "zip":12345
                            },
                "married":true,
                "conferences":[
                                "JavaOne",
                                 "Gr8conf"
                                ]
            }
        }
        }
       */      
    }
//*************************************************************************************************************************************   
// initiateConnectionWithCrumb     
    def initiateConnectionWithCrumb(String url, String userid, String pw)
    {
     // Get appid
     def data = doGetHttpRequestWithJsonWithCrumb(userid,pw,"${url}");
     
    println "_class is " + data._class;
    println "crumb is " + data.crumb;
    println "crumbRequest is " + data.crumbRequestField;
    
     if (data.size() == 0) {
      println "Size of data returned is 0";
      return [false,"Nothing returned '" + Application + "' from '" + FromDomain + "' using the '" + Task + "' Task"];
     }
    
     println "initializing this object"
      def i=0;
      def taskid = 0;
      for (i=0;i<data.size();i++){
        println(data[i])
      }
    }


    def ServersInEnvironment(String url, String userid, String pw, String Environment)
    {
     def data = doGetHttpRequestWithJson(userid,pw,"${url}/dmadminweb/API/environment/" + enc(Environment));
     if (data.size() == 0)
      return [false, "Could not test server '" + server];
     else
      return [true,data];
    }
    
    def ServerRunning(String url, String userid, String pw, String server)
    {
     def data = doGetHttpRequestWithJson(userid,pw,"${url}/dmadminweb/API/testserver/" + enc(server));
     if (data.size() == 0)
      return [false, "Could not test server '" + server];
     else
      return [true,data];
    }
    
    def deployApplication(String url, String userid, String pw, String Application, String Environment)
    {
     def data = doGetHttpRequestWithJson(userid,pw,"${url}/dmadminweb/API/deploy/" + enc(Application) + "/" + enc(Environment) + "?wait=N");
     if (data.size() == 0)
      return [false, "Could not Deploy Application '" + Application + "' to Environment '" + Environment + "'"];
     else
      return [true,data];
    }

    def getLogs(String url, String userid, String pw, String deployid)
    {
     def done = 0;
     
     while (done == 0)
     {
      def res = this.isDeploymentDone(url, userid, pw, "$deployid");
     
      if (res != null)
      {
       if (res[0])
       {
        def s = res[1];

        if (res[1]['success'] && res[1]['iscomplete'])
         done = 1;
       }
       else
        done = 1;
      }
      
						sleep(10000); // 10 seconds
     } 
     
     def data = doGetHttpRequestWithJson(userid,pw,"${url}/dmadminweb/API/log/" + deployid);

     if (data == null || data.size() == 0)
      return [false, "Could not get log #" + deployid];
     
     def lines = data['logoutput'];
     def output = "";
     
     def i = 0;
     for (i = 0; i < lines.size(); i++) {
       output += lines[i] + "\n";
     }

     return [true,output];
    }
}


def aConnection = new appHub();
// Get the crumb and find users
// def data = aConnection.initiateConnectionWithCrumb("http://127.0.0.1:8080/crumbIssuer/api/json","admin","admin");
// def data = aConnection.moveApplication("http://127.0.0.1:8080/crumbIssuer/api/json","admin","admin","Uptime War for Tomcat;10","GLOBAL.My Pipeline.Development","Move to Integration");
//println(aConnection.jsonBuildExample())
aConnection.createAppHubESIndex()