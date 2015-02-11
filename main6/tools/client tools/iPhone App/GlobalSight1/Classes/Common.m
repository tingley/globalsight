//
//  Common.m
//  GlobalSight1
//
//  Created by apple on 20/01/11.
//  Copyright 2011 Welocalize. All rights reserved.
//

#import "Common.h"


@implementation Common

// Update the last update time on the toolbar
- (void) updateTime {
	UIBarButtonItem *textField = (UIBarButtonItem *)
	[self.navigationController.toolbar.items objectAtIndex:1];
	UITextField *text = (UITextField *) textField.customView;
	NSDate *now = [NSDate date];
	NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
	[formatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
	NSString *time = [formatter stringFromDate:now];
	text.text = [NSString stringWithFormat:@"Last Update: %@", time];
	[formatter release];
}

-(UITableViewCell *) tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return nil;
}

- (NSInteger) tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return 0;
}

@end
