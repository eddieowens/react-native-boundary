
#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#else
#import "RCTBridgeModule.h"
#endif
#import <CoreLocation/CoreLocation.h>
#import <React/RCTConvert.h>
#import <React/RCTEventEmitter.h>

@interface RNBoundary : RCTEventEmitter <RCTBridgeModule, CLLocationManagerDelegate>
- (bool) removeBoundary:(NSString *)boundaryId;
- (void) removeAllBoundaries;
@property (strong, nonatomic) CLLocationManager *locationManager;
@end
  
