//
//  LoginViewController.m
//  GlobalSight1
//
//  Created by System Administrator on 9/21/10.
//  Copyright 2010 Welocalize. All rights reserved.
//

#import "LoginViewController.h"
#import "GlobalSight1AppDelegate.h"
#import "StatusNavigation.h"
#import "TouchXML.h"

#define SAVE_FILE_LOCATION @"/tmp/globalsight-save.txt"
#define DATABASE_NAME @"GLOBALSIGHT.sql"


@implementation LoginViewController

@synthesize username;
@synthesize password;
@synthesize address;
@synthesize indicator;
@synthesize port;
@synthesize https;
@synthesize logo;

@synthesize webData;


static NSString *flag;
BOOL logining;
sqlite3 *database_;

- (void) viewWillAppear:(BOOL)animated {
	self.navigationController.navigationBarHidden = YES;
	self.navigationController.toolbarHidden = YES;
	
	[super viewWillAppear:animated];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	[self checkAndCreateDatabase];
	
	NSMutableDictionary *data = [self queryData];
	
	if (data != nil) {
		self.username.text = [data objectForKey:@"username"];
		self.address.text = [data objectForKey:@"address"];
		self.port.text = [data objectForKey:@"port"];
		NSString *hs = [data objectForKey:@"https"];
		if ([hs isEqualToString:@"1"]) {
			[self.https setOn:YES];
		}
	}
	
	[super viewDidLoad];
}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
	NSLog(@"there is memory warning in login view");
    [super didReceiveMemoryWarning];
}

- (void)viewDidUnload {
	[super viewDidUnload];
}

- (void)dealloc {
	[username release];
	[password release];
	[address release];
	[port release];
	[webData release];
	[indicator release];
	[https release];
	[logo release];
	
	[super dealloc];
}

#pragma mark -
#pragma mark Page Response

BOOL inputting = NO;// a flag shows whether one of the textbox is focusing.

/*
 scroll the screen upward to leave enough space for keyboard
 */
- (IBAction) startInput {
	if (!inputting) {
		inputting = YES;
		[UIView beginAnimations:nil context:NULL];
		[UIView setAnimationDuration:0.5];
		
		CGRect rect = self.view.frame;
		rect.origin.y -= 70;
		rect.size.height -= 70;
		self.view.frame = rect;
		
		[UIView commitAnimations];
	}
}
/*
 after inputting, scroll the screen downward
 */
- (void) endInput {
	if (inputting) {
		inputting = NO;
		[UIView beginAnimations:nil context:NULL];
		[UIView setAnimationDuration:0.5];
		
		CGRect rect = self.view.frame;
		rect.origin.y += 70;
		rect.size.height +=70;
		self.view.frame = rect;
		
		[UIView commitAnimations];
	}
}

//called when the address is done
- (IBAction) addressDoneEditing
{
	[port becomeFirstResponder];
}

//called when the port text is done
- (IBAction) portDoneEditing
{
	[username becomeFirstResponder];
}

//called when the username text is done
- (IBAction) usernameDoneEditing
{
	[password becomeFirstResponder];
}

//called when password text is done
- (IBAction) passwordDoneEditing
{
	[self loginPressed];
}

//called to make the pad disappear
- (IBAction) backgroundTap
{
	[address resignFirstResponder];
	[port resignFirstResponder];
	[username resignFirstResponder];
	[password resignFirstResponder];
	
	[self endInput];
}

// before login, check whether all the input text is filled
- (BOOL) validateInput {
	if ([address.text length] == 0) {
		UIAlertView *alert = [[UIAlertView alloc] 
							  initWithTitle:@"Invalid input" 
							  message:@"Please input the proper IP." 
							  delegate:self 
							  cancelButtonTitle:@"OK" 
							  otherButtonTitles:nil];
		[alert show];
		[alert release];
		return NO;
	}
	if ([port.text length] == 0) {
		UIAlertView *alert = [[UIAlertView alloc] 
							  initWithTitle:@"Invalid input" 
							  message:@"Please input the proper port number." 
							  delegate:self 
							  cancelButtonTitle:@"OK" 
							  otherButtonTitles:nil];
		[alert show];
		[alert release];
		return NO;
	}
	if ([username.text length] == 0) {
		UIAlertView *alert = [[UIAlertView alloc] 
							  initWithTitle:@"Invalid input" 
							  message:@"Please input your username." 
							  delegate:self 
							  cancelButtonTitle:@"OK" 
							  otherButtonTitles:nil];
		[alert show];
		[alert release];
		return NO;
	}
	if ([password.text length] == 0) {
		UIAlertView *alert = [[UIAlertView alloc] 
							  initWithTitle:@"Invalid input" 
							  message:@"Please input your password." 
							  delegate:self 
							  cancelButtonTitle:@"OK" 
							  otherButtonTitles:nil];
		[alert show];
		[alert release];
		return NO;
	}
	
	return YES;
}

//called to when login button pressed
- (IBAction) loginPressed
{
	if ([self validateInput] == NO) {
		return;
	}
	if (logining == NO) {
		logining = YES;
		
		[self backgroundTap];
		[indicator startAnimating];
		
		NSString *soapMessage = [NSString stringWithFormat:
								 WEBSERVICE_HEADER
								 "<login xmls=\"http://www.globalsight.com/webservices/\">\n"
								 "<p_username>%@</p_username>"
								 "<p_password>%@</p_password>"
								 "</login>\n"
								 WEBSERVICE_TAIL,username.text,password.text
								 ];
		
		BOOL setting = [https isOn];//http or https
		NSString *addressText = NULL;
		
		if (setting) {
			addressText = [NSString stringWithFormat:
						   @"https://%@:%@/globalsight/services/AmbassadorWebService", 
						   address.text, port.text];
			flag = @"1";
		} else {
			addressText = [NSString stringWithFormat:
						   @"http://%@:%@/globalsight/services/AmbassadorWebService", 
						   address.text, port.text];
			flag = @"0";
		}
		
		[GlobalSight1AppDelegate app].url = [NSURL URLWithString:addressText];
		[GlobalSight1AppDelegate app].userId = username.text;
		
		NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[GlobalSight1AppDelegate app].url];
		NSString *msgLength = [NSString stringWithFormat:@"%d", [soapMessage length]];
		
		[request addValue: @"text/xml; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
		[request addValue: @"http://www.globalsight.com/webservices/login" forHTTPHeaderField:@"SOAPAction"];
		
		[request addValue: msgLength forHTTPHeaderField:@"Content-Length"];
		[request setHTTPMethod:@"POST"];
		[request setHTTPBody: [soapMessage dataUsingEncoding:NSUTF8StringEncoding]];
		
		NSURLConnection *theConnection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
		
		if (theConnection) {
			//if the connection works, go on.
			webData = [[NSMutableData data] retain];
		} 
	}
}

#pragma mark -
#pragma mark Webservice Part
- (void) connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
	[webData appendData:data];
}
- (void) connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
	[webData setLength:0];
}
- (BOOL) connection:(NSURLConnection *)connection 
	canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace {
	return [protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust];
}
- (void) connection:(NSURLConnection *)connection 
	didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge {
	[challenge.sender useCredential:[NSURLCredential credentialForTrust:challenge.protectionSpace.serverTrust] 
		 forAuthenticationChallenge:challenge];
	[challenge.sender continueWithoutCredentialForAuthenticationChallenge:challenge];
}
- (void) connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
	[connection release];
	[webData release];
	logining = NO;
	
	[indicator stopAnimating];
	
	UIAlertView *alert = [[UIAlertView alloc] 
						  initWithTitle:@"Login failed" 
						  message:@"Request timed out.\nPlease check your configuration." 
						  delegate:self 
						  cancelButtonTitle:@"OK" 
						  otherButtonTitles:nil];
	[alert show];
	[alert release];
}
- (void) connectionDidFinishLoading:(NSURLConnection *)connection
{
	[indicator stopAnimating];
	logining = NO;
	
	NSString *theXMLString = [[NSString alloc] 
						initWithBytes:[webData mutableBytes] 
						length:[webData length] 
						encoding:NSUTF8StringEncoding];
	//NSLog(@"login theXMLString :%@", theXMLString);
	CXMLDocument *doc = [[[CXMLDocument alloc] 
						  initWithXMLString:theXMLString 
						  options:0 error:nil] autorelease];
	NSArray *nodes = [doc nodesForXPath:@"//loginReturn" error:nil];
	[webData release];
	
	if ([nodes count] > 0) {
		[theXMLString release];
		
		CXMLElement *node = [nodes objectAtIndex:0];
		NSString *token = [[node childAtIndex:0] stringValue];
		token = [token stringByReplacingOccurrencesOfString:@"<" withString:@"&lt;"];
		token = [token stringByReplacingOccurrencesOfString:@">" withString:@"&gt;"];
		
		[GlobalSight1AppDelegate app].token = token;
		
		[self clearData];
		[self insertData:username.text url:address.text po:port.text hs:flag];
		
		StatusNavigation *statusController = [[StatusNavigation alloc] initWithNibName:@"StatusNavigation" bundle:nil];
		[self.navigationController pushViewController:statusController animated:YES];
	} else {
		NSRange range1 = [theXMLString rangeOfString:@"&lt;error&gt;"];
		NSRange range2 = [theXMLString rangeOfString:@"&lt;/error&gt;"];
		
		if (range1.location != NSNotFound && range2.location != NSNotFound) {
			NSString *faultMsg = [NSString stringWithFormat:@"%@", 
								  [theXMLString substringWithRange:
								   NSMakeRange(range1.location + range1.length, 
											   range2.location - range1.location - range1.length)]];
			[theXMLString release];
			
			UIAlertView *alert = [[UIAlertView alloc] 
								  initWithTitle:@"Login failed" 
								  message:faultMsg 
								  delegate:self 
								  cancelButtonTitle:@"OK" 
								  otherButtonTitles:nil];
			[alert show];
			[alert release];
		} else {
			[theXMLString release];
			
			UIAlertView *alert = [[UIAlertView alloc] 
								  initWithTitle:@"Login failed" 
								  message:@"Access denied" 
								  delegate:self 
								  cancelButtonTitle:@"OK" 
								  otherButtonTitles:nil];
			[alert show];
			[alert release];
		}
	}

	[connection release];
}

#pragma mark -
#pragma mark database part

-(void) checkAndCreateDatabase{
	NSArray *documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDir = [documentPaths objectAtIndex:0];
	NSString *databasePath = [documentsDir stringByAppendingPathComponent:DATABASE_NAME];
	
	NSFileManager *fileManager = [NSFileManager defaultManager];
	BOOL found = [fileManager fileExistsAtPath:databasePath];
	
	if(found) {
		//NSLog(@"the database already exists");
		
		if (sqlite3_open([databasePath UTF8String], &database_) != SQLITE_OK) {
			sqlite3_close(database_);
		}
	} else {
		//NSLog(@"cannot find database");
	}
	
	if (sqlite3_open([databasePath UTF8String], &database_) == SQLITE_OK) {
		[self createTable:database_];
	} else {
		sqlite3_close(database_);
	}
}

// create the globalsight table
- (void) createTable:(sqlite3 *) db {
	const char *createSql = "CREATE TABLE GLOBALSIGHT (id integer primary key, name text, url text, port text, https char(1))";
	sqlite3_stmt *statement;
	if (sqlite3_prepare_v2(db, createSql, -1, &statement, nil) != SQLITE_OK) {
		//NSLog(@"Error: failed to prepare stement: create globalsight table");
		return;
	}
	int success = sqlite3_step(statement);
	sqlite3_finalize(statement);
	if (success != SQLITE_DONE) {
		//NSLog(@"Error: failed to dehydrate: create tabel");
	} else {
		//NSLog(@"Create table succeed");
	}
}

// insert data into table 
- (void) insertData:(NSString *)name url:(NSString *)url 
		   po:(NSString *)po hs:(NSString *) hs {
	sqlite3_stmt *statement;
	static char *insertSql = "INSERT INTO GLOBALSIGHT (name, url, port, https) values(?, ?, ?, ?)";
	int success = sqlite3_prepare_v2(database_, insertSql, -1, &statement, NULL);
	if (success != SQLITE_OK) {
		//NSLog(@"Error: failed to insert data");
		return;
	}
	
	sqlite3_bind_text(statement, 1, [name cStringUsingEncoding:1], -1, SQLITE_TRANSIENT);
	sqlite3_bind_text(statement, 2, [url cStringUsingEncoding:1], -1, SQLITE_TRANSIENT);
	sqlite3_bind_text(statement, 3, [po cStringUsingEncoding:1], -1, SQLITE_TRANSIENT);
	sqlite3_bind_text(statement, 4, [hs cStringUsingEncoding:1], -1, SQLITE_TRANSIENT);
	
	success = sqlite3_step(statement);
	sqlite3_finalize(statement);
	if (success == SQLITE_ERROR) {
		//NSLog(@"Error: failed to insert data with message");
	} else {
		//NSLog(@"Insert data succeed");
	}
}

// query data from database, there will be only one piece of data in the database.
- (NSMutableDictionary *) queryData {
	sqlite3_stmt *statement;
	const char *querySql = "SELECT * FROM GLOBALSIGHT";
	
	if (sqlite3_prepare_v2(database_, querySql, -1, &statement, NULL) != SQLITE_OK) {
		//NSLog(@"Error: failed to prepare statement with message: query");
		return nil;
	}
	
	NSMutableDictionary *data = [NSMutableDictionary dictionary];
	while (sqlite3_step(statement) == SQLITE_ROW) {
		char *name = (char *)sqlite3_column_text(statement, 1);
		char *url = (char *)sqlite3_column_text(statement, 2);
		char *po = (char *)sqlite3_column_text(statement, 3);
		char *hs = (char *)sqlite3_column_text(statement, 4);
		
		[data setObject:[NSString stringWithCString:name encoding:1] forKey:@"username"];
		[data setObject:[NSString stringWithCString:po encoding:1] forKey:@"port"];
		[data setObject:[NSString stringWithCString:url encoding:1] forKey:@"address"];
		[data setObject:[NSString stringWithCString:hs encoding:1] forKey:@"https"];
	}
	
	sqlite3_finalize(statement);
	return data;
}

// delete data from database
- (void) clearData {
	const char *deleteSql = "DELETE FROM GLOBALSIGHT";
	sqlite3_stmt *statement;
	if (sqlite3_prepare_v2(database_, deleteSql, -1, &statement, nil) != SQLITE_OK) {
		//NSLog(@"Error: failed to prepare statement: delete data");
		return;
	}
	int success = sqlite3_step(statement);
	sqlite3_finalize(statement);
	if (success != SQLITE_DONE) {
		//NSLog(@"Error: failed to detele data");
	} else {
		//NSLog(@"Delete data success");
	}
}
@end
