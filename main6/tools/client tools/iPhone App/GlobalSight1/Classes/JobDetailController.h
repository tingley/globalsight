//
//  JobDetailController.h
//  GlobalSight1
//
//  Created by System Administrator on 9/21/10.
//  Copyright 2010 Welocalize. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Common.h"

#define kFirst_name_label 1
#define kFirst_value_labe 2
#define kSecond_name_label 3
#define kSecond_value_label 4
#define kThird_name_label 5
#define kThird_value_label 6
#define kFour_name_label 7
#define kFour_value_label 8

@interface JobDetailController : Common {
	NSString *jobId;
	NSString *jobStatus;
	
	UIActivityIndicatorView *indicator;
	UITableView *table;
	NSArray *firstSectionData;
	NSArray *secondSectionData;
	NSArray *thirdSectionData;
	NSArray *fourthSectionData;
	
	NSArray *workflowData;
	NSMutableData *webData;
}

@property (nonatomic, retain) NSString *jobId;
@property (nonatomic, retain) NSString *jobStatus;
@property (nonatomic, retain) IBOutlet UIActivityIndicatorView *indicator;
@property (nonatomic, retain) IBOutlet UITableView *table;
@property (nonatomic, retain) NSArray *firstSectionData;
@property (nonatomic, retain) NSArray *secondSectionData;
@property (nonatomic, retain) NSArray *thirdSectionData;
@property (nonatomic, retain) NSArray *fourthSectionData;
@property (nonatomic, retain) NSArray *workflowData;

@property (nonatomic, retain) NSMutableData *webData;

@end
