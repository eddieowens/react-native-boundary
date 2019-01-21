
#import "RNBoundary.h"

@implementation RNBoundary
{
  bool hasListeners;
}

RCT_EXPORT_MODULE()

-(instancetype)init
{
    self = [super init];
    if (self) {
        self.locationManager = [[CLLocationManager alloc] init];
        self.locationManager.delegate = self;
    }

    return self;
}

RCT_EXPORT_METHOD(add:(NSDictionary*)boundary addWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    CLAuthorizationStatus status = [CLLocationManager locationServicesEnabled];

    if (!status) {
        [self.locationManager requestAlwaysAuthorization];
    }

    if ([CLLocationManager authorizationStatus] == kCLAuthorizationStatusAuthorizedAlways) {
        NSString *id = boundary[@"id"];
        CLLocationCoordinate2D center = CLLocationCoordinate2DMake([boundary[@"lat"] doubleValue], [boundary[@"lng"] doubleValue]);
        CLRegion *boundaryRegion = [[CLCircularRegion alloc]initWithCenter:center
                                                                    radius:[boundary[@"radius"] doubleValue]
                                                                identifier:id];

        [self.locationManager startMonitoringForRegion:boundaryRegion];

        resolve(id);
    } else {
        reject(@"PERM", @"Access fine location is not permitted", [NSError errorWithDomain:@"boundary" code:200 userInfo:@{@"Error reason": @"Invalid permissions"}]);
    }
}

RCT_EXPORT_METHOD(remove:(NSString *)boundaryId removeWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if ([self removeBoundary:boundaryId]) {
        resolve(boundaryId);
    } else {
        reject(@"@no_boundary", @"No boundary with the provided id was found", [NSError errorWithDomain:@"boundary" code:200 userInfo:@{@"Error reason": @"Invalid boundary ID"}]);
    }
}

RCT_EXPORT_METHOD(removeAll:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    @try {
        [self removeAllBoundaries];
    }
    @catch (NSException *ex) {
        reject(@"failed_remove_all", @"Failed to remove all boundaries", ex);
    }
    resolve(NULL);
}

// Will be called when this module's first listener is added.
-(void)startObserving {
    hasListeners = YES;
}

// Will be called when this module's last listener is removed, or on dealloc.
-(void)stopObserving {
    hasListeners = NO;
}

- (void) removeAllBoundaries
{
    for(CLRegion *region in [self.locationManager monitoredRegions]) {
        [self.locationManager stopMonitoringForRegion:region];
    }
}

- (bool) removeBoundary:(NSString *)boundaryId
{
    for(CLRegion *region in [self.locationManager monitoredRegions]){
        if ([region.identifier isEqualToString:boundaryId]) {
            [self.locationManager stopMonitoringForRegion:region];
            return true;
        }
    }
    return false;
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"onEnter", @"onExit"];
}

- (void)sendEventAfter:(NSString *)event withBody:(NSString *)body andRetry:(int)retry
{
  if (retry < 5) {
    __weak typeof(self) weakSelf = self;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 2 * NSEC_PER_SEC), dispatch_get_current_queue(), ^{
      typeof(self) strongSelf = weakSelf;
      if (strongSelf) {
        if (strongSelf->hasListeners) {
          [strongSelf sendEventWithName:event body:body];
        } else {
          [strongSelf sendEventAfter:event withBody:body andRetry:(retry + 1)];
        }
      }
    });
  }
}


- (void)locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region
{
  NSLog(@"didEnter : %@", region);
  UIApplicationState state = [[UIApplication sharedApplication] applicationState];
  // if state is not active
  if (!hasListeners) {
    if (state != UIApplicationStateActive) {
      [self sendEventAfter:@"onEnter" withBody:region.identifier andRetry:0];
    }
  } else {
    [self sendEventWithName:@"onEnter" body:region.identifier];
  }
}

- (void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region
{
    NSLog(@"didExit : %@", region);
    UIApplicationState state = [[UIApplication sharedApplication] applicationState];
    // if state is not active
    if (!hasListeners) {
      if (state != UIApplicationStateActive) {
        [self sendEventAfter:@"onExit" withBody:region.identifier andRetry:0];
      }
    } else {
      [self sendEventWithName:@"onExit" body:region.identifier];
    }
}

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

@end
