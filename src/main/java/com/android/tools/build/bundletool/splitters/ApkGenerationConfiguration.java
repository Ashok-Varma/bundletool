/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.tools.build.bundletool.splitters;

import com.android.tools.build.bundletool.model.OptimizationDimension;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

/** Configuration to be passed to Module Splitters and Variant generators. */
@AutoValue
public abstract class ApkGenerationConfiguration {

  public abstract ImmutableSet<OptimizationDimension> getOptimizationDimensions();

  public abstract boolean isForInstantAppVariants();

  public abstract boolean getEnableNativeLibraryCompressionSplitter();

  public abstract boolean getEnableDexCompressionSplitter();

  public static ApkGenerationConfiguration.Builder builder() {
    return new AutoValue_ApkGenerationConfiguration.Builder()
        .setForInstantAppVariants(false)
        .setEnableNativeLibraryCompressionSplitter(false)
        .setEnableDexCompressionSplitter(false)
        .setOptimizationDimensions(ImmutableSet.of());
  }

  public static ApkGenerationConfiguration getDefaultInstance() {
    return ApkGenerationConfiguration.builder().build();
  }

  /** Builder for the {@link ApkGenerationConfiguration}. */
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setOptimizationDimensions(
        ImmutableSet<OptimizationDimension> optimizationDimensions);

    public abstract Builder setForInstantAppVariants(boolean forInstantAppVariants);

    public abstract Builder setEnableNativeLibraryCompressionSplitter(
        boolean enableNativeLibraryCompressionSplitter);

    public abstract Builder setEnableDexCompressionSplitter(boolean enableDexCompressionSplitter);

    public abstract ApkGenerationConfiguration build();
  }

  // Don't subclass outside the package. Hide the implicit constructor from IDEs/docs.
  ApkGenerationConfiguration() {}
}
