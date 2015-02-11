//
//  StatusNavigation.h
//  GlobalSight1
//
//  Created by Administrator on 1/10/11.
//  Copyright 2011 Welocalize. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Common.h"

@interface StatusNavigation : Common {
	UIActivityIndicatorView *indicator;
	
	NSMutableData *webData;
	
	UITableView *table;
	NSMutableDictionary *statusData;
}

@property (nonatomic, retain) IBOutlet UIActivityIndicatorView *indicator;
@property (nonatomic, retain) NSMutableData *webData;
@property (nonatomic, retain) IBOutlet UITableView *table;
@property (nonatomic, retain) NSMutableDictionary *statusData;

@end
