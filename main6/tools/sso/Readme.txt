Under folder "ssoidpdemo" is a demo project (java, Eclipse project) for GlobalSight SSO 
IdP (Identify Provider). And "tomcat-sso-idp-demosite.zip" is the built package which 
includes tomcat server and "ssoidpdemo" web site.

Unzip "tomcat-sso-idp-demosite.zip" and navigate to folder "tomcat-sso-idp-demosite\bin", 
run startup command to start this tomcat server. And the IdP service URL (which is used in 
GlobalSight SSO configuration) is "http://localhost:8090/ssoidpdemo/ssoservice.jsp". 

Use Case 1: 
1. Enable Single sign-on for one company in GlobalSight, and set the IdP url as 
"http://localhost:8090/ssoidpdemo/ssoservice.jsp". 
2. Edit SSO user mapping
Edit one user in this company, and set SSO user name as "user1" (user1 is store in IdP demo)
3. Navigate to GlobalSight login page, enter user name "user1", password "password", check 
"Is SSO Account" and then click login button. 

Use Case 2:
1. Make sure there is company "way" in GlobalSight, and mapping one GlobalSight user (who 
belong to "way" company) to SSO user name "user1". You can change the company name and 
sso user id in "index.jsp". 
2. Navigate to "http://localhost:8090/ssoidpdemo/index.jsp" to test this case. 