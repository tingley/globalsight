//
//  LoginViewController.h
//  GlobalSight1
//
//  Created by System Administrator on 9/21/10.
//  Copyright 2010 Welocalize. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <sqlite3.h>

@interface LoginViewController : UIViewController {
	UITextField *port;
	UITextField *username;
	UITextField *password;
	UITextField *address;
	UIActivityIndicatorView *indicator;
	UISwitch *https;
	
	NSMutableData *webData;
	
	UIImageView *logo;
}

@property (nonatomic, retain) IBOutlet UITextField *username;
@property (nonatomic, retain) IBOutlet UITextField *password;
@property (nonatomic, retain) IBOutlet UITextField *address;
@property (nonatomic, retain) IBOutlet UITextField *port;
@property (nonatomic, retain) IBOutlet UIActivityIndicatorView *indicator;
@property (nonatomic, retain) IBOutlet UISwitch *https;
@property (nonatomic, retain) IBOutlet UIImageView *logo;

@property (nonatomic, retain) NSMutableData *webData;

- (IBAction) addressDoneEditing;
- (IBAction) portDoneEditing;
- (IBAction) usernameDoneEditing;
- (IBAction) passwordDoneEditing;
- (IBAction) backgroundTap;
- (IBAction) loginPressed;
- (IBAction) startInput;

@end
