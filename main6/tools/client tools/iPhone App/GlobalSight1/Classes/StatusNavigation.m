//
//  StatusNavigation.m
//  GlobalSight1
//
//  Created by Administrator on 1/10/11.
//  Copyright 2011 Welocalize. All rights reserved.
//

#import "StatusNavigation.h"
#import "GlobalSight1AppDelegate.h"
#import "JobViewController.h"
#import "TouchXML.h"

@implementation StatusNavigation

@synthesize indicator;
@synthesize webData;
@synthesize statusData;
@synthesize table;

BOOL searchingData = NO;

- (void) viewWillAppear:(BOOL)animated
{
	[self searchData];
	[super viewWillAppear:animated];
}

- (void) viewDidLoad {
	statusData = [[NSMutableDictionary alloc] init];
	
	self.navigationController.navigationBarHidden = NO;
	self.navigationItem.title = @"Job Status";
	
	[self.navigationController setToolbarHidden:NO animated:NO];
	self.navigationController.toolbar.barStyle = UIBarStyleDefault;
	
	UIImage *refreshImage = [UIImage imageNamed:@"refresh.png"];
	UIButton *customBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 19, 24)];
	[customBtn addTarget:self action:@selector(refresh:) forControlEvents:UIControlEventTouchUpInside];
	[customBtn setBackgroundImage:refreshImage forState:UIControlStateNormal];
	UIBarButtonItem *refreshButton = [[UIBarButtonItem alloc] initWithCustomView:customBtn];
	[customBtn release];
	
	UITextField *text = [[UITextField alloc] initWithFrame:CGRectMake(0, 10, 250, 24)];
	text.font = [UIFont italicSystemFontOfSize:12];
	text.textAlignment = UITextAlignmentCenter;
	text.enabled = NO;
	UIBarButtonItem *textField = [[UIBarButtonItem alloc] initWithCustomView:text];
	[text release];
	
	NSArray *myToolBarItems = [[NSArray alloc] initWithObjects:refreshButton, textField, nil];
	[refreshButton release];
	[textField release];
	[self setToolbarItems:myToolBarItems animated:YES];
	[myToolBarItems release];
	
	[super viewDidLoad];
}

- (void) refresh:(id) sender {
	[self searchData];
}

- (void) dealloc
{	
	[indicator release];
	[webData release];
	[table release];
	[statusData release];
	[super dealloc];
}

#pragma mark -
#pragma mark webservice part

- (void) searchData
{
	if (searchingData == NO) {
		searchingData = YES;
		[indicator startAnimating];
		
		NSString *soapMsg = [NSString stringWithFormat:
							 WEBSERVICE_HEADER
							 "<getCountsByJobState xmls=\"http://www.globalsight.com/webservices/\">\n"
							 "<p_accessToken>%@</p_accessToken>"
							 "</getCountsByJobState>\n"
							 WEBSERVICE_TAIL, [GlobalSight1AppDelegate app].token
							 ];
		NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[GlobalSight1AppDelegate app].url];
		NSString *msgLength = [NSString stringWithFormat:@"%d", [soapMsg length]];
		
		[request addValue: @"text/xml; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
		[request addValue: @"http://www.globalsight.com/webservices/getCountsByJobState" forHTTPHeaderField:@"SOAPAction"];
		
		[request addValue: msgLength forHTTPHeaderField:@"Content-Length"];
		[request setHTTPMethod:@"POST"];
		[request setHTTPBody: [soapMsg dataUsingEncoding:NSUTF8StringEncoding]];
		
		NSURLConnection *theConnection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
		
		if (theConnection) {
			//if the connection works, go on.
			webData = [[NSMutableData data] retain];
		}
	}
}

- (void) connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
	[webData appendData:data];
}
- (void) connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
	[webData setLength:0];
}
- (void) connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
	[connection release];
	searchingData = NO;
	[webData release];
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
	[self updateTime];
	
	[connection release];
	[indicator stopAnimating];
	searchingData = NO;
	
	NSString *theXMLString = [[NSString alloc] 
							  initWithBytes:[webData mutableBytes] 
							  length:[webData length] 
							  encoding:NSUTF8StringEncoding];
	//NSLog(@"theXMLString: %@", theXMLString);
	CXMLDocument *doc = [[[CXMLDocument alloc] initWithXMLString:theXMLString options:0 error:nil] autorelease];
	NSArray *nodes = [doc nodesForXPath:@"//getCountsByJobStateReturn" error:nil];
	[webData release];
	
	if ([nodes count] > 0) {
		[theXMLString release];
		CXMLElement *node = [nodes objectAtIndex:0];
		NSString *returnValue = [[node childAtIndex:0] stringValue];
		//NSLog(@"%@", returnValue);
		
		doc = [[[CXMLDocument alloc] 
				initWithXMLString:returnValue 
				options:0 error:nil] autorelease];
		
		NSArray *jobInfo = [doc nodesForXPath:@"//counts//countByState" error:nil];
		
		int i;
		for (i = 0; i < [jobInfo count]; i++) {
			CXMLElement *info = [jobInfo objectAtIndex:i];
			
			NSString *state = NULL;
			NSString *count = NULL;
			
			int j;
			for (j = 0; j < [info childCount]; j++) {
				NSString *tagName = [[info childAtIndex:j] name];
				
				if ([tagName isEqualToString:@"state"]) {
					state = [[info childAtIndex:j] stringValue];
				}
				if ([tagName isEqualToString:@"count"]) {
					count = [[info childAtIndex:j] stringValue];
				}
			}
			[statusData setObject:count forKey:state];
		}
		[table reloadData];
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
								  message:@"Access denied." 
								  delegate:self 
								  cancelButtonTitle:@"OK" 
								  otherButtonTitles:nil];
			[alert show];
			[alert release];
		}
	}
}

#pragma mark -
#pragma mark table part

- (NSInteger) tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return 7;
}

- (UITableViewCell *) tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSInteger row = [indexPath row];
	
	static NSString *fid = @"jobStatusIdifer";
	UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:fid];
	
	if (cell == nil) {
		cell = [[[UITableViewCell alloc] 
				 initWithStyle:UITableViewCellStyleValue1
				 reuseIdentifier:fid] autorelease];
	}
	
	int count = 0;
	switch (row) {
		case 0:// PENDING
			count = [[statusData objectForKey:@"PENDING"] intValue] + 
					[[statusData objectForKey:@"BATCH_RESERVED"] intValue] + 
					[[statusData objectForKey:@"IMPORT_FAILED"] intValue] + 
					[[statusData objectForKey:@"ADDING_FILES"] intValue];
			cell.textLabel.text = @"Pending";
			cell.detailTextLabel.text = [NSString stringWithFormat:@"(%d)", count];
			break;
		case 1:// READY
			count = [[statusData objectForKey:@"READY_TO_BE_DISPATCHED"] intValue];
			cell.textLabel.text = @"Ready";
			cell.detailTextLabel.text = [NSString stringWithFormat:@"(%d)", count];
			break;
		case 2:// IN PROGRESS
			count = [[statusData objectForKey:@"DISPATCHED"] intValue];
			cell.textLabel.text = @"In Progress";
			cell.detailTextLabel.text = [NSString stringWithFormat:@"(%d)", count];
			break;
		case 3:// LOCALIZED
			count = [[statusData objectForKey:@"LOCALIZED"] intValue];
			cell.textLabel.text = @"Localized";
			cell.detailTextLabel.text = [NSString stringWithFormat:@"(%d)", count];
			break;
		case 4:// EXPORTED
			count = [[statusData objectForKey:@"EXPORTED"] intValue] + 
						[[statusData objectForKey:@"EXPORT_FAILED"] intValue];
			cell.textLabel.text = @"Exported";
			cell.detailTextLabel.text = [NSString stringWithFormat:@"(%d)", count];
			break;
		case 5:// ARCHIVED
			count = [[statusData objectForKey:@"ARCHIVED"] intValue];
			cell.textLabel.text = @"Archived";
			cell.detailTextLabel.text = [NSString stringWithFormat:@"(%d)", count];
			break;
		case 6:// ALL STATUS
			count = [[statusData objectForKey:@"PENDING"] intValue] + 
					[[statusData objectForKey:@"BATCH_RESERVED"] intValue] + 
					[[statusData objectForKey:@"IMPORT_FAILED"] intValue] + 
					[[statusData objectForKey:@"ADDING_FILES"] intValue] + 
					[[statusData objectForKey:@"READY_TO_BE_DISPATCHED"] intValue] + 
					[[statusData objectForKey:@"DISPATCHED"] intValue] + 
					[[statusData objectForKey:@"LOCALIZED"] intValue] +
					[[statusData objectForKey:@"EXPORTED"] intValue] + 
					[[statusData objectForKey:@"EXPORT_FAILED"] intValue] + 
					[[statusData objectForKey:@"ARCHIVED"] intValue];
			cell.textLabel.text = @"All Status";
			cell.detailTextLabel.text = [NSString stringWithFormat:@"(%d)", count];
			break;
		default:
			break;
	}
	cell.detailTextLabel.font = [UIFont italicSystemFontOfSize:13];
	cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
	return cell;
}

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	UITableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
	
	JobViewController *jobController = [[JobViewController alloc] 
										initWithNibName:@"JobViewController" bundle:nil];
	switch (row) {
		case 0:// PENDING
			jobController.jobStatus = @"'PENDING','BATCH_RESERVED','IMPORT_FAILED','ADDING_FILES'";
			jobController.jobDisplayStatus = @"Pending";
			break;
		case 1:// READY
			jobController.jobStatus = @"'READY_TO_BE_DISPATCHED'";
			jobController.jobDisplayStatus = @"Ready";
			break;
		case 2:// IN PROGRESS
			jobController.jobStatus = @"'DISPATCHED'";
			jobController.jobDisplayStatus = @"Progress";
			break;
		case 3:// LOCALIZED
			jobController.jobStatus = @"'LOCALIZED'";
			jobController.jobDisplayStatus = @"Localized";
			break;
		case 4:// EXPORTED
			jobController.jobStatus = @"'EXPORTED','EXPORT_FAILED'";
			jobController.jobDisplayStatus = @"Exported";
			break;
		case 5:// ARCHIVED
			jobController.jobStatus = @"'ARCHIVED'";
			jobController.jobDisplayStatus = @"Archived";
			break;
		case 6:// ALL STATUS
			jobController.jobStatus = @"'PENDING','BATCH_RESERVED',"
					"'IMPORT_FAILED','ADDING_FILES','READY_TO_BE_DISPATCHED',"
					"'DISPATCHED','LOCALIZED','EXPORTED','EXPORT_FAILED','ARCHIVED'";
			jobController.jobDisplayStatus = @"All";
			break;
		default:
			break;
	}
	NSString *count = cell.detailTextLabel.text;
	jobController.totalNo = [count substringWithRange:NSMakeRange(1, [count length] - 1)];
	[tableView deselectRowAtIndexPath:indexPath animated:YES];
	[self.navigationController pushViewController:jobController animated:YES];
}

@end
