
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
- (void) sendEventAfter:(NSString *)event withBody:(NSString *)body andRetry:(int)retry;
@property (strong, nonatomic) CLLocationManager *locationManager;
@end
