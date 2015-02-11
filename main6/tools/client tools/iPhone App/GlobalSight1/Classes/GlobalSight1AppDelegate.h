//
//  GlobalSight1AppDelegate.h
//  GlobalSight1
//
//  Created by System Administrator on 9/20/10.
//  Copyright Welocalize 2010. All rights reserved.
//

#import <UIKit/UIKit.h>

#define WEBSERVICE_HEADER @"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n<soap:Body>\n"
#define WEBSERVICE_TAIL @"</soap:Body>\n</soap:Envelope>\n"

@interface GlobalSight1AppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
	UINavigationController *navController;
	
	NSString *token;
	NSString *userId;
	NSURL *url;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet UINavigationController *navController;

@property (nonatomic, retain) NSString *token;
@property (nonatomic, retain) NSString *userId;
@property (nonatomic, retain) NSURL *url;

+ (GlobalSight1AppDelegate *) app;

@end

