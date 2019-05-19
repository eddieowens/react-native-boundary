package com.eddieowens.services;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.bridge.Arguments;

public class BoundaryEventHeadlessTaskService extends HeadlessJsTaskService {
    @Nullable
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        return new HeadlessJsTaskConfig(
                "OnBoundaryEvent",
                extras != null ? Arguments.fromBundle(extras) : null,
                5000,
                true);
    }
}
