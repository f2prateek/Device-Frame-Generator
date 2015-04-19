/*
 * Copyright 2014 Prateek Srivastava (@f2prateek)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.f2prateek.dfg;

import android.app.Application;
import android.content.Context;
import android.view.WindowManager;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.f2prateek.dfg.model.Device;
import com.f2prateek.dfg.prefs.FirstRun;
import com.f2prateek.dfg.prefs.model.BooleanPreference;
import com.f2prateek.dfg.ui.ActivityHierarchyServer;
import com.f2prateek.ln.DebugLn;
import com.f2prateek.ln.Ln;
import com.segment.analytics.Analytics;
import com.squareup.otto.Bus;
import hugo.weaving.DebugLog;
import io.fabric.sdk.android.Fabric;
import javax.inject.Inject;

import static com.f2prateek.dfg.Utils.isStorageAvailable;

public class DFGApplication extends Application {
  DFGComponent component;
  @Inject ActivityHierarchyServer activityHierarchyServer;
  @Inject Bus bus;
  @Inject WindowManager windowManager;
  @Inject DeviceProvider deviceProvider;
  @Inject @FirstRun BooleanPreference firstRun;
  @Inject Analytics analytics;

  @Override
  public void onCreate() {
    super.onCreate();

    // Perform Injection
    buildApplicationGraphAndInject();

    registerActivityLifecycleCallbacks(activityHierarchyServer);

    if (BuildConfig.DEBUG) {
      Ln.set(DebugLn.from(this));
    } else {
      Fabric.with(this, new Crashlytics());
      Ln.set(new CrashlyticsLn(getPackageName()));
    }

    if (!isStorageAvailable()) {
      Toast.makeText(this, R.string.storage_unavailable, Toast.LENGTH_SHORT).show();
      Ln.w("storage unavailable");
    }

    if (firstRun.get()) {
      analytics.track("First launch");
      Device device = deviceProvider.find(windowManager);
      if (device != null) {
        deviceProvider.saveDefaultDevice(device);
        bus.post(new Events.DefaultDeviceUpdated(device));
      }
      firstRun.set(false);
    }
  }

  @DebugLog
  public void buildApplicationGraphAndInject() {
    component = RealDFGComponent.Initializer.init(this);
    component.inject(this);
  }

  public static DFGApplication get(Context context) {
    return (DFGApplication) context.getApplicationContext();
  }

  public DFGComponent component() {
    return component;
  }
}