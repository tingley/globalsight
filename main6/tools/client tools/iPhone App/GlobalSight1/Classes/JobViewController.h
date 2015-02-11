//
//  JobViewController.h
//  GlobalSight1
//
//  Created by System Administrator on 9/21/10.
//  Copyright 2010 Welocalize. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Common.h"

#define kJobIdTag 1
#define kJobNameTag 2
#define kJobStatusTag 3

@class JobDetailController;

@interface JobViewController : Common {
	JobDetailController *detailController;
	
	NSArray *listData;
	UITableView *table;
	
	UIActivityIndicatorView *indicator;
	
	NSMutableData *webData;
	
	NSString *jobStatus;
	NSString *jobDisplayStatus;
	NSString *totalNo;
	int currentPage;
	
	NSMutableArray *selectedData;
}

@property (nonatomic, retain) IBOutlet UIActivityIndicatorView *indicator;
@property (nonatomic, retain) IBOutlet UITableView *table;

@property (nonatomic, retain) NSArray *listData;
@property (nonatomic, retain) NSString *jobStatus;
@property (nonatomic, retain) NSString *jobDisplayStatus;
@property (nonatomic, retain) NSString *totalNo;

@property (nonatomic, retain) NSMutableData *webData;
@property (nonatomic, retain) NSMutableArray *selectedData;

@end
