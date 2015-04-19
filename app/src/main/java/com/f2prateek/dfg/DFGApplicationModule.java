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

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import com.f2prateek.dfg.model.Device;
import com.f2prateek.dfg.prefs.DefaultDevice;
import com.f2prateek.dfg.prefs.model.StringPreference;
import com.segment.analytics.Analytics;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;
import java.util.Set;
import javax.inject.Singleton;

@Module
public class DFGApplicationModule {
  private final DFGApplication application;

  public DFGApplicationModule(DFGApplication application) {
    this.application = application;
  }

  @Provides @Singleton @ForApplication Context provideAppContext() {
    return application;
  }

  @Provides @Singleton //
  SharedPreferences provideDefaultSharedPreferences(@ForApplication Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  @Provides @Singleton Resources provideResources(@ForApplication Context context) {
    return context.getResources();
  }

  @Provides @Singleton PackageInfo providePackageInfo(@ForApplication Context context) {
    try {
      return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked") //
  <T> T getSystemService(final Context context, final String serviceConstant) {
    return (T) context.getSystemService(serviceConstant);
  }

  @Provides @Singleton //
  NotificationManager provideNotificationManager(@ForApplication Context context) {
    return getSystemService(context, Context.NOTIFICATION_SERVICE);
  }

  @Provides @Singleton WindowManager provideWindow(@ForApplication Context context) {
    return getSystemService(context, Context.WINDOW_SERVICE);
  }

  @Provides @Singleton Bus provideOttoBus() {
    return new Bus();
  }

  @Provides @Singleton //
  DeviceProvider devices(Set<Device> deviceSet, @DefaultDevice StringPreference defaultDevice) {
    return DeviceProvider.fromSet(deviceSet, defaultDevice);
  }

  @Provides @Singleton @AnalyticsKey String provideAnalyticsKey() {
    return "6bsdtx1twy";
  }

  @Provides @Singleton //
  Analytics provideAnalytics(@ForApplication Context context, @AnalyticsKey String key) {
    return new Analytics.Builder(context, key).build();
  }

  @Provides @Singleton Picasso providePicasso(@ForApplication Context app) {
    return new Picasso.Builder(app).build();
  }
}