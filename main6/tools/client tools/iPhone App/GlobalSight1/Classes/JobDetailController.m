//
//  JobDetailController.m
//  GlobalSight1
//
//  Created by System Administrator on 9/21/10.
//  Copyright 2010 Welocalize. All rights reserved.
//

#import "JobDetailController.h"
#import "GlobalSight1AppDelegate.h"
#import "TouchXML.h"

#define ROWS_OF_SECOUND_PART_TABLE 2
#define ROWS_OF_THIRD_PART_TABLE 9
#define ROWS_OF_FOURTH_PART_TABLE 7
#define READY_TO_DISPATH @"Ready to be dispatched"
#define HEIGHT_OF_SINGLE_ROW 44

@implementation JobDetailController

@synthesize jobId;
@synthesize jobStatus;
@synthesize indicator;
@synthesize webData;
@synthesize firstSectionData;
@synthesize secondSectionData;
@synthesize thirdSectionData;
@synthesize fourthSectionData;
@synthesize workflowData;
@synthesize table;


BOOL searchingData;


- (void) viewWillAppear:(BOOL)animated {
	[self searchJobData];
	// init the discard button on the right top corner
	if ([jobStatus isEqualToString:@"Pending"] || 
		[jobStatus isEqualToString:@"Ready"] || 
		[jobStatus isEqualToString:@"In Progress"]) {
		UIImage *discardImage = [UIImage imageNamed:@"discard.png"];
		UIButton *customBtn1 = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 82, 30)];
		[customBtn1 addTarget:self action:@selector(discard:) forControlEvents:UIControlEventTouchUpInside];
		[customBtn1 setBackgroundImage:discardImage forState:UIControlStateNormal];
		UIBarButtonItem *discardButton = [[UIBarButtonItem alloc] initWithCustomView:customBtn1];
		[customBtn1 release];
		self.navigationItem.rightBarButtonItem = discardButton;
		[discardButton release];
	} else {
		self.navigationItem.rightBarButtonItem = nil;
	}

	[super viewWillAppear:animated];
}

- (void) viewWillDisappear:(BOOL)animated {
	[super viewWillDisappear:animated];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	self.navigationItem.title = @"Job Detail";
	
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
	NSDate *now = [[NSDate alloc] init];
	text.text = [NSString stringWithFormat:@"Last Update: %@", 
				 [[now description] substringWithRange:NSMakeRange(0, 19)]];
	[now release];
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

- (IBAction) discard:(id) sender {
	NSString *msg = @"This will permanently remove this "
			"job and all associated workflows from the system.\n "
			"Note: There may be a short delay when job is being discarded.";
	UIAlertView *alert = [[UIAlertView alloc] 
						initWithTitle:@"Warning" 
						message:msg
						delegate:self 
						cancelButtonTitle:@"No" 
						otherButtonTitles:@"Yes", nil];
	[alert show];
	[alert release];
}

- (IBAction) refresh:(id)sender {
	[self searchJobData];
}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
	NSLog(@"there is memory warning in job detail view");
    [super didReceiveMemoryWarning];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

- (void)viewDidUnload {
	self.jobId = nil;
	self.table = nil;
	self.firstSectionData = nil;
	self.secondSectionData = nil;
	self.thirdSectionData = nil;
	self.fourthSectionData = nil;
    [super viewDidUnload];
}

- (void)dealloc {
	[jobId release];
	[jobStatus release];
	[indicator release];
	[webData release];
	[table release];
	[firstSectionData release];
	[secondSectionData release];
	[thirdSectionData release];
	[fourthSectionData release];
	[workflowData release];
	[super dealloc];
}

#pragma mark -
#pragma mark alertView
- (void) alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
	if (buttonIndex == 1) {
		[self discardJob];
	}
}

#pragma mark -
#pragma mark webservice part
/*
 use webservice to get job and workflow infomation
 */
- (void) searchJobData {
	if (searchingData == NO) {
		searchingData = YES;
		
		[indicator startAnimating];
		NSString *soapMsg = [[NSString alloc] initWithFormat:
							 WEBSERVICE_HEADER
							 "<getJobAndWorkflowInfo xmls=\"http://www.globalsight.com/webservices/\">\n"
							 "<p_accessToken>%@</p_accessToken>"
							 "<p_jobId>%@</p_jobId>"
							 "</getJobAndWorkflowInfo>\n"
							 WEBSERVICE_TAIL,[GlobalSight1AppDelegate app].token, jobId];
		NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[GlobalSight1AppDelegate app].url];
		NSString *msgLength = [[NSString alloc] initWithFormat:@"%d", [soapMsg length]];
		
		[request addValue: @"text/xml; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
		[request addValue: @"http://www.globalsight.com/webservices/getJobAndWorkflowInfo" forHTTPHeaderField:@"SOAPAction"];
		[request addValue: msgLength forHTTPHeaderField:@"Content-Length"];
		[request setHTTPMethod:@"POST"];
		[request setHTTPBody: [soapMsg dataUsingEncoding:NSUTF8StringEncoding]];
		[msgLength release];
		[soapMsg release];
		
		NSURLConnection *theConnection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
		
		if (theConnection) {
			//if the connection works, go on.
			webData = [[NSMutableData data] retain];
		}
	}
}

- (void) discardJob {
	if (searchingData == NO) {
		[indicator startAnimating];
		NSString *soapMsg = [[NSString alloc] initWithFormat:
							 WEBSERVICE_HEADER
							 "<cancelJobById xmls=\"http://www.globalsight.com/webservices/\">\n"
							 "<p_accessToken>%@</p_accessToken>"
							 "<p_jobId>%d</p_jobId>"
							 "</cancelJobById>\n"
							 WEBSERVICE_TAIL,[GlobalSight1AppDelegate app].token, [jobId longLongValue]];
		NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[GlobalSight1AppDelegate app].url];
		NSString *msgLength = [[NSString alloc] initWithFormat:@"%d", [soapMsg length]];
		
		[request addValue: @"text/xml; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
		[request addValue: @"http://www.globalsight.com/webservices/cancelJobById" forHTTPHeaderField:@"SOAPAction"];
		[request addValue: msgLength forHTTPHeaderField:@"Content-Length"];
		[request setHTTPMethod:@"POST"];
		[request setHTTPBody: [soapMsg dataUsingEncoding:NSUTF8StringEncoding]];
		[msgLength release];
		[soapMsg release];
		
		NSURLConnection *theConnection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
		
		if (theConnection) {
			//if the connection works, go on.
			webData = [[NSMutableData data] retain];
		}
	}
}

- (void) dispathWorkflow:(id) sender {
	[sender setHidden:YES];
	[indicator startAnimating];
	
	NSString *workflowId = [NSString stringWithFormat:@"%d", [sender tag]];
	//[sender setHidden:YES];
	NSString *soapMsg = [[NSString alloc] initWithFormat:
						 WEBSERVICE_HEADER
						 "<dispatchWorkflow xmls=\"http://www.globalsight.com/webservices/\">\n"
						 "<p_accessToken>%@</p_accessToken>"
						 "<p_wfIds>%@</p_wfIds>"
						 "</dispatchWorkflow>\n"
						 WEBSERVICE_TAIL,[GlobalSight1AppDelegate app].token, workflowId];
	NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[GlobalSight1AppDelegate app].url];
	NSString *msgLength = [[NSString alloc] initWithFormat:@"%d", [soapMsg length]];
	
	[request addValue: @"text/xml; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
	[request addValue: @"http://www.globalsight.com/webservices/dispatchWorkflow" forHTTPHeaderField:@"SOAPAction"];
	[request addValue: msgLength forHTTPHeaderField:@"Content-Length"];
	[request setHTTPMethod:@"POST"];
	[request setHTTPBody: [soapMsg dataUsingEncoding:NSUTF8StringEncoding]];
	[msgLength release];
	[soapMsg release];
	
	NSURLConnection *theConnection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
	if (theConnection) {
		//if the connection works, go on.
		webData = [[NSMutableData data] retain];
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
/*
 after webservice is back, parser the result into certain pattern
 */
- (void) connectionDidFinishLoading:(NSURLConnection *)connection
{
	[self updateTime];
	
	[connection release];
	searchingData = NO;
	[indicator stopAnimating];
	NSString *theXMLString = [[NSString alloc] 
						initWithBytes:[webData mutableBytes] 
						length:[webData length] 
						encoding:NSUTF8StringEncoding];
	//NSLog(@"%@", theXMLString);
	
	CXMLDocument *doc = [[[CXMLDocument alloc] 
						  initWithXMLString:theXMLString 
						  options:0 error:nil] autorelease];
	NSArray *nodes = [doc nodesForXPath:@"//getJobAndWorkflowInfoReturn" error:nil];
	NSArray *dispatchNodes = [doc nodesForXPath:@"//dispatchWorkflowReturn" error:nil];
	NSArray *discardNodes = [doc nodesForXPath:@"//cancelJobByIdReturn" error:nil];
	//webData = nil;
	[webData release];
	
	if ([nodes count] > 0) {
		[theXMLString release];
		
		CXMLElement *node = [nodes objectAtIndex:0];
		NSString *returnValue = [[node childAtIndex:0] stringValue];
		//NSLog(@"%@", returnValue);
		
		doc = [[[CXMLDocument alloc] 
				initWithXMLString:returnValue 
				options:0 error:nil] autorelease];
		
		// this tmparray is used for the first section info
		NSMutableArray *tmpFirstSectionArray = [[NSMutableArray alloc] init];
		// this tmparray is used for the second section info
		NSMutableArray *tmpSecondSectionArray = [[NSMutableArray alloc] init];
		// this tmparray is used for the third section info
		NSMutableArray *tmpThirdSectionArray = [[NSMutableArray alloc] init];
		// this tmparray is used for the fourth section info
		NSMutableArray *tmpFourSectionArray = [[NSMutableArray alloc] init];
		
		// get job id
		NSString *jobIdText = [[NSString alloc] initWithFormat:@"%@", self.jobId];
		[tmpFirstSectionArray addObject:jobIdText];// set jobId at the first cell
		[jobIdText release];
		
		NSArray *jobInfo = [doc nodesForXPath:@"//jobInfo" error:nil];
		CXMLElement *job = [jobInfo objectAtIndex:0];
		
		int i;
		for (i = 0; i < [job childCount]; i++) {
			NSString *tagName = [[job childAtIndex:i] name];
			
			// get job name
			if ([tagName isEqualToString:@"name"]) {
				NSString *jobNameText = [[NSString alloc] initWithFormat:@"%@", 
										 [[job childAtIndex:i] stringValue]];
				[tmpFirstSectionArray addObject:jobNameText];// set jobName at the second cell of first section
				[jobNameText release];
			}
			// get source files info
			if ([tagName isEqualToString:@"sourcePages"]) {
				NSArray *sourceFiles = [doc nodesForXPath:@"//jobInfo//sourcePages//sourcePage" error:nil];
				
				//int totalWordCount = 0;
				
				for (CXMLElement *sourcePage in sourceFiles) {
					NSString *nameText = NULL;
					NSString *numberText = NULL;
					
					int j;
					for (j = 0; j < [sourcePage childCount]; j++) {
						NSString *tagNameForSource = [[sourcePage childAtIndex:j] name];
						
						if ([tagNameForSource isEqualToString:@"externalPageId"]) {
							// get file name
							NSString *externalPage = [[sourcePage childAtIndex:j] stringValue];
							NSRange range = [externalPage rangeOfString:@"\\" options:NSBackwardsSearch];
							nameText = [externalPage substringFromIndex:range.location + 1];
							
							// if filename has string "(" or ")" in the beginning, add the string to the filename
							if ([externalPage hasPrefix:@"("]) {
								NSRange sheetEnd = [externalPage rangeOfString:@")"];
								NSString *sheetName =  [externalPage substringToIndex:sheetEnd.location + 1];
								nameText = [NSString stringWithFormat:@"%@%@", sheetName, nameText];
							}
						}
						if ([tagNameForSource isEqualToString:@"wordCount"]) {
							numberText = [[sourcePage childAtIndex:j] stringValue];
						}
					}
					NSDictionary *ele = [[NSDictionary alloc] initWithObjectsAndKeys:
										 nameText, @"nameText", 
										 numberText, @"valueText", nil];
					//totalWordCount += [numberText intValue];
					[tmpSecondSectionArray addObject:ele];
					[ele release];
				}
			}
			// get workflow info for the 3rd and 4th table
			if ([tagName isEqualToString:@"workflowInfo"]) {
				NSArray *workflows = [doc nodesForXPath:@"//jobInfo//workflowInfo//workflow" error:nil];
				
				for (CXMLElement *workflow in workflows) {
					NSMutableString *wid = [NSMutableString stringWithCapacity:1];// id
					NSMutableString *tl = [NSMutableString stringWithCapacity:1];// target locale
					NSMutableString *wc = [NSMutableString stringWithCapacity:1];// word count
					NSMutableString *complete = [NSMutableString stringWithCapacity:1];// complete
					NSMutableString *state = [NSMutableString stringWithCapacity:1];// state
					NSMutableString *ca = [NSMutableString stringWithCapacity:1];// current activity
					NSMutableString *etcd = [NSMutableString stringWithCapacity:1];// estimated translate completion date
					NSMutableString *ecd = [NSMutableString stringWithCapacity:1];// estimated completion date
					NSMutableString *slr = [NSMutableString stringWithCapacity:1];// subLevRepetition
					NSMutableString *slm = [NSMutableString stringWithCapacity:1];// subLevMatch
					NSMutableString *isInContextMatch = [NSMutableString stringWithCapacity:1];// isInContextMatch
					
					int z;
					for (z = 0; z < [workflow childCount]; z++) {
						NSString *tagNameForWorkflow = [[workflow childAtIndex:z] name];
						
						if ([tagNameForWorkflow isEqualToString:@"state"]) {
							[state appendString:[[workflow childAtIndex:z] stringValue]];// state
						}
						if ([tagNameForWorkflow isEqualToString:@"id"]) {
							[wid appendString:[[workflow childAtIndex:z] stringValue]];// id
						}
						if ([tagNameForWorkflow isEqualToString:@"targetLocale"]) {
							[tl appendString:[[workflow childAtIndex:z] stringValue]];// target locale
						}
						if ([tagNameForWorkflow isEqualToString:@"percentageComplete"]) {
							[complete appendString:[[workflow childAtIndex:z] stringValue]];// complete
						}
						if ([tagNameForWorkflow isEqualToString:@"currentActivity"]) {
							[ca appendString:[[workflow childAtIndex:z] stringValue]];// current activity
						}
						if ([tagNameForWorkflow isEqualToString:@"estimatedTranslateCompletionDate"]) {
							[etcd appendString:[[workflow childAtIndex:z] stringValue]];// estimated translate completion date
						}
						if ([tagNameForWorkflow isEqualToString:@"estimatedCompletionDate"]) {
							[ecd appendString:[[workflow childAtIndex:z] stringValue]];// estimated completion date
						}
						if ([tagNameForWorkflow isEqualToString:@"subLevRepetition"]) {
							[slr appendString:[[workflow childAtIndex:z] stringValue]];// subLevRepetition
						}
						if ([tagNameForWorkflow isEqualToString:@"subLevMatch"]) {
							[slm appendString:[[workflow childAtIndex:z] stringValue]];// subLevMatch
						}
						if ([tagNameForWorkflow isEqualToString:@"isInContextMatch"]) {
							[isInContextMatch appendString:[[workflow childAtIndex:z] stringValue]];// isInContextMatch
						}
						
						if ([tagNameForWorkflow isEqualToString:@"targetWordCount"]) {
							NSArray *targetWordCount = [doc 
								nodesForXPath:@"//jobInfo//workflowInfo//workflow//targetWordCount" error:nil];
							CXMLElement *twc = [targetWordCount objectAtIndex:0];
							[wc appendString:[[twc attributeForName:@"total"] stringValue]];
							
							NSMutableString *ninty = [NSMutableString stringWithCapacity:1];// 95%-99%
							NSMutableString *eighty = [NSMutableString stringWithCapacity:1];// 85%-94%
							NSMutableString *seventy = [NSMutableString stringWithCapacity:1];// 75%-84%
							NSMutableString *nm = [NSMutableString stringWithCapacity:1];// no match
							NSMutableString *repetition = [NSMutableString stringWithCapacity:1];// repetition
							NSMutableString *icm = [NSMutableString stringWithCapacity:1];// In Context Matches
							NSMutableString *cm = [NSMutableString stringWithCapacity:1];// Context Matches
							NSMutableString *nem = [NSMutableString stringWithCapacity:1];// noExactMatch
							NSMutableString *stm = [NSMutableString stringWithCapacity:1];// segmentTmMatch
							
							int zz;
							for (zz = 0; zz < [twc childCount]; zz++) {
								NSString *tagNameForWordCount = [[twc childAtIndex:zz] name];
								
								if ([tagNameForWordCount isEqualToString:@"inContextMatch"]) {
									[icm appendString:[[twc childAtIndex:zz] stringValue]];// In Context Matches
								}
								if ([tagNameForWordCount isEqualToString:@"segmentTmMatch"]) {
									[stm appendString:[[twc childAtIndex:zz] stringValue]];// segmentTmMatch
								}
								if ([tagNameForWordCount isEqualToString:@"noExactMatch"]) {
									[nem appendString:[[twc childAtIndex:zz] stringValue]];// noExactMatch
								}
								if ([tagNameForWordCount isEqualToString:@"medFuzzyMatch"]) {
									[seventy appendString:[[twc childAtIndex:zz] stringValue]];// 75%-84%
								}
								if ([tagNameForWordCount isEqualToString:@"medHiFuzzyMatch"]) {
									[eighty appendString:[[twc childAtIndex:zz] stringValue]];// 85%-94%
								}
								if ([tagNameForWordCount isEqualToString:@"hiFuzzyMatch"]) {
									[ninty appendString:[[twc childAtIndex:zz] stringValue]];// 95%-99%
								}
								if ([tagNameForWordCount isEqualToString:@"repetitionMatch"]) {
									NSString *rmr = [[twc childAtIndex:zz] stringValue];
									NSInteger a = [rmr intValue] + [slr intValue];// repetition + subLevRepetition
									[repetition appendString:[NSString stringWithFormat:@"%d", a]];
								}
								if ([tagNameForWordCount isEqualToString:@"noMatch"]) {
									NSString *nmr = [[twc childAtIndex:zz] stringValue];
									NSInteger a = [nmr intValue] + [slm intValue];// noMatch + subLevMatch
									[nm appendString:[NSString stringWithFormat:@"%d", a]];
								}
								if ([tagNameForWordCount isEqualToString:@"contextMatch"]) {
									[cm appendString:[[twc childAtIndex:zz] stringValue]];// context match
								}
							}
							
							
							if (![state isEqualToString:@"CANCELLED"]) {
								if ([isInContextMatch isEqualToString:@"true"]) {
									NSDictionary *ele3 = [[NSDictionary alloc] initWithObjectsAndKeys:
														  tl, @"tl", stm, @"hundred",
														  ninty, @"ninty", eighty, @"eighty", 
														  seventy, @"seventy", nm, @"nm", 
														  repetition, @"repetition", icm, @"icm",
														  wc, @"total", isInContextMatch, @"isInContextMatch", nil];
									[tmpThirdSectionArray addObject:ele3];
									[ele3 release];
								} else {
									NSInteger tmp = [nem intValue] - [cm intValue];// noUseExactMatch - contextMatch
									
									NSDictionary *ele3 = [[NSDictionary alloc] initWithObjectsAndKeys:
														  tl, @"tl", 
														  [NSString stringWithFormat:@"%d", tmp], @"hundred",
														  ninty, @"ninty", eighty, @"eighty", 
														  seventy, @"seventy", nm, @"nm", 
														  repetition, @"repetition", cm, @"cm",
														  wc, @"total", isInContextMatch, @"isInContextMatch", nil];
									[tmpThirdSectionArray addObject:ele3];
									[ele3 release];
								}
							}
						}
					}
					
					// The cancelled workflow should not be displayed
					if (![state isEqualToString:@"CANCELLED"]) {
						NSDictionary *ele4 = [[NSDictionary alloc] initWithObjectsAndKeys:
											  wid, @"wid", tl, @"tl", wc, @"wc", complete, @"complete", 
											  state, @"state", ca, @"ca", 
											  etcd, @"etcd", ecd, @"ecd", nil];
						[tmpFourSectionArray addObject:ele4];
						[ele4 release];
					}
				}
			}
		}
		
		self.firstSectionData = tmpFirstSectionArray;
		[tmpFirstSectionArray release];
		self.secondSectionData = tmpSecondSectionArray;
		[tmpSecondSectionArray release];
		self.thirdSectionData = tmpThirdSectionArray;
		[tmpThirdSectionArray release];
		self.fourthSectionData = tmpFourSectionArray;
		[tmpFourSectionArray release];
		
		self.table.clearsContextBeforeDrawing = YES; 
		[self.table reloadData];
	} else if ([dispatchNodes count] > 0) {
		[theXMLString release];
		UIAlertView *alert = [[UIAlertView alloc] 
							  initWithTitle:@"" 
							  message:@"Dispatched successfully." 
							  delegate:self 
							  cancelButtonTitle:@"OK" 
							  otherButtonTitles:nil];
		[alert show];
		[alert release];
		
		[self searchJobData];
		[table reloadData];
	} else if ([discardNodes count] > 0) {
		[theXMLString release];
		UIAlertView *alert = [[UIAlertView alloc] 
							  initWithTitle:@"" 
							  message:@"Discard successfully" 
							  delegate:self 
							  cancelButtonTitle:@"OK" 
							  otherButtonTitles:nil];
		[alert show];
		[alert release];
		
		jobStatus = @"CANCELLED";
		[self searchJobData];
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
								  initWithTitle:@"Error" 
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
#pragma mark table view part
- (NSInteger) numberOfSectionsInTableView:(UITableView *)tableView {
	return 1 + [self.secondSectionData count] + [self.thirdSectionData count] + [self.fourthSectionData count];
}

- (NSInteger) tableView:(UITableView *)table numberOfRowsInSection:(NSInteger)section {
	if (section == 0) {
		return [self.firstSectionData count];
	} else if (section > 0 && section < [self.secondSectionData count] + 1) {
		return ROWS_OF_SECOUND_PART_TABLE;
	} else if (section > [self.secondSectionData count]  && 
			   section < [self.thirdSectionData count] + [self.secondSectionData count] + 1) {
		return ROWS_OF_THIRD_PART_TABLE;
	} else {
		return ROWS_OF_FOURTH_PART_TABLE;
	}
}

- (UITableViewCell *) tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
	NSUInteger section = [indexPath section];
	NSUInteger row = [indexPath row];
	
	// the first section, contains job id and job name.
	if (section == 0) {
		static NSString *fid = @"firstSectionId";
		UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:fid];
		if (cell == nil) {
			cell = [[[UITableViewCell alloc] 
					 initWithStyle:UITableViewCellStyleDefault 
					 reuseIdentifier:fid] autorelease];
			
			CGRect jinfo = CGRectMake(12.0f, 5.0f, 80.0f, 35.0f);
			CGRect jvalue = CGRectMake(95.0f, 5.0f, 200.0f, 35.0f);
			
			UILabel *nameLabelOfSectionOne = [[UILabel alloc] initWithFrame:jinfo];// label for jobid
			UILabel *valueLabelOfSectionOne = [[UILabel alloc] initWithFrame:jvalue];// label for jobname
			
			nameLabelOfSectionOne.textAlignment = UITextAlignmentRight;
			nameLabelOfSectionOne.font = [UIFont boldSystemFontOfSize:14];
			nameLabelOfSectionOne.textColor = [UIColor colorWithRed:0 green:0.2 blue:0.5 alpha:1.0];
			nameLabelOfSectionOne.tag = kFirst_name_label;
			[cell.contentView addSubview:nameLabelOfSectionOne];
			
			valueLabelOfSectionOne.font = [UIFont systemFontOfSize:14];
			valueLabelOfSectionOne.lineBreakMode = UILineBreakModeMiddleTruncation;
			valueLabelOfSectionOne.tag = kFirst_value_labe;
			[cell.contentView addSubview:valueLabelOfSectionOne];
			
			[nameLabelOfSectionOne release];
			[valueLabelOfSectionOne release];
		}
		
		UILabel *jNameLabel = (UILabel *)[cell.contentView viewWithTag:kFirst_name_label];
		switch (row) {
			case 0:
				jNameLabel.text = @"Job ID:";
				break;
			case 1:
				jNameLabel.text = @"Job Name:";
				break;
			default:
				break;
		}
		UILabel *jValueLabel = (UILabel *)[cell.contentView viewWithTag:kFirst_value_labe];
		jValueLabel.text = [self.firstSectionData objectAtIndex:row];
		return cell;
	} else if (section > 0 && section < [self.secondSectionData count] + 1) {
		NSString *sid = [NSString stringWithFormat:@"secondId%d", section];
		UITableViewCell *cell2 = [tableView dequeueReusableCellWithIdentifier:sid];
		if (cell2 == nil) {
			cell2 = [[[UITableViewCell alloc] 
					 initWithStyle:UITableViewCellStyleDefault 
					 reuseIdentifier:sid] autorelease];
			
			CGRect psf = CGRectMake(12.0f, 5.0f, 125.0f, 35.0f);
			CGRect swc = CGRectMake(140.0f, 5.0f, 158.0f, 35.0f);
			
			UILabel *nameLabelOfSectionTwo = [[UILabel alloc] initWithFrame:psf];
			nameLabelOfSectionTwo.textAlignment = UITextAlignmentRight;
			nameLabelOfSectionTwo.adjustsFontSizeToFitWidth = YES;
			nameLabelOfSectionTwo.tag = kSecond_name_label;
			nameLabelOfSectionTwo.font = [UIFont boldSystemFontOfSize:14];
			nameLabelOfSectionTwo.textColor = [UIColor colorWithRed:0 green:0.2 blue:0.5 alpha:1.0];
			
			UILabel *valueLabelOfSectionTwo = [[UILabel alloc] initWithFrame:swc];
			valueLabelOfSectionTwo.font = [UIFont systemFontOfSize:13];
			valueLabelOfSectionTwo.tag = kSecond_value_label;
			valueLabelOfSectionTwo.lineBreakMode = UILineBreakModeMiddleTruncation;
			//valueLabelOfSectionTwo.adjustsFontSizeToFitWidth = YES;
			
			[cell2.contentView addSubview:nameLabelOfSectionTwo];
			[cell2.contentView addSubview:valueLabelOfSectionTwo];
			[nameLabelOfSectionTwo release];
			[valueLabelOfSectionTwo release];
		}
		
		NSDictionary *rowData2 = [self.secondSectionData objectAtIndex:(section - 1)];
		
		UILabel *jNameLabelTwo = (UILabel *)[cell2.contentView viewWithTag:kSecond_name_label];
		UILabel *jValueLabelTwo = (UILabel *)[cell2.contentView viewWithTag:kSecond_value_label];
		switch (row) {
			case 0:
				jNameLabelTwo.text = @"Source File Name:";
				jValueLabelTwo.text = [rowData2 objectForKey:@"nameText"];
				break;
			case 1:
				jNameLabelTwo.text = @"File Word Count:";
				jValueLabelTwo.text = [rowData2 objectForKey:@"valueText"];
				break;
			default:
				break;
		}
		return cell2;
	} else if (section > [self.secondSectionData count]  && 
			   section < [self.thirdSectionData count] + [self.secondSectionData count] + 1) {
		NSString *tid = [NSString stringWithFormat:@"thirdId%d", section];
		
		UITableViewCell *cell3 = [tableView dequeueReusableCellWithIdentifier:tid];
		if (cell3 == nil) {
			cell3 = [[[UITableViewCell alloc] 
					  initWithStyle:UITableViewCellStyleDefault 
					  reuseIdentifier:tid] autorelease];
			
			CGRect oneRect = CGRectMake(12.0f, 5.0f, 100.0f, 35.0f);
			CGRect twoRect = CGRectMake(115.0f, 5.0f, 185.0f, 35.0f);
			
			UILabel *nameLabelOfSectionThree = [[UILabel alloc] initWithFrame:oneRect];
			nameLabelOfSectionThree.textAlignment = UITextAlignmentRight;
			nameLabelOfSectionThree.adjustsFontSizeToFitWidth = YES;
			nameLabelOfSectionThree.font = [UIFont boldSystemFontOfSize:14];
			nameLabelOfSectionThree.textColor = [UIColor colorWithRed:0 green:0.2 blue:0.5 alpha:1.0];
			nameLabelOfSectionThree.lineBreakMode = UILineBreakModeWordWrap;
			nameLabelOfSectionThree.tag = kThird_name_label;
			nameLabelOfSectionThree.numberOfLines = 0;
			
			UILabel *valueLabelOfSectionThree = [[UILabel alloc] initWithFrame:twoRect];
			valueLabelOfSectionThree.font = [UIFont systemFontOfSize:13];
			valueLabelOfSectionThree.adjustsFontSizeToFitWidth = YES;
			valueLabelOfSectionThree.tag = kThird_value_label;
			
			[cell3.contentView addSubview:nameLabelOfSectionThree];
			[cell3.contentView addSubview:valueLabelOfSectionThree];
			[nameLabelOfSectionThree release];
			[valueLabelOfSectionThree release];
		}
		NSDictionary *rowData3 = [self.thirdSectionData 
								  objectAtIndex:(section - [self.secondSectionData count] - 1)];
		
		UILabel *jNameLabelThree = (UILabel *)[cell3.contentView viewWithTag:kThird_name_label];
		UILabel *jValueLabelThree = (UILabel *)[cell3.contentView viewWithTag:kThird_value_label];
		
		switch (row) {
			case 0:
				jNameLabelThree.text = @"Target Locale:";
				jValueLabelThree.text = [rowData3 objectForKey:@"tl"];
				break;
			case 1:
				jNameLabelThree.text = @"100%:";
				jValueLabelThree.text = [rowData3 objectForKey:@"hundred"];
				break;
			case 2:
				jNameLabelThree.text = @"95%-99%:";
				jValueLabelThree.text = [rowData3 objectForKey:@"ninty"];
				break;
			case 3:
				jNameLabelThree.text = @"85%-94%:";
				jValueLabelThree.text = [rowData3 objectForKey:@"eighty"];
				break;
			case 4:
				jNameLabelThree.text = @"75%-84%:";
				jValueLabelThree.text = [rowData3 objectForKey:@"seventy"];
				break;
			case 5:
				jNameLabelThree.text = @"No Match:";
				jValueLabelThree.text = [rowData3 objectForKey:@"nm"];
				break;
			case 6:
				jNameLabelThree.text = @"Repetitions:";
				jValueLabelThree.text = [rowData3 objectForKey:@"repetition"];
				break;
			case 7:
				if ([[rowData3 objectForKey:@"isInContextMatch"] isEqualToString:@"true"]) {
					jNameLabelThree.text = @"In Context Matches:";
					jValueLabelThree.text = [rowData3 objectForKey:@"icm"];
				} else {
					jNameLabelThree.text = @"Context Matches:";
					jValueLabelThree.text = [rowData3 objectForKey:@"cm"];
				}
				break;
			default:
				jNameLabelThree.text = @"Total:";
				jValueLabelThree.text = [rowData3 objectForKey:@"total"];
				break;
		}
		
		return cell3;
	} else {
		NSString *foid = [NSString stringWithFormat:@"fourthId%d", section];
		UITableViewCell *cell4 = [tableView dequeueReusableCellWithIdentifier:foid];
		if (cell4 == nil) {
			cell4 = [[[UITableViewCell alloc] 
					 initWithStyle:UITableViewCellStyleDefault 
					 reuseIdentifier:foid] autorelease];
			
			CGRect labelRect = CGRectMake(12.0f, 5.0f, 125.0f, 35.0f);
			CGRect valueRect = CGRectMake(140.0f, 5.0f, 160.0f, 35.0f);
			
			UILabel *nameLabelOfSectionFour = [[UILabel alloc] initWithFrame:labelRect];
			nameLabelOfSectionFour.textAlignment = UITextAlignmentRight;
			nameLabelOfSectionFour.adjustsFontSizeToFitWidth = YES;
			nameLabelOfSectionFour.font = [UIFont boldSystemFontOfSize:14];
			nameLabelOfSectionFour.lineBreakMode = UILineBreakModeWordWrap;
			nameLabelOfSectionFour.textColor = [UIColor colorWithRed:0 green:0.2 blue:0.5 alpha:1.0];
			nameLabelOfSectionFour.numberOfLines = 0;
			nameLabelOfSectionFour.tag = kFour_name_label;
			
			UILabel *valueLabeOfSectionFour = [[UILabel alloc] initWithFrame:valueRect];
			valueLabeOfSectionFour.font = [UIFont systemFontOfSize:12];
			valueLabeOfSectionFour.adjustsFontSizeToFitWidth = YES;
			valueLabeOfSectionFour.tag = kFour_value_label;
//			valueLabeOfSectionFour.frame = CGRectMake(145.0f, 5.0f, 160.0f, 35.0f);
//			valueLabeOfSectionFour.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleLeftMargin;
//			valueLabeOfSectionFour.backgroundColor = [UIColor grayColor];
//			valueLabeOfSectionFour.clearsContextBeforeDrawing = YES;
//			valueLabeOfSectionFour.contentMode = UIViewContentModeScaleToFill;
		
			[cell4.contentView addSubview:nameLabelOfSectionFour];
			[cell4.contentView addSubview:valueLabeOfSectionFour];
			[nameLabelOfSectionFour release];
			[valueLabeOfSectionFour release];
		}
		
		NSDictionary *rowData4 = [self.fourthSectionData objectAtIndex:
								  (section - [self.thirdSectionData count] - 
								   [self.secondSectionData count] - 1)];
		UILabel *jNameLabelFour = (UILabel *)[cell4.contentView viewWithTag:kFour_name_label];
		UILabel *jValueLabelFour = (UILabel *)[cell4.contentView viewWithTag:kFour_value_label];
		
		switch (row) {
			case 0:
				jNameLabelFour.text = @"Target Locale:";
				jValueLabelFour.text = [rowData4 objectForKey:@"tl"];
				cell4.accessoryView = nil;
				break;
			case 1:
				jNameLabelFour.text = @"Word Count:";
				jValueLabelFour.text = [rowData4 objectForKey:@"wc"];
				cell4.accessoryView = nil;
				break;
			case 2:
				jNameLabelFour.text = @"Complete%:";
				jValueLabelFour.text = [NSString stringWithFormat:@"%@%%", 
											   [rowData4 objectForKey:@"complete"]];
				cell4.accessoryView = nil;
				break;
			case 3:
				jNameLabelFour.text = @"State:";
				
				if ([[rowData4 objectForKey:@"state"] hasPrefix:@"READY"]) {
					UIButton *dispathButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
					dispathButton.frame = CGRectMake(0.0, 0.0, 150.0, 30.0);
					dispathButton.titleLabel.font = [UIFont boldSystemFontOfSize:12];
					UIImage *buttonUpImage = [UIImage imageNamed:@"image165.png"];
					[dispathButton setBackgroundImage:buttonUpImage forState:UIControlStateNormal];
					[dispathButton setTitle:READY_TO_DISPATH forState:UIControlStateNormal];
					
					NSInteger workflowId = [[rowData4 objectForKey:@"wid"] intValue];
					[dispathButton setTag:workflowId];// this tag is used to transfer workflow id to the dispath function
					
					[dispathButton addTarget:self action:@selector(dispathWorkflow:) 
							forControlEvents:UIControlEventTouchUpInside];
					
					cell4.accessoryView = dispathButton;
					jValueLabelFour.adjustsFontSizeToFitWidth = YES;
					jValueLabelFour.text = nil;
				} else {
					cell4.accessoryView = nil;
					jValueLabelFour.text = [rowData4 objectForKey:@"state"];
				}
				break;
			case 4:
				jNameLabelFour.text = @"Current Activity:";
				jValueLabelFour.text = [rowData4 objectForKey:@"ca"];
				cell4.accessoryView = nil;
				break;
			case 5:
				jNameLabelFour.text = @"Estimated Translate Completion Date:";
				jNameLabelFour.font = [UIFont boldSystemFontOfSize:10];
				jValueLabelFour.text = [rowData4 objectForKey:@"etcd"];
				cell4.accessoryView = nil;
				break;
			default:
				jNameLabelFour.text = @"Estimated Completion Date:";
				jNameLabelFour.font = [UIFont boldSystemFontOfSize:10];
				jValueLabelFour.text = [rowData4 objectForKey:@"ecd"];
				cell4.accessoryView = nil;
				break;
		}
		
		return cell4;
	}
}

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	[tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (NSIndexPath *) tableView:(UITableView *)tableView willSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	return nil;
}

- (NSString *) tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
	if (section == 1) {
		return @"Primary Source File";
	} else if (section == [self.secondSectionData count] + 1) {
		return @"Detailed Word Counts";
	} else if (section == [self.thirdSectionData count] + [self.secondSectionData count] + 1) {
		return @"Workflows";
	} else {
		return nil;
	}
}

@end
