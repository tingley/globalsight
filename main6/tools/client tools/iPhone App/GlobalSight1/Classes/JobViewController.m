//
//  JobViewController.m
//  GlobalSight1
//
//  Created by System Administrator on 9/21/10.
//  Copyright 2010 Welocalize. All rights reserved.
//

#import "JobViewController.h"
#import "JobDetailController.h"
#import "GlobalSight1AppDelegate.h"
#import "TouchXML.h"

#define page_size 10

@implementation JobViewController

@synthesize listData;
@synthesize indicator;
@synthesize table;
@synthesize webData;
@synthesize jobStatus;
@synthesize jobDisplayStatus;
@synthesize selectedData;
@synthesize totalNo;

BOOL searchingData;

- (void) viewWillAppear:(BOOL)animated {
	[self searchData];
	selectedData = [[NSMutableArray alloc] initWithCapacity:0];
	[super viewWillAppear:animated];
}

- (void) viewDidLoad {
	currentPage = 1;
	
	self.navigationController.navigationBarHidden = NO;
	self.navigationItem.title = @"My Jobs";
	// init the discard button on the top right corner
	if ([self showDiscardButton]) {
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

	//make toolbar show
	[self.navigationController setToolbarHidden:NO animated:NO];
	self.navigationController.toolbar.barStyle = UIBarStyleDefault;
	// init the refresh button on the left bottom corner
	UIImage *refreshImage = [UIImage imageNamed:@"refresh.png"];
	UIButton *customBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 19, 24)];
	[customBtn addTarget:self action:@selector(refresh:) forControlEvents:UIControlEventTouchUpInside];
	[customBtn setBackgroundImage:refreshImage forState:UIControlStateNormal];
	UIBarButtonItem *refreshButton = [[UIBarButtonItem alloc] initWithCustomView:customBtn];
	[customBtn release];
	// init the text field on toolbar and set current time
	UITextField *text = [[UITextField alloc] initWithFrame:CGRectMake(0, 10, 250, 24)];
	text.font = [UIFont italicSystemFontOfSize:12];
	text.textAlignment = UITextAlignmentCenter;
//	NSDate *now = [[NSDate alloc] init];
//	text.text = [NSString stringWithFormat:@"Last Update: %@", 
//				 [[now description] substringWithRange:NSMakeRange(0, 19)]];
//	[now release];
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

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
	NSLog(@"there is memory warning in job list view");
    [super didReceiveMemoryWarning];
}

- (void)viewDidUnload {
	self.listData = nil;
	self.selectedData = nil;
	[detailController release];
	detailController = nil;
}

- (void)dealloc {
	[listData release];
	[table release];
	[detailController release];
	[indicator release];
	[webData release];
	[jobStatus release];
	[jobDisplayStatus release];
	[selectedData release];
	[totalNo release];
	[super dealloc];
}

- (IBAction) refresh:(id)sender
{
	[self searchData];
}

- (void) discard:(id) sender
{
	if ([selectedData count] < 1) {
		UIAlertView *alert = [[UIAlertView alloc] 
							  initWithTitle:@"Warning" 
							  message:@"Please select at least one job."
							  delegate:self 
							  cancelButtonTitle:nil 
							  otherButtonTitles:@"OK", nil];
		[alert show];
		[alert release];
	} else {
		NSString *msg = @"This will permanently remove the selected "
		"jobs and all associated workflows from the system.\n "
		"Note: There may be a short delay when jobs are being discarded.";
		UIAlertView *alert = [[UIAlertView alloc] 
							  initWithTitle:@"Warning" 
							  message:msg
							  delegate:self 
							  cancelButtonTitle:@"No" 
							  otherButtonTitles:@"Yes", nil];
		[alert show];
		[alert release];
	}
}

- (IBAction) logout:(id)sender
{
	[self.navigationController popToRootViewControllerAnimated:YES];
}

- (BOOL) showDiscardButton
{
	if ([jobDisplayStatus isEqualToString:@"Pending"] || 
		[jobDisplayStatus isEqualToString:@"Ready"] || 
		[jobDisplayStatus isEqualToString:@"Progress"]) {
		return YES;
	} else {
		return NO;
	}

}

#pragma mark -
#pragma mark alertView
- (void) alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
	if (buttonIndex == 1) {
		[self discardJobs];
	}
}

#pragma mark -
#pragma mark websrvice part
- (void) searchData
{
	if (searchingData == NO) {
		searchingData = YES;
		[indicator startAnimating];
		
		NSString *soapMsg = [NSString stringWithFormat:
							WEBSERVICE_HEADER
							"<fetchJobsByState xmls=\"http://www.globalsight.com/webservices/\">\n"
							"<p_accessToken>%@</p_accessToken>"
							"<p_state>%@</p_state>"
							"<p_offset>%d</p_offset>"
							"<p_count>%d</p_count>"
							"<p_isDescOrder>true</p_isDescOrder>"
							"</fetchJobsByState>\n"
							WEBSERVICE_TAIL, [GlobalSight1AppDelegate app].token, jobStatus, 1, currentPage * page_size
							];
		NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[GlobalSight1AppDelegate app].url];
		NSString *msgLength = [NSString stringWithFormat:@"%d", [soapMsg length]];
		
		[request addValue: @"text/xml; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
		[request addValue: @"http://www.globalsight.com/webservices/fetchJobsPerCompany" forHTTPHeaderField:@"SOAPAction"];
		
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

- (void) discardJobs
{
	if (searchingData == NO) {
		searchingData = YES;
		[indicator startAnimating];
		
		// this string element will contain all selected jobids
		NSMutableString *selectedJobId = [NSMutableString stringWithCapacity:0];
		int i;
		for (i = 0; i < [selectedData count]; i++) {
			// transform the arry to a string splited by ,
			[selectedJobId appendFormat:@"%@,", [selectedData objectAtIndex:i]];
		}
		[selectedJobId deleteCharactersInRange:NSMakeRange([selectedJobId length] - 1, 1)];
		
		NSString *soapMsg = [NSString stringWithFormat:
							 WEBSERVICE_HEADER
							 "<cancelJobs xmls=\"http://www.globalsight.com/webservices/\">\n"
							 "<p_accessToken>%@</p_accessToken>"
							 "<p_jobIds>%@</p_jobIds>"
							 "</cancelJobs>\n"
							 WEBSERVICE_TAIL, [GlobalSight1AppDelegate app].token, selectedJobId
							 ];
		NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[GlobalSight1AppDelegate app].url];
		NSString *msgLength = [NSString stringWithFormat:@"%d", [soapMsg length]];
		
		[request addValue: @"text/xml; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
		[request addValue: @"http://www.globalsight.com/webservices/cancelJobs" forHTTPHeaderField:@"SOAPAction"];
		
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
- (void) connectionDidFinishLoading:(NSURLConnection *) connection
{
	[self updateTime];
	
	[connection release];
	[indicator stopAnimating];
	searchingData = NO;
	NSString *theXMLString = [[NSString alloc] 
						initWithBytes:[webData mutableBytes] 
						length:[webData length] 
						encoding:NSUTF8StringEncoding];
	//NSLog(@"theXMLString - job list: %@", theXMLString);
	CXMLDocument *doc = [[[CXMLDocument alloc] initWithXMLString:theXMLString options:0 error:nil] autorelease];
	NSArray *nodes = [doc nodesForXPath:@"//fetchJobsByStateReturn" error:nil];
	NSArray *cancelJobNodes = [doc nodesForXPath:@"//cancelJobsReturn" error:nil];
	
	listData = nil;// make sure the listdata is empty before setting new data
	[webData release];
	
	if ([nodes count] > 0) {
		[theXMLString release];
		CXMLElement *node = [nodes objectAtIndex:0];
		NSString *returnValue = [[node childAtIndex:0] stringValue];
		//NSLog(@"%@", returnValue);
		
		if (returnValue == nil) {
			[table reloadData];
			return;
		} 

		doc = [[[CXMLDocument alloc] 
				initWithXMLString:returnValue 
				options:0 error:nil] autorelease];
		
		NSArray *jobInfo = [doc nodesForXPath:@"//Jobs//Job" error:nil];
		
		NSMutableArray *tmpArray = [[NSMutableArray alloc] initWithCapacity:1];
		
		int i;
		for (i = 0; i < [jobInfo count]; i++) {
			CXMLElement *job = [jobInfo objectAtIndex:i];
			
			NSMutableString *jId = [NSMutableString stringWithCapacity:1];
			NSMutableString *jName = [NSMutableString stringWithCapacity:1];
			NSMutableString *jDispalyState = [NSMutableString stringWithCapacity:1];
			NSMutableString *jState = [NSMutableString stringWithCapacity:1];
			
			int j;
			for (j = 0; j < [job childCount]; j++) {
				NSString *tagName = [[job childAtIndex:j] name];
				
				if ([tagName isEqualToString:@"id"]) {
					[jId appendString:[[job childAtIndex:j] stringValue]];
				}
				if ([tagName isEqualToString:@"name"]) {
					[jName appendString:[[job childAtIndex:j] stringValue]];
				}
				if ([tagName isEqualToString:@"displayState"]) {
					[jDispalyState appendString:[[job childAtIndex:j] stringValue]];
				}
				if ([tagName isEqualToString:@"state"]) {
					[jState appendString:[[job childAtIndex:j] stringValue]];
				}
			}
			
			NSDictionary *ele = [[NSDictionary alloc] initWithObjectsAndKeys:
								jId, @"jobId", jName, @"jobName", 
								jDispalyState, @"displayState", jState, @"state",
								nil];
			[tmpArray addObject:ele];
			[ele release];
		}

		self.listData = tmpArray;
		[tmpArray release];
		[table reloadData];
	} else if ([cancelJobNodes count] > 0){
		[theXMLString release];
		
		UIAlertView *alert = [[UIAlertView alloc] 
							  initWithTitle:@"" 
							  message:@"Discard successfully" 
							  delegate:self 
							  cancelButtonTitle:@"OK" 
							  otherButtonTitles:nil];
		[alert show];
		[alert release];
		
		// here, we need to refresh the total number.
		self.totalNo = [NSString stringWithFormat:@"%d", [totalNo intValue] - [selectedData count]];
		[selectedData removeAllObjects];// be sure to empty the array
		
		// reload data and refresh screen
		[self searchData];
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
#pragma mark Table View Part
- (NSInteger) tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return [self.listData count] + 1;
}

- (UITableViewCell *) tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	static NSString *cellid = @"jobCellIdifier";
	UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellid];
	
	if (cell == nil) {
		cell = [[[UITableViewCell alloc] 
				 initWithStyle:UITableViewCellStyleSubtitle 
				 reuseIdentifier:cellid] autorelease];
	}
	
	NSUInteger row = [indexPath row];
	
	if (row < [listData count]) {
		NSDictionary *rowData = [listData objectAtIndex:row];
	
		cell.textLabel.text = [rowData objectForKey:@"jobName"];
		cell.textLabel.font = [UIFont systemFontOfSize:15];
		cell.textLabel.lineBreakMode = UILineBreakModeMiddleTruncation;
		
		NSString *thejobId = [rowData objectForKey:@"jobId"];
		cell.detailTextLabel.text = [NSString stringWithFormat:@"ID: %@.   Status: %@", 
									 thejobId, 
									 [rowData objectForKey:@"displayState"]];
		cell.detailTextLabel.font = [UIFont italicSystemFontOfSize:12];
		cell.detailTextLabel.textAlignment = UITextAlignmentCenter;
		
		cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
		
		if ([[rowData objectForKey:@"displayState"] isEqualToString:@"Pending"] && 
			![[rowData objectForKey:@"state"] isEqualToString:@"BATCH_RESERVED"]) {
			cell.textLabel.textColor = [UIColor redColor];
			cell.detailTextLabel.textColor = [UIColor redColor];
		} else {
			cell.textLabel.textColor = [UIColor blackColor];
			cell.detailTextLabel.textColor = [UIColor blackColor];
		}
		
		if ([self showDiscardButton]) {
			// set cell image according to the selection
			if ([selectedData containsObject:thejobId]) {
				UIImage *image = [UIImage imageNamed:@"selected.png"];
				cell.imageView.image = image;
			} else {
				UIImage *image1 = [UIImage imageNamed:@"unselected.png"];
				cell.imageView.image = image1;
			}
		} else {
			cell.imageView.image = nil;
		}
	} else {
		cell.detailTextLabel.text = nil;
		cell.imageView.image = nil;
		cell.imageView.highlightedImage = nil;
		
		// content of the last row
		cell.textLabel.font = [UIFont italicSystemFontOfSize:13];
		cell.textLabel.textAlignment = UITextAlignmentCenter;
		
		if (searchingData) {
			cell.textLabel.text = @"Loading data...";
		} else {
			cell.accessoryType = UITableViewCellAccessoryNone;
			if ([listData count] < currentPage * page_size || [listData count] == [totalNo intValue]) {
				cell.textLabel.text = [NSString stringWithFormat:@"Total %d rows.", [listData count]];
			} else {
				cell.textLabel.text = [NSString stringWithFormat:@"Show next %d rows. (Current: %d, Total: %d)", 
									   page_size, [self.listData count], [totalNo intValue]];
			}
		}
	}
	
	return cell;
}

- (void) tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	[tableView deselectRowAtIndexPath:indexPath animated:YES];
	NSUInteger row = [indexPath row];
	
	if (row == [listData count]) {
		currentPage += 1;
		[self searchData];
	} else {
		if ([self showDiscardButton]) {
			UITableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
			UIImage *image1 = [UIImage imageNamed:@"selected.png"];
			UIImage *image2 = [UIImage imageNamed:@"unselected.png"];
			
			NSDictionary *rowData = [listData objectAtIndex:row];
			NSString *status = [rowData objectForKey:@"displayState"];
			NSString *jobId = [rowData objectForKey:@"jobId"];
			
			if (cell.imageView.image == image1) {
				cell.imageView.image = image2;
				
				[selectedData removeObject:jobId];// when unselected, remove id from the array
				cell.backgroundColor = [UIColor whiteColor];
			} else {
				cell.imageView.image = image1;
				[selectedData addObject:jobId];// when selected, save id in the array
				cell.backgroundColor = [UIColor colorWithRed:0 green:0.5 blue:0.9 alpha:0.5];
			}
		}
	}
}

- (void) tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath
{
	cell.detailTextLabel.backgroundColor = [UIColor clearColor];
	cell.textLabel.backgroundColor = [UIColor clearColor];
	
	UIImage *image1 = [UIImage imageNamed:@"selected.png"];
	UIImage *image2 = [UIImage imageNamed:@"unselected.png"];
	
	if (cell.imageView.image == image2) {
		cell.backgroundColor = [UIColor whiteColor];
	} else if (cell.imageView.image == image1) {
		cell.backgroundColor = [UIColor colorWithRed:0 green:0.5 blue:0.9 alpha:0.5];
	}
}

// when searching data, disable the table click function.
- (NSIndexPath *) tableView:(UITableView *)tableView willSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	if (searchingData) {
		return nil;
	} else {
		return indexPath;
	}
}

- (void) tableView:(UITableView *)tableView accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	if (detailController == nil) {
		detailController = [[JobDetailController alloc] initWithNibName:@"JobDetailController" bundle:nil];
	}	
	NSDictionary *rowData = [listData objectAtIndex:row];
	detailController.jobId = [rowData objectForKey:@"jobId"];
	detailController.jobStatus = [rowData objectForKey:@"displayState"];
	[self.navigationController pushViewController:detailController animated:YES];
}

@end
